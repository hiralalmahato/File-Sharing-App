$ErrorActionPreference = "Stop"

# Free port 8080 if an older backend instance is still running.
$existing = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

if ($existing) {
    Write-Host "Stopping existing process on port 8080 (PID: $($existing.OwningProcess))..."
    Stop-Process -Id $existing.OwningProcess -Force
}

Write-Host "Starting Spring Boot backend on port 8080..."
Set-Location $PSScriptRoot
.\mvnw.cmd spring-boot:run
