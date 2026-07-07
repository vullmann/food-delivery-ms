# create NAT gateway
$natGatewayJson = aws ec2 create-nat-gateway `
  --subnet-id subnet-08f682fd2c785e7b0 `
  --allocation-id eipalloc-008d3f60de537652c `
  --connectivity-type public `
  --tag-specifications 'ResourceType=natgateway,Tags=[{Key=Name,Value=food-delivery-nat}]' `
  --region eu-central-1

$natGatewayId = ($natGatewayJson | ConvertFrom-Json).NatGateway.NatGatewayId

Write-Host "NAT Gateway created: $natGatewayId"

Write-Host "Waiting for NAT Gateway to become available..."

do
{
    Start-Sleep -Seconds 15
    $status = (aws ec2 describe-nat-gateways `
        --nat-gateway-ids $natGatewayId `
        --region eu-central-1 | ConvertFrom-Json).NatGateways[0].State
    Write-Host "Status: $status"
} while ($status -ne "available")

Write-Host "NAT Gateway is available"

# update routing table for private subnets using that NAT gateway
aws ec2 replace-route `
  --route-table-id rtb-0f13c7080ca1e13ae `
  --destination-cidr-block 0.0.0.0/0 `
  --nat-gateway-id $natGatewayId `
  --region eu-central-1

Write-Host "routing table for 1. private subnet updated"

aws ec2 replace-route `
  --route-table-id rtb-0c44655c4be3b485f `
  --destination-cidr-block 0.0.0.0/0 `
  --nat-gateway-id $natGatewayId `
  --region eu-central-1

Write-Host "routing table for 2. private subnet updated"

# create ALB
$albJson = aws elbv2 create-load-balancer `
  --name food-delivery-alb `
  --subnets subnet-08f682fd2c785e7b0 subnet-0fd1975309dd3b06d `
  --security-groups sg-0d4c00914a368c728 `
  --scheme internet-facing `
  --type application `
  --ip-address-type ipv4 `
  --region eu-central-1

$albArn = ($albJson | ConvertFrom-Json).LoadBalancers[0].LoadBalancerArn
Write-Host "ALB created: $albArn"

# create target group
$tgJson = aws elbv2 create-target-group `
  --name api-gateway-tg-food-delivery `
  --protocol HTTP `
  --port 8088 `
  --vpc-id vpc-0ab8e974a0ade6c48 `
  --target-type ip `
  --health-check-path /actuator/health `
  --health-check-interval-seconds 30 `
  --healthy-threshold-count 2 `
  --unhealthy-threshold-count 3 `
  --region eu-central-1

$tgArn = ($tgJson | ConvertFrom-Json).TargetGroups[0].TargetGroupArn
Write-Host "Target Group created: $tgArn"

# create listener
aws elbv2 create-listener `
  --load-balancer-arn $albArn `
  --protocol HTTP `
  --port 80 `
  --default-actions Type=forward,TargetGroupArn=$tgArn `
  --region eu-central-1 | Out-Null

Write-Host "Listener created"

# create api-gateway-service with ALB target group
aws ecs create-service `
  --cluster food-delivery-cluster `
  --service-name "api-gateway-service" `
  --task-definition "api-gateway" `
  --desired-count 1 `
  --launch-type FARGATE `
  --network-configuration "awsvpcConfiguration={subnets=[subnet-0746dc0eecff69632,subnet-0ef48475b096607b4],securityGroups=[sg-0a5484544d557ce43],assignPublicIp=DISABLED}" `
  --load-balancers "targetGroupArn=$tgArn,containerName=api-gateway,containerPort=8088" `
  --health-check-grace-period-seconds 120 `
  --region eu-central-1 | Out-Null

Write-Host "api-gateway-service created with ALB"


# wait for RDS to be fully stopped before starting
Write-Host "Checking RDS status before starting..."

do
{
    $rdsStatus = (aws rds describe-db-instances `
        --db-instance-identifier database-food-delivery `
        --region eu-central-1 | ConvertFrom-Json).DBInstances[0].DBInstanceStatus
    Write-Host "RDS Status: $rdsStatus"
    if ($rdsStatus -ne "stopped")
    {
        Start-Sleep -Seconds 30
    }
} while ($rdsStatus -ne "stopped")

# start RDS database
aws rds start-db-instance `
  --db-instance-identifier database-food-delivery `
  --region eu-central-1 | Out-Null

Write-Host "Waiting for RDS to become available..."

do
{
    Start-Sleep -Seconds 30
    $rdsStatus = (aws rds describe-db-instances `
        --db-instance-identifier database-food-delivery `
        --region eu-central-1 | ConvertFrom-Json).DBInstances[0].DBInstanceStatus
    Write-Host "RDS Status: $rdsStatus"
} while ($rdsStatus -ne "available")

Write-Host "RDS is available"


# restart container services - discovery server first
aws ecs update-service --cluster food-delivery-cluster --service "discovery-server-service" --force-new-deployment --desired-count 1 --region eu-central-1 | Out-Null
Write-Host "started service: discovery-server-service"

Write-Host "Waiting for discovery-server to be healthy..."

do
{
    Start-Sleep -Seconds 15
    $runningCount = (aws ecs describe-services `
        --cluster food-delivery-cluster `
        --services "discovery-server-service" `
        --region eu-central-1 | ConvertFrom-Json).services[0].runningCount
    Write-Host "discovery-server running tasks: $runningCount"
} while ($runningCount -lt 1)

Write-Host "discovery-server is running"

# restart remaining container services
$services = @("customer-service", "restaurant-service", "order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "chat-service")

foreach ($svc in $services)
{
    aws ecs update-service --cluster food-delivery-cluster --service "$svc-service" --force-new-deployment --desired-count 1 --region eu-central-1 | Out-Null
    Write-Host "started service: $svc-service"
}

$albDns = (aws elbv2 describe-load-balancers `
  --names food-delivery-alb `
  --region eu-central-1 | ConvertFrom-Json).LoadBalancers[0].DNSName

Write-Host "ALB DNS: $albDns"