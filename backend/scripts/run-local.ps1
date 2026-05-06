param(
    [ValidateSet(
        "gateway-service",
        "auth-service",
        "user-service",
        "workspace-service",
        "creative-service",
        "billing-service",
        "notification-service"
    )]
    [string]$Service = "gateway-service"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path "$PSScriptRoot\..\.."

docker compose -f "$Root\backend\docker\docker-compose.yml" up -d postgres redis
& "$Root\mvnw.cmd" -pl "backend/$Service" -am spring-boot:run "-Dspring-boot.run.profiles=local"
