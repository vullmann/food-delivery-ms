# ============================================================
# Build & Push all food-delivery-ms images to ECR
# Usage: .\build-and-push.ps1
# Run from the root directory of food-delivery-ms
# ============================================================

$AWS_ACCOUNT = "207206034400"
$AWS_REGION = "eu-central-1"
$ECR_BASE = "$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com/food-delivery"

$services = @(
    "customer-service",
    "restaurant-service",
    "order-service",
    "delivery-service",
    "auth-service",
    "notification-service",
    "mcp-service",
    "chat-service",
    "api-gateway",
    "discovery-server"
)

# ------------------------------------------------------------
# 1. ECR Login
# ------------------------------------------------------------
Write-Host "`n[1/3] Logging in to ECR..." -ForegroundColor Cyan
aws ecr get-login-password --region $AWS_REGION | `
    docker login --username AWS --password-stdin "$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com"

if ($LASTEXITCODE -ne 0)
{
    Write-Host "ECR login failed. Aborting." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------
# 2. Maven Build
# ------------------------------------------------------------
Write-Host "`n[2/3] Running Maven build..." -ForegroundColor Cyan
mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0)
{
    Write-Host "Maven build failed. Aborting." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------
# 3. Docker Build & Push
# ------------------------------------------------------------
Write-Host "`n[3/3] Building and pushing Docker images..." -ForegroundColor Cyan

$failed = @()

foreach ($svc in $services)
{
    $image = "$ECR_BASE/$svc`:latest"
    Write-Host "`n>>> $svc" -ForegroundColor Yellow

    docker build -t $image ./$svc
    if ($LASTEXITCODE -ne 0)
    {
        Write-Host "Build FAILED for $svc" -ForegroundColor Red
        $failed += $svc
        continue
    }

    docker push $image
    if ($LASTEXITCODE -ne 0)
    {
        Write-Host "Push FAILED for $svc" -ForegroundColor Red
        $failed += $svc
        continue
    }

    Write-Host "OK: $svc pushed successfully" -ForegroundColor Green
}

# ------------------------------------------------------------
# Summary
# ------------------------------------------------------------
Write-Host "`n============================================================" -ForegroundColor Cyan
if ($failed.Count -eq 0)
{
    Write-Host "All images built and pushed successfully!" -ForegroundColor Green
}
else
{
    Write-Host "The following services FAILED:" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
}
Write-Host "============================================================`n" -ForegroundColor Cyan