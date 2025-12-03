# 🚀 GUIA RÁPIDO - APRESENTAÇÃO MICROSERVIÇOS

> **Resumo executivo para apresentação de 5 minutos**

---

## 🎯 O QUE FOI FEITO?

Evoluímos um **sistema monolítico** para **arquitetura de microserviços** usando:
- **RabbitMQ** (mensageria assíncrona)
- **Event-Driven Architecture** (arquitetura orientada a eventos)
- **Clean Architecture + DDD** (boas práticas)

---

## 📊 ARQUITETURA EM 1 IMAGEM

```
API REST → Use Case → EventPublisher → RabbitMQ
                                          ↓
                              ┌───────────┼───────────┐
                              ↓           ↓           ↓
                         📧 Email    📜 Certificado  🎮 Gamificação
```

**Fluxo:**
1. Cliente chama API REST
2. Use Case executa lógica + publica evento
3. RabbitMQ recebe evento e roteia para filas
4. Microserviços (consumers) processam **assincronamente**

---

## 🔥 3 EVENTOS IMPLEMENTADOS

| Evento | Quando dispara | Quem consome |
|--------|----------------|--------------|
| **AlunoCriadoEvent** | Aluno cadastrado | EmailService (boas-vindas) |
| **AlunoConcluidoEvent** | Curso concluído | CertificadoService + GamificacaoService |
| **TentativaRegistradaEvent** | Tentativa registrada | AnalyticsService (métricas) |

---

## 🏗️ 4 MICROSERVIÇOS (Consumers)

### 📧 EmailService
- **Escuta**: `aluno.criado.queue`
- **Ação**: Envia email de boas-vindas

### 📜 CertificadoService
- **Escuta**: `aluno.concluido.queue`
- **Ação**: Gera PDF do certificado (se aprovado)

### 🎮 GamificacaoService
- **Escuta**: `aluno.concluido.queue`
- **Ação**: Atribui pontos e badges

### 📊 AnalyticsService
- **Escuta**: `aluno.tentativa.queue`
- **Ação**: Registra métricas de tentativas

---

## ⚙️ TECNOLOGIAS USADAS

- **Spring Boot 3.5.7** + Java 21
- **Spring AMQP** (RabbitMQ)
- **H2 Database** (in-memory)
- **JUnit 5 + Mockito** (testes)
- **JaCoCo** (99% cobertura)
- **Docker Compose** (infraestrutura)
- **Swagger/OpenAPI** (documentação)

---

## 🚀 COMO RODAR (3 comandos)

```bash
# 1. Subir RabbitMQ
docker-compose up -d

# 2. Compilar e testar
mvn clean install

# 3. Executar aplicação
mvn spring-boot:run
```

**Acessar:**
- API: http://localhost:8080/swagger-ui.html
- RabbitMQ: http://localhost:15672 (admin/admin123)

---

## 🧪 TESTAR (via PowerShell)

### Criar aluno:
```powershell
$body = @{
    nome = "João Silva"
    ra = "12345"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/alunos" `
    -Method POST -Body $body -ContentType "application/json"
```

**Log esperado:**
```
📧 EMAIL enviado para João Silva
```

### Concluir curso:
```powershell
$body = @{ mediaFinal = 8.5 } | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/alunos/1/concluir" `
    -Method POST -Body $body -ContentType "application/json"
```

**Logs esperados:**
```
📜 CERTIFICADO gerado para João Silva
🎮 GAMIFICAÇÃO: 100 pontos para João Silva
```

---

## 💡 POR QUE ISSO É MELHOR?

### Antes (Monolito):
```
POST /alunos → Criar + Email + Certificado + Pontos
                ↓
            Tudo SÍNCRONO
                ↓
        Se email falha = API trava ❌
```

### Depois (Microserviços):
```
POST /alunos → Criar aluno + Publicar evento
                ↓
        Responde IMEDIATAMENTE ✅
                ↓
        Services processam em background
```

---

## 🎯 BENEFÍCIOS (FALE NA APRESENTAÇÃO)

✅ **Escalabilidade**: Cada serviço escala independente  
✅ **Resiliência**: Se um cai, outros continuam  
✅ **Performance**: API responde rápido (assíncrono)  
✅ **Manutenção**: Times trabalham em paralelo  
✅ **Deploy**: Atualiza um serviço sem afetar outros  

---

## 🧪 COBERTURA DE TESTES

- **14 arquivos de teste**
- **99% de cobertura** (JaCoCo)
- **Testes unitários** (mocks, sem infraestrutura)
- **Testes de mensageria** (verifica eventos)
- **Testes de integração** (Spring Boot Test)

```bash
# Rodar testes
mvn test

