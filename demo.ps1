# Script de Demonstração - Microserviços com RabbitMQ
# Execute este script para testar a aplicação completa

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   DEMONSTRAÇÃO - MICROSERVIÇOS COM RABBITMQ               ║" -ForegroundColor Cyan
Write-Host "║   Projeto AC2 → AF - Evolução para Event-Driven          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Função para verificar se comando existe
function Test-Command {
    param($command)
    $null = Get-Command $command -ErrorAction SilentlyContinue
    return $?
}

# Verificar pré-requisitos
Write-Host "📋 Verificando pré-requisitos..." -ForegroundColor Yellow
Write-Host ""

$prereqOk = $true

# Java
if (Test-Command java) {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*version "([^"]*)".*', '$1' }
    Write-Host "✓ Java encontrado: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Java NÃO encontrado" -ForegroundColor Red
    $prereqOk = $false
}

# Maven
if (Test-Command mvn) {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven" | ForEach-Object { $_ -replace '.*Apache Maven ([^ ]*).*', '$1' }
    Write-Host "✓ Maven encontrado: $mvnVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Maven NÃO encontrado" -ForegroundColor Red
    $prereqOk = $false
}

# Docker
if (Test-Command docker) {
    $dockerVersion = docker --version | ForEach-Object { $_ -replace 'Docker version ([^,]*),.*', '$1' }
    Write-Host "✓ Docker encontrado: $dockerVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Docker NÃO encontrado" -ForegroundColor Red
    $prereqOk = $false
}

Write-Host ""

if (-not $prereqOk) {
    Write-Host "❌ Pré-requisitos faltando! Instale os componentes necessários." -ForegroundColor Red
    exit 1
}

# Menu de opções
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   MENU DE DEMONSTRAÇÃO                                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 🐰 Subir RabbitMQ (Docker)" -ForegroundColor White
Write-Host "2. 🧪 Compilar e Testar (mvn clean install)" -ForegroundColor White
Write-Host "3. 🚀 Executar Aplicação (mvn spring-boot:run)" -ForegroundColor White
Write-Host "4. 📊 Ver Relatório JaCoCo (Cobertura)" -ForegroundColor White
Write-Host "5. 🌐 Abrir RabbitMQ Management UI" -ForegroundColor White
Write-Host "6. 📖 Abrir Swagger UI" -ForegroundColor White
Write-Host "7. 🎬 Executar Demonstração Completa (1→2→3)" -ForegroundColor White
Write-Host "8. 🛑 Parar RabbitMQ (Docker)" -ForegroundColor White
Write-Host "0. ❌ Sair" -ForegroundColor White
Write-Host ""

$opcao = Read-Host "Escolha uma opção"

switch ($opcao) {
    "1" {
        Write-Host ""
        Write-Host "🐰 Subindo RabbitMQ com Docker Compose..." -ForegroundColor Yellow
        docker-compose up -d
        Write-Host ""
        Write-Host "✓ RabbitMQ iniciado!" -ForegroundColor Green
        Write-Host "  Management UI: http://localhost:15672" -ForegroundColor Cyan
        Write-Host "  Login: admin / admin123" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "2" {
        Write-Host ""
        Write-Host "🧪 Compilando e testando projeto..." -ForegroundColor Yellow
        Write-Host ""
        mvn clean install
        Write-Host ""
        Write-Host "✓ Build concluído!" -ForegroundColor Green
        Write-Host "  Relatório JaCoCo: target\site\jacoco\index.html" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "3" {
        Write-Host ""
        Write-Host "🚀 Iniciando aplicação Spring Boot..." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "⚠️  A aplicação irá rodar. Para parar, pressione Ctrl+C" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Aguarde alguns segundos para a aplicação inicializar..." -ForegroundColor Cyan
        Write-Host "Depois acesse: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
        Write-Host ""
        mvn spring-boot:run
    }
    
    "4" {
        Write-Host ""
        Write-Host "📊 Abrindo relatório JaCoCo..." -ForegroundColor Yellow
        $jacocoPath = "target\site\jacoco\index.html"
        if (Test-Path $jacocoPath) {
            Start-Process $jacocoPath
            Write-Host "✓ Relatório aberto no navegador!" -ForegroundColor Green
        } else {
            Write-Host "✗ Relatório não encontrado. Execute 'mvn clean test' primeiro." -ForegroundColor Red
        }
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "5" {
        Write-Host ""
        Write-Host "🌐 Abrindo RabbitMQ Management UI..." -ForegroundColor Yellow
        Start-Process "http://localhost:15672"
        Write-Host "✓ Management UI aberto no navegador!" -ForegroundColor Green
        Write-Host "  Login: admin / admin123" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "6" {
        Write-Host ""
        Write-Host "📖 Abrindo Swagger UI..." -ForegroundColor Yellow
        Start-Process "http://localhost:8080/swagger-ui.html"
        Write-Host "✓ Swagger UI aberto no navegador!" -ForegroundColor Green
        Write-Host "  Certifique-se de que a aplicação está rodando!" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "7" {
        Write-Host ""
        Write-Host "🎬 DEMONSTRAÇÃO COMPLETA" -ForegroundColor Yellow
        Write-Host ""
        
        # Passo 1: Subir RabbitMQ
        Write-Host "Passo 1/3: Subindo RabbitMQ..." -ForegroundColor Cyan
        docker-compose up -d
        Start-Sleep -Seconds 5
        Write-Host "✓ RabbitMQ iniciado!" -ForegroundColor Green
        Write-Host ""
        
        # Passo 2: Compilar e testar
        Write-Host "Passo 2/3: Compilando e testando..." -ForegroundColor Cyan
        mvn clean install
        Write-Host "✓ Build concluído!" -ForegroundColor Green
        Write-Host ""
        
        # Passo 3: Executar aplicação
        Write-Host "Passo 3/3: Iniciando aplicação..." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "⚠️  A aplicação irá rodar. Para parar, pressione Ctrl+C" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Acesse:" -ForegroundColor Cyan
        Write-Host "  - Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor White
        Write-Host "  - RabbitMQ: http://localhost:15672 (admin/admin123)" -ForegroundColor White
        Write-Host ""
        mvn spring-boot:run
    }
    
    "8" {
        Write-Host ""
        Write-Host "🛑 Parando RabbitMQ..." -ForegroundColor Yellow
        docker-compose down
        Write-Host ""
        Write-Host "✓ RabbitMQ parado!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "0" {
        Write-Host ""
        Write-Host "👋 Até logo!" -ForegroundColor Cyan
        exit 0
    }
    
    default {
        Write-Host ""
        Write-Host "❌ Opção inválida!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Pressione qualquer tecla para continuar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
}

Write-Host ""
Write-Host "════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
