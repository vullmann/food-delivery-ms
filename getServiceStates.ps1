aws ecs describe-services `
  --cluster food-delivery-cluster `
  --services discovery-server-service customer-service-service restaurant-service-service order-service-service delivery-service-service auth-service-service notification-service-service mcp-service-service api-gateway-service chat-service-service `
  --query "services[*].{Service:serviceName,Running:runningCount,Desired:desiredCount,Status:status}" `
  --output table `
  --region eu-central-1