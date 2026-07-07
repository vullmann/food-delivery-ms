# Phase 7: ALB in front of api-gateway only
# Creates: ALB SG, ALB, Target Group, HTTP:80 Listener
# Recreates api-gateway ECS service with the target group attached
#
# Usage: .\createAlb.ps1
# Prerequisites:
#   - Phase 6 task definitions registered in ECS
#   - aws CLI configured (eu-central-1, account 207206034400)
#
# Idempotency: NOT idempotent — rerunning will fail on duplicate SG/ALB names.
# To recreate from scratch, delete the ALB, target group, and ALB SG first.

$REGION           = "eu-central-1"
$CLUSTER          = "food-delivery-cluster"
$ECS_SVC_SG       = "sg-0a5484544d557ce43"   # shared SG used by all ECS tasks
$PRIVATE_SUBNET_1 = "subnet-0ef48475b096607b4"
$PRIVATE_SUBNET_2 = "subnet-0746dc0eecff69632"
$API_GW_PORT      = 8088

# ── 1. VPC ────────────────────────────────────────────────────────────────────
Write-Host "`n[1/7] Discovering VPC..." -ForegroundColor Cyan
$vpcId = (aws ec2 describe-subnets `
  --subnet-ids $PRIVATE_SUBNET_1 `
  --region $REGION | ConvertFrom-Json).Subnets[0].VpcId
Write-Host "VPC: $vpcId"

# ── 2. Public subnets ─────────────────────────────────────────────────────────
Write-Host "`n[2/7] Finding public subnets (map-public-ip-on-launch=true)..." -ForegroundColor Cyan
$publicSubnets = (aws ec2 describe-subnets `
  --filters "Name=vpc-id,Values=$vpcId" "Name=map-public-ip-on-launch,Values=true" `
  --region $REGION | ConvertFrom-Json).Subnets

if ($publicSubnets.Count -lt 2) {
    Write-Host "ERROR: ALB requires >= 2 public subnets; found $($publicSubnets.Count)." -ForegroundColor Red
    Write-Host "Verify subnets have 'Enable auto-assign public IPv4 address' enabled in the console." -ForegroundColor Red
    exit 1
}
$pubIds = $publicSubnets | Select-Object -ExpandProperty SubnetId
Write-Host "Public subnets: $($pubIds -join ', ')"

# ── 3. ALB security group ─────────────────────────────────────────────────────
Write-Host "`n[3/7] Creating ALB security group..." -ForegroundColor Cyan
$albSgId = (aws ec2 create-security-group `
  --group-name food-delivery-alb-sg `
  --description "Internet-facing ALB for food-delivery api-gateway" `
  --vpc-id $vpcId `
  --region $REGION | ConvertFrom-Json).GroupId
Write-Host "ALB SG: $albSgId"

# Inbound HTTP:80 from the internet
aws ec2 authorize-security-group-ingress `
  --group-id $albSgId `
  --protocol tcp --port 80 `
  --cidr 0.0.0.0/0 `
  --region $REGION | Out-Null
Write-Host "Allowed inbound TCP:80 on ALB SG"

# Allow ALB to reach api-gateway on port 8088 inside the ECS services SG
aws ec2 authorize-security-group-ingress `
  --group-id $ECS_SVC_SG `
  --protocol tcp --port $API_GW_PORT `
  --source-group $albSgId `
  --region $REGION | Out-Null
Write-Host "Allowed ALB SG → ECS SG TCP:$API_GW_PORT"

