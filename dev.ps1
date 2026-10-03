# Sobe e para o SO2 no Windows.
# Uso, cada um na sua aba do terminal do Cursor:
#   .\dev.ps1 up
#   .\dev.ps1 api
#   .\dev.ps1 web
#   .\dev.ps1 down

param(
    [Parameter(Position = 0)]
    [ValidateSet('up', 'api', 'web', 'down')]
    [string]$Command = 'up'
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

switch ($Command) {
    'up' {
        docker start so2_postgres_iam
        docker compose -f (Join-Path $root 'docker-compose.yml') up -d mailpit minio minio-init
        Write-Host ''
        Write-Host 'Infra no ar. Banco :5433, Mailpit :8025, MinIO :9000.'
        Write-Host 'Em outra aba: .\dev.ps1 api'
        Write-Host 'Em outra aba: .\dev.ps1 web'
    }
    'api' {
        Set-Location (Join-Path $root 'backend')
        mvn spring-boot:run
    }
    'web' {
        Set-Location (Join-Path $root 'frontend-react')
        if (-not (Test-Path 'node_modules')) {
            npm install
        }
        npm run dev
    }
    'down' {
        docker compose -f (Join-Path $root 'docker-compose.yml') stop mailpit minio
        docker stop so2_postgres_iam
        Write-Host 'Banco, Mailpit e MinIO parados. API e site: Ctrl+C na aba de cada um.'
    }
}