# Ver relatório de cobertura
start target/site/jacoco/index.html
```

---

## 🗣️ ROTEIRO DE APRESENTAÇÃO (5 MIN)

### 1️⃣ Introdução (30s)
"Evoluímos sistema monolítico para microserviços com RabbitMQ, usando Event-Driven Architecture."

### 2️⃣ Problema (30s)
"No monolito, operações lentas (email, PDF) travavam a API. Agora são assíncronas via eventos."

### 3️⃣ Arquitetura (1min)
Mostrar diagrama: "API publica eventos → RabbitMQ roteia → 4 microserviços consomem"

### 4️⃣ Demo ao Vivo (2min)
```bash
# Mostrar RabbitMQ Management UI
start http://localhost:15672

# Criar aluno (mostrar logs dos consumers)
# Concluir curso (mostrar 2 consumers processando)
```

### 5️⃣ Testes (30s)
```bash
mvn test  # Mostrar todos passando
```

### 6️⃣ Conclusão (30s)
"Conseguimos: escalabilidade, resiliência, desenvolvimento paralelo. Sistema pronto para crescer."

---

## ❓ 3 PERGUNTAS MAIS PROVÁVEIS

### **Q1: "Por que RabbitMQ e não REST direto?"**
**R**: REST é síncrono (espera resposta). RabbitMQ é assíncrono (fire-and-forget). Se enviar email demora 5s, com REST usuário espera 5s. Com RabbitMQ responde instantâneo.

### **Q2: "E se RabbitMQ cair?"**
**R**: Temos retry policy (3 tentativas). Mensagens ficam em memória temporariamente. Em produção, configuramos persistência (não perde mensagens).

### **Q3: "Como garante que mensagem foi processada?"**
**R**: RabbitMQ usa acknowledgement (ACK). Consumer só confirma após sucesso. Se consumer cai, mensagem volta pra fila.

---

## 📋 CHECKLIST PRÉ-APRESENTAÇÃO

- [ ] RabbitMQ rodando: `docker ps`
- [ ] App compila: `mvn clean install`
- [ ] Testes passam: `mvn test`
- [ ] Swagger acessível: http://localhost:8080/swagger-ui.html
- [ ] RabbitMQ UI acessível: http://localhost:15672
- [ ] Testou demo 2x

---

## 🎓 CONCEITOS-CHAVE (MEMORIZE)

**Event-Driven Architecture (EDA)**  
Componentes se comunicam via eventos assíncronos. Publisher não conhece subscriber.

**Publisher/Subscriber Pattern**  
Publisher envia eventos → RabbitMQ → Subscribers recebem. Desacoplamento total.

**Clean Architecture**  
Camadas bem separadas. Domínio não depende de infraestrutura.

**Port/Adapter Pattern**  
Interface (port) define contrato. Implementação (adapter) usa tecnologia específica.

---

## 📦 ESTRUTURA DO PROJETO

```
domain/
├── events/              → AlunoCriadoEvent, AlunoConcluidoEvent
├── ports/              → EventPublisher (interface)
└── entities/           → Aluno

application/
└── usecases/           → CriarAlunoUseCase, ConcluirCursoUseCase

infrastructure/
├── messaging/
│   ├── config/         → RabbitMQConfig
│   ├── adapters/       → RabbitMQEventPublisher
│   └── consumers/      → 4 microserviços (Email, Certificado, etc)
└── persistence/        → AlunoRepositoryImpl
```

---

## 🔧 TROUBLESHOOTING RÁPIDO

**Erro: "Connection refused 5672"**  
→ RabbitMQ não está rodando: `docker-compose up -d`

**Erro: "Port 8080 already in use"**  
→ Mata processo: `Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess`

**Consumers não processam**  
→ Verifica logs: `mvn spring-boot:run` e procura por `@RabbitListener`

**Testes falhando**  
→ Limpa build: `mvn clean install`

---

## 📚 ARQUIVOS DE REFERÊNCIA

- `EXPLICACAO_COMPLETA_APRESENTACAO.md` → Guia detalhado completo
- `README_MICROSERVICES.md` → Visão técnica da implementação
- `ARQUITETURA_TECNICA.md` → Diagramas e decisões arquiteturais
- `GUIA_TESTES_API.md` → Como testar todos os endpoints
- `ROTEIRO_VIDEO.md` → Roteiro para gravação do vídeo
- `CHECKLIST_VALIDACAO.md` → Validação final
- `demo.ps1` → Script PowerShell automatizado

---

## ✨ PONTOS FORTES PARA DESTACAR

🎯 **Arquitetura moderna** (Event-Driven)  
🎯 **Boas práticas** (Clean Architecture, DDD, SOLID)  
🎯 **Alta cobertura de testes** (99%)  
🎯 **Documentação completa** (Swagger + 7 arquivos MD)  
🎯 **Pronto para produção** (Docker, health checks, retry)  
🎯 **Escalável** (microserviços independentes)  

---

## 🚀 MENSAGEM FINAL

**Você implementou uma arquitetura de nível profissional!**

- Desacoplamento via eventos ✅
- Escalabilidade horizontal ✅
- Resiliência a falhas ✅
- Testes robustos ✅
- DevOps ready ✅

**Confiança total. Você está preparado! 💪**

---

**📖 Para detalhes completos, consulte: `EXPLICACAO_COMPLETA_APRESENTACAO.md`**
