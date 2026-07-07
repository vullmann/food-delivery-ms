#     "discovery-server", "customer-service", "restaurant-service", "order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "api-gateway", "chat-service",

# $services = @("order-service", "delivery-service", "notification-service", "auth-service")

$services = @("order-service", "delivery-service", "auth-service", "notification-service", "mcp-service", "api-gateway", "chat-service")

foreach ($svc in $services)
{
    aws ecs create-service --cluster food-delivery-cluster --service-name "$svc-service" --task-definition "$svc" --desired-count 1 --launch-type FARGATE --network-configuration "awsvpcConfiguration={subnets=[subnet-0ef48475b096607b4,subnet-0746dc0eecff69632],securityGroups=[sg-0a5484544d557ce43],assignPublicIp=DISABLED}" --region eu-central-1 | Out-Null
    Write-Host "Created service: $svc-service"
}

