$services = @("discovery-server", "customer-service", "restaurant-service", "order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "api-gateway", "chat-service")

# $services = @("discovery-server", "customer-service", "restaurant-service", "order-service", "delivery-service", "notification-service", "auth-service", "mcp-service", "api-gateway", "chat-service")

# $services = @("customer-service", "restaurant-service", "order-service", "delivery-service", "notification-service", "auth-service", "mcp-service", "api-gateway", "chat-service")



foreach ($svc in $services)
{
    aws ecs update-service --cluster food-delivery-cluster --service "$svc-service" --force-new-deployment --region eu-central-1 | Out-Null
    Write-Host "Updated service: $svc-service"
}


