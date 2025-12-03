# 🚀 AC2 - Evolução para Arquitetura de Microserviços

## 📋 Descrição

Este projeto evoluiu do monolito de Clean Architecture + DDD (AC2) para uma **arquitetura orientada a eventos com RabbitMQ**, demonstrando conceitos de **microserviços** e **mensageria**.

### 🎯 O que foi implementado (Item 5 da AF)

✅ **Arquitetura Event-Driven** com RabbitMQ  
✅ **Publisher/Consumer Pattern** (Producer/Consumer)  
✅ **Clean Architecture + DDD** mantido (ports/adapters)  
✅ **4 Microserviços simulados** como consumers  
✅ **Testes unitários com Mocks** para mensageria  
✅ **Docker Compose** para infraestrutura  
✅ **Logging completo** para demonstração  

---

## 🏗️ Arquitetura Implementada

### Antes (Monolito)
```
[Controller] → [UseCase] → [Repository] → [Database]
```

### Depois (Event-Driven Microservices)
```
[Controller] → [UseCase] → [Repository] → [Database]
                    ↓
              [EventPublisher]
                    ↓
              [RabbitMQ Exchange]
                    ↓
         ┌──────────┼──────────┐
         ↓          ↓          ↓
    [Queue 1]  [Queue 2]  [Queue 3]
         ↓          ↓          ↓
   [Consumer 1] [Consumer 2] [Consumer 3]
   (Email)      (Certif.)   (Gamific.)
```

---

## 🔧 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring AMQP** (RabbitMQ)
- **RabbitMQ** (Mensageria)
- **H2 Database** (In-memory)
- **JUnit 5 + Mockito** (Testes)
- **Docker & Docker Compose**
- **Lombok**
- **JaCoCo** (Cobertura de testes)

---

## 📦 Estrutura do Projeto

```
src/main/java/com/devops/projeto_ac2/
├── domain/
│   ├── entities/          # Entidades DDD (Aluno)
│   ├── events/            # 🆕 Eventos de domínio
│   ├── ports/             # 🆕 Interfaces (EventPublisher)
│   ├── repositories/      # Ports para persistência
│   └── valueobjects/      # Value Objects DDD
│
├── application/
│   └── usecases/          # 🔄 UseCases (agora publicam eventos)
│
└── infrastructure/
    ├── messaging/         # 🆕 Camada de mensageria
    │   ├── config/        # Config do RabbitMQ
    │   ├── adapters/      # RabbitMQEventPublisher
    │   └── consumers/     # 🆕 Microserviços (Listeners)
    └── persistence/       # Implementação JPA
```

---

## 🚀 Como Executar

### 1️⃣ Subir o RabbitMQ com Docker

```powershell
docker-compose up -d
```

**Verificar se subiu:**
- RabbitMQ Management UI: http://localhost:15672
- Login: `admin` / `admin123`

### 2️⃣ Compilar e Testar

```powershell
mvn clean install
```

Isso irá:
- ✅ Compilar o projeto
- ✅ Rodar todos os testes (incluindo testes de mensageria com mocks)
- ✅ Gerar relatório JaCoCo em `target/site/jacoco/index.html`

### 3️⃣ Executar a Aplicação

```powershell
mvn spring-boot:run
```

A aplicação estará disponível em: http://localhost:8080

### 4️⃣ Acessar Swagger (Testar APIs)

http://localhost:8080/swagger-ui.html

---

## 🧪 Testando o Sistema de Mensageria

### Cenário 1: Criar um Aluno

**Endpoint:** `POST /api/alunos`

**Body:**
```json
{
  "nome": "João Silva",
  "ra": "RA123456"
}
```

**O que acontece:**
1. UseCase cria o aluno no banco
2. Publica evento `AlunoCriado` no RabbitMQ
3. Consumer `EmailService` consome e "envia email de boas-vindas"

**Logs esperados:**
```
INFO - Aluno criado com sucesso - ID: 1
INFO - Evento AlunoCriado publicado - AlunoID: 1, EventID: xxx
INFO - MICROSERVIÇO: Email Service
INFO - Enviando email de boas-vindas...
```

### Cenário 2: Concluir um Curso

**Endpoint:** `POST /api/alunos/{id}/concluir`

**Body:**
```json
{
  "mediaFinal": 8.5
}
```

**O que acontece:**
1. UseCase atualiza aluno no banco (concluiu = true)
2. Publica evento `AlunoConcluido` no RabbitMQ
3. **3 Consumers** processam o evento simultaneamente:
   - `CertificadoService` → Gera certificado PDF
   - `GamificacaoService` → Atribui pontos e badges
   - `EmailService` → Envia email de parabéns

