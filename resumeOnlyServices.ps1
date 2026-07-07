# create api-gateway-service with ALB target group
aws ecs create-service `
  --cluster food-delivery-cluster `
  --service-name "api-gateway-service" `
  --task-definition "api-gateway" `
  --desired-count 1 `
  --launch-type FARGATE `
  --network-configuration "awsvpcConfiguration={subnets=[subnet-0746dc0eecff69632,subnet-0ef48475b096607b4],securityGroups=[sg-0a5484544d557ce43],assignPublicIp=DISABLED}" `
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:207206034400:targetgroup/api-gateway-tg-food-delivery/58ff4e63e2348cdb,containerName=api-gateway,containerPort=8088" `
  --health-check-grace-period-seconds 120 `
  --region eu-central-1 | Out-Null

Write-Host "api-gateway-service created with ALB"

# restart container services - discovery server first
aws ecs update-service --cluster food-delivery-cluster --service "discovery-server-service" --desired-count 1 --region eu-central-1 | Out-Null
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
    aws ecs update-service --cluster food-delivery-cluster --service "$svc-service" --desired-count 1 --region eu-central-1 | Out-Null
    Write-Host "started service: $svc-service"
}

Write-Host "all services started"

