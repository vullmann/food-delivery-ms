$AWS_ACCOUNT = "207206034400"
$AWS_REGION = "eu-central-1"
$ECR_BASE = "$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com/food-delivery"

Write-Host "`n[1/3] Logging in to ECR..." -ForegroundColor Cyan
aws ecr get-login-password --region $AWS_REGION | `
    docker login --username AWS --password-stdin "$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com"

if ($LASTEXITCODE -ne 0)
{
    Write-Host "ECR login failed. Aborting." -ForegroundColor Red
    exit 1
}

# delete api-gateway-service (will be re-created with fresh target group on resume)
aws ecs delete-service `
  --cluster food-delivery-cluster `
  --service "api-gateway-service" `
  --force `
  --region eu-central-1 | Out-Null

Write-Host "api-gateway-service deleted"

# stop container services
$services = @("discovery-server", "customer-service", "restaurant-service", "order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "chat-service")

foreach ($svc in $services)
{
    aws ecs update-service --cluster food-delivery-cluster --service "$svc-service" --desired-count 0 --region eu-central-1 | Out-Null
    Write-Host "stopped service: $svc-service"
}


# stop RDS database
aws rds stop-db-instance `
  --db-instance-identifier database-food-delivery `
  --region eu-central-1 | Out-Null

Write-Host "RDS stop initiated"


# delete NAT gateway
$natGatewayId = (aws ec2 describe-nat-gateways `
  --filter "Name=tag:Name,Values=food-delivery-nat" "Name=state,Values=available" `
  --region eu-central-1 | ConvertFrom-Json).NatGateways[0].NatGatewayId

aws ec2 delete-nat-gateway `
  --nat-gateway-id $natGatewayId `
  --region eu-central-1 | Out-Null

Write-Host "NAT Gateway deleted: $natGatewayId"

# delete ALB
$albArn = (aws elbv2 describe-load-balancers `
  --names "food-delivery-alb" `
  --region eu-central-1 | ConvertFrom-Json).LoadBalancers[0].LoadBalancerArn

aws elbv2 delete-load-balancer `
  --load-balancer-arn $albArn `
  --region eu-central-1 | Out-Null

Write-Host "ALB deleted: $albArn"

# wait for ALB to be fully deleted before deleting target group
Write-Host "Waiting for ALB to be fully deleted..."

# delete Target Group
$tgArn = (aws elbv2 describe-target-groups `
  --names "api-gateway-tg-food-delivery" `
  --region eu-central-1 | ConvertFrom-Json).TargetGroups[0].TargetGroupArn

do
{
    Start-Sleep -Seconds 15
    $lbs = (aws elbv2 describe-load-balancers `
        --load-balancer-arns $albArn `
        --region eu-central-1 2> $null | ConvertFrom-Json).LoadBalancers
    Write-Host "Waiting for ALB deletion..."
} while ($lbs.Count -gt 0)

Write-Host "ALB fully deleted"

aws elbv2 delete-target-group `
  --target-group-arn $tgArn `
  --region eu-central-1 | Out-Null

Write-Host "Target Group deleted"