# ── 4. Application Load Balancer ──────────────────────────────────────────────
Write-Host "`n[4/7] Creating Application Load Balancer..." -ForegroundColor Cyan
$albResult = aws elbv2 create-load-balancer `
  --name food-delivery-alb `
  --subnets $pubIds `
  --security-groups $albSgId `
  --scheme internet-facing `
  --type application `
  --ip-address-type ipv4 `
  --region $REGION | ConvertFrom-Json
$albArn = $albResult.LoadBalancers[0].LoadBalancerArn
$albDns = $albResult.LoadBalancers[0].DNSName
Write-Host "ALB ARN: $albArn"
Write-Host "ALB DNS: $albDns"

# ── 5. Target group (IP type — required for Fargate awsvpc networking) ─────────
Write-Host "`n[5/7] Creating target group..." -ForegroundColor Cyan
$tgArn = (aws elbv2 create-target-group `
  --name api-gateway-tg `
  --protocol HTTP `
  --port $API_GW_PORT `
  --vpc-id $vpcId `
  --target-type ip `
  --health-check-protocol HTTP `
  --health-check-path /actuator/health `
  --health-check-interval-seconds 30 `
  --health-check-timeout-seconds 5 `
  --healthy-threshold-count 2 `
  --unhealthy-threshold-count 3 `
  --region $REGION | ConvertFrom-Json).TargetGroups[0].TargetGroupArn
Write-Host "Target group: $tgArn"

# ── 6. HTTP listener ──────────────────────────────────────────────────────────
Write-Host "`n[6/7] Creating HTTP:80 listener..." -ForegroundColor Cyan
aws elbv2 create-listener `
  --load-balancer-arn $albArn `
  --protocol HTTP --port 80 `
  --default-actions "Type=forward,TargetGroupArn=$tgArn" `
  --region $REGION | Out-Null
Write-Host "Listener: HTTP:80 → api-gateway-tg"

# ── 7. Recreate api-gateway ECS service with target group ─────────────────────
# ECS does not support adding a load balancer to an existing service —
# the service must be deleted and recreated with --load-balancers.
Write-Host "`n[7/7] Recreating api-gateway ECS service with ALB..." -ForegroundColor Cyan

$svcCheck = (aws ecs describe-services `
  --cluster $CLUSTER --services api-gateway-service `
  --region $REGION | ConvertFrom-Json).services
$existingActive = $svcCheck | Where-Object { $_.status -ne "INACTIVE" }

if ($existingActive) {
    Write-Host "Scaling existing api-gateway-service to 0..."
    aws ecs update-service `
      --cluster $CLUSTER --service api-gateway-service `
      --desired-count 0 `
      --region $REGION | Out-Null

    Write-Host "Deleting api-gateway-service..."
    aws ecs delete-service `
      --cluster $CLUSTER --service api-gateway-service `
      --region $REGION | Out-Null

    Write-Host "Waiting for service to reach INACTIVE..."
    do {
        Start-Sleep -Seconds 10
        $svcStatus = (aws ecs describe-services `
          --cluster $CLUSTER --services api-gateway-service `
          --region $REGION | ConvertFrom-Json).services[0].status
        Write-Host "  Status: $svcStatus"
    } while ($svcStatus -ne "INACTIVE")
} else {
    Write-Host "No active api-gateway-service found — creating fresh."
}

aws ecs create-service `
  --cluster $CLUSTER `
  --service-name api-gateway-service `
  --task-definition api-gateway `
  --desired-count 1 `
  --launch-type FARGATE `
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SVC_SG],assignPublicIp=DISABLED}" `
  --load-balancers "targetGroupArn=$tgArn,containerName=api-gateway,containerPort=$API_GW_PORT" `
  --health-check-grace-period-seconds 120 `
  --region $REGION | Out-Null
Write-Host "api-gateway-service created with ALB target group"

# ── Done ──────────────────────────────────────────────────────────────────────
Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "Phase 7 complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Public endpoint:  http://$albDns" -ForegroundColor Yellow
Write-Host ""
Write-Host "Health check:     http://$albDns/actuator/health" -ForegroundColor Yellow
Write-Host "Auth login:       POST http://$albDns/auth/login" -ForegroundColor Yellow
Write-Host "============================================================`n" -ForegroundColor Green