**Logs esperados:**
```
INFO - MICROSERVIÇO: Certificado Service
INFO - Gerando certificado de conclusão...

INFO - MICROSERVIÇO: Gamificação Service
INFO - Pontos ganhos: 800
INFO - Badge conquistado: Alto Desempenho
```

### Cenário 3: Registrar Tentativa

**Endpoint:** `POST /api/alunos/{id}/tentativas`

**Body:**
```json
{
  "nota": 7.0
}
```

**O que acontece:**
1. UseCase registra tentativa no banco
2. Publica evento `TentativaRegistrada`
3. Consumer `AnalyticsService` registra métricas

---

## 📊 Visualizando Mensagens no RabbitMQ

1. Acesse: http://localhost:15672
2. Login: `admin` / `admin123`
3. Vá em **Queues**
4. Você verá as filas:
   - `aluno.criado.queue`
   - `aluno.concluido.queue`
   - `aluno.tentativa.queue`

5. Clique em uma fila → **Get Messages** para ver mensagens

---

## 🧪 Executando Testes

### Todos os testes
```powershell
mvn test
```

### Apenas testes de mensageria
```powershell
mvn test -Dtest="*Mensageria*"
```

### Ver cobertura JaCoCo
```powershell
mvn clean test jacoco:report
# Abrir: target/site/jacoco/index.html
```

---

## 🎯 Microserviços Implementados (Simulados)

### 1. EmailService
- **Fila:** `aluno.criado.queue`
- **Função:** Envia emails de boas-vindas quando aluno é criado
- **Em produção:** Integraria com SendGrid, AWS SES, etc.

### 2. CertificadoService
- **Fila:** `aluno.concluido.queue`
- **Função:** Gera certificado PDF quando aluno conclui curso
- **Em produção:** Usaria iText, JasperReports, etc.

### 3. GamificacaoService
- **Fila:** `aluno.concluido.queue`
- **Função:** Atribui pontos e badges baseado na performance
- **Em produção:** Integraria com banco de gamificação

### 4. AnalyticsService
- **Fila:** `aluno.tentativa.queue`
- **Função:** Registra métricas e estatísticas de tentativas
- **Em produção:** Enviaria para Elasticsearch, BigQuery, etc.

---

## 🎓 Conceitos Demonstrados

### 1. **Event-Driven Architecture**
- Eventos publicados quando ações importantes ocorrem
- Desacoplamento entre produtor e consumidores
- Múltiplos sistemas podem reagir ao mesmo evento

### 2. **Publisher/Consumer Pattern**
- **Publisher:** UseCases publicam eventos
- **Consumer:** Microserviços consomem eventos
- **Broker:** RabbitMQ gerencia as mensagens

### 3. **Clean Architecture + DDD**
- **Domain Events:** Eventos vivem no domínio
- **Ports:** Interface `EventPublisher` (independente de framework)
- **Adapters:** `RabbitMQEventPublisher` implementa o port
- **Domínio isolado:** Não conhece RabbitMQ

### 4. **Microserviços**
- Cada consumer representa um microserviço independente
- Em produção, cada um seria uma aplicação separada
- Comunicação assíncrona via mensageria

### 5. **Testes com Mocks**
- Testes unitários não dependem do RabbitMQ real
- Mockito simula o `EventPublisher`
- Testes rápidos e confiáveis para CI/CD

---

## 📝 Justificativa dos Mocks (Resposta para Item 3a da AF)

### Por que usar Mocks em testes de mensageria?

#### ✅ **Isolamento**
- Testa apenas a lógica do UseCase
- Não depende de RabbitMQ, Kafka ou MQTT rodando
- Falhas na infraestrutura não quebram testes unitários

#### ✅ **Velocidade**
- Testes executam em milissegundos
- Sem I/O de rede ou conexões externas
- CI/CD mais rápido

#### ✅ **Confiabilidade**
- Resultados determinísticos e reproduzíveis
- Não falha por timeout, conexão perdida, etc.
- Ambiente de teste sempre disponível

#### ✅ **Flexibilidade**
- Simula cenários difíceis (falhas, timeouts)
- Testa comportamento em condições adversas
- Verifica que eventos corretos são publicados

#### ✅ **Foco**
- Testa **lógica de negócio**, não infraestrutura
- Infraestrutura é testada em **testes de integração**
- Separação clara de responsabilidades

### Testes Unitários vs Testes de Integração

| Aspecto | Unitários (Mocks) | Integração (Real) |
|---------|-------------------|-------------------|
| **Velocidade** | Muito rápido (ms) | Lento (segundos) |
| **Dependências** | Nenhuma (mocks) | RabbitMQ, DB, etc. |
| **Confiabilidade** | Alta | Média (infraestrutura) |
| **Quando rodar** | Toda hora (TDD) | Antes de deploy |
| **CI/CD** | Sempre | Opcional/controlado |

