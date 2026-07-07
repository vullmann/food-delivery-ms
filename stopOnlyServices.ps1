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

Write-Host "all services stopped"