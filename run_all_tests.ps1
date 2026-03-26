$ErrorActionPreference = "Stop"

$services = @(
    "JPMS-EurekaServer",
    "JPMS-ApiGateWay",
    "JPMS-AuthService",
    "JPMS-JobService",
    "JPMS-AdminService",
    "JPMS-ApplicationService",
    "JPMS-NotificationService"
)

foreach ($svc in $services) {
    Write-Host "========================================"
    Write-Host "Testing $svc..."
    Write-Host "========================================"
    Push-Location $svc
    
    # Run tests
    & .\mvnw.cmd test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Tests failed in $svc" -ForegroundColor Red
        Pop-Location
        exit $LASTEXITCODE
    }
    
    Pop-Location
}

Write-Host "All tests completed successfully!" -ForegroundColor Green
