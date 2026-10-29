<#
.SYNOPSIS
    Script de arranque para los 3 servicios del proyecto E-Commerce Composite API.
    - Microservicio 1 (Catálogo de Productos): http://localhost:3001
    - Microservicio 2 (Inventario y Almacén): http://localhost:3002
    - Composite API Gateway & Dashboard Web:  http://localhost:3000
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "  UTCH - E-Commerce Gateway & Dashboard (Composite API)       " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

Write-Host "`n[1/4] Empaquetando los 3 microservicios con Maven..." -ForegroundColor Yellow
.\mvnw.cmd package -DskipTests -o

if (!(Test-Path "$ProjectRoot\logs")) {
    New-Item -ItemType Directory -Path "$ProjectRoot\logs" | Out-Null
}

Write-Host "[2/4] Iniciando Microservicio 1: Catalog Service (:3001)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-jar", "catalog-service\target\catalog-service-1.0.0.jar" `
    -RedirectStandardOutput "logs\catalog-service.log" -RedirectStandardError "logs\catalog-service-err.log" -WindowStyle Hidden

Write-Host "[3/4] Iniciando Microservicio 2: Inventory Service (:3002)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-jar", "inventory-service\target\inventory-service-1.0.0.jar" `
    -RedirectStandardOutput "logs\inventory-service.log" -RedirectStandardError "logs\inventory-service-err.log" -WindowStyle Hidden

Write-Host "[4/4] Iniciando Composite API Gateway & Dashboard (:3000)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-jar", "composite-gateway\target\composite-gateway-1.0.0.jar" `
    -RedirectStandardOutput "logs\composite-gateway.log" -RedirectStandardError "logs\composite-gateway-err.log" -WindowStyle Hidden

Write-Host "`nServicios iniciados correctamente:" -ForegroundColor Cyan
Write-Host " -> Dashboard Web & Composite API : http://localhost:3000"
Write-Host " -> Consola Pruebas REST Client   : http://localhost:3000/rest-client.html"
Write-Host " -> Endpoint Composite Summary    : http://localhost:3000/api/v1/product-summary/P-101"
Write-Host " -> Microservicio 1 (Catálogo)    : http://localhost:3001/microservices/products/P-101"
Write-Host " -> Microservicio 2 (Inventario)  : http://localhost:3002/microservices/inventory/P-101"