---

## 🎬 Demonstração em Vídeo (Sugestão de Roteiro)

### Parte 1: Introdução (2 min)
1. Mostrar estrutura do projeto
2. Explicar evolução do monolito para microserviços
3. Mostrar diagrama de arquitetura

### Parte 2: Infraestrutura (3 min)
1. Mostrar `docker-compose.yml`
2. Executar `docker-compose up -d`
3. Acessar RabbitMQ Management (http://localhost:15672)
4. Mostrar exchanges e queues criadas

### Parte 3: Código (5 min)
1. Mostrar evento `AlunoCriadoEvent` (Domain Event)
2. Mostrar port `EventPublisher` (Clean Architecture)
3. Mostrar adapter `RabbitMQEventPublisher`
4. Mostrar UseCase publicando evento
5. Mostrar Consumer processando evento

### Parte 4: Testes (3 min)
1. Executar `mvn test`
2. Mostrar testes com mocks
3. Explicar por que mocks são importantes
4. Mostrar relatório JaCoCo

### Parte 5: Execução Real (5 min)
1. Executar `mvn spring-boot:run`
2. Acessar Swagger
3. Criar um aluno via API
4. Mostrar logs dos consumers processando
5. Acessar RabbitMQ e mostrar mensagens
6. Concluir curso e mostrar 3 consumers processando

### Parte 6: DevOps e Qualidade (2 min)
1. Mostrar cobertura de testes (JaCoCo)
2. Mostrar que aplicação pode rodar sem RabbitMQ em modo fallback
3. Explicar que isso permite deploy gradual
4. Conclusão e perguntas

---

## 🏆 Diferenciais Implementados

✅ **Clean Architecture mantida** - Domínio isolado de frameworks  
✅ **DDD preservado** - Eventos de domínio, Value Objects, Entidades  
✅ **Múltiplos consumers** - Demonstra escalabilidade  
✅ **Logs formatados** - Facilita demonstração e debug  
✅ **Testes completos** - Unitários com mocks + explicação  
✅ **Docker Compose** - Infraestrutura como código  
✅ **Configuração profissional** - Retry, prefetch, cache  
✅ **Documentação completa** - Este README  

---

## 📚 Referências e Conceitos

### RabbitMQ
- **Exchange:** Roteador de mensagens
- **Queue:** Fila de mensagens
- **Binding:** Ligação entre exchange e queue
- **Routing Key:** Chave para roteamento
- **Topic Exchange:** Permite padrões (aluno.*)

### Padrões de Mensageria
- **Publisher/Subscriber:** Um evento, múltiplos consumers
- **Work Queue:** Distribuição de carga entre workers
- **Dead Letter Queue:** Fila para mensagens com erro

### Clean Architecture
- **Ports:** Interfaces (contratos)
- **Adapters:** Implementações concretas
- **Domínio:** Isolado de frameworks e infraestrutura

---

## 👥 Autor

**Matheus Diogo**  
Projeto AC2/AF - Disciplina de DevOps  
Implementação: Evolução de Monolito para Microserviços com RabbitMQ

---

## 📌 Checklist da AF (Item 5)

- [x] Implementar recurso de microserviços (RabbitMQ)
- [x] Evoluir monolito Clean Architecture + DDD
- [x] Usar recursos do Spring Boot (AMQP)
- [x] Demonstrar DevOps (Docker, testes, CI-ready)
- [x] Demonstrar qualidade (JaCoCo, testes, logs)
- [x] Preparar demonstração em vídeo (roteiro pronto)

---

## 🆘 Troubleshooting

### Erro: "Connection refused" ao subir aplicação
```powershell
# Verificar se RabbitMQ está rodando
docker ps

# Se não estiver, subir novamente
docker-compose up -d
```

### Erro: "Port 5672 already in use"
```powershell
# Parar containers
docker-compose down

# Verificar se algum processo está usando a porta
netstat -ano | findstr :5672

# Reiniciar
docker-compose up -d
```

### Mensagens não aparecem no RabbitMQ
- Verificar se aplicação está conectada (logs devem mostrar conexão)
- Verificar se exchanges e queues foram criadas (Management UI)
- Verificar routing keys na configuração

---

## 🎉 Conclusão

Este projeto demonstra com sucesso a **evolução de um monolito para microserviços** utilizando **RabbitMQ** e mantendo os princípios de **Clean Architecture e DDD**.

A implementação está pronta para:
- ✅ Apresentação em vídeo
- ✅ Demonstração ao vivo
- ✅ Análise de código pelos professores
- ✅ Discussão técnica sobre arquitetura
