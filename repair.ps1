# stop container services
$services = @("discovery-server", "customer-service", "restaurant-service", "order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "chat-service")

foreach ($svc in $services)
{
    aws ecs update-service --cluster food-delivery-cluster --service "$svc-service" --desired-count 0 --region eu-central-1 | Out-Null
    Write-Host "stopped service: $svc-service"
}