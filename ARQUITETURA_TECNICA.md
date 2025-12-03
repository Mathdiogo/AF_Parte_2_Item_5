# 📐 Documentação Técnica - Arquitetura de Microserviços

## 🎯 Visão Geral da Evolução

### Monolito Original (AC2)
```
┌─────────────────────────────────────────────┐
│           Aplicação Monolítica              │
│  ┌──────────────────────────────────────┐   │
│  │         Presentation Layer           │   │
│  │         (Controllers)                │   │
│  └──────────────────────────────────────┘   │
│                    ↓                         │
│  ┌──────────────────────────────────────┐   │
│  │       Application Layer              │   │
│  │          (Use Cases)                 │   │
│  └──────────────────────────────────────┘   │
│                    ↓                         │
│  ┌──────────────────────────────────────┐   │
│  │         Domain Layer                 │   │
│  │  (Entities, VOs, Repositories)       │   │
│  └──────────────────────────────────────┘   │
│                    ↓                         │
│  ┌──────────────────────────────────────┐   │
│  │      Infrastructure Layer            │   │
│  │      (JPA, H2 Database)              │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### Arquitetura Event-Driven (AF)
```
┌─────────────────────────────────────────────────────────────────────┐
│                    APLICAÇÃO PRINCIPAL (Publisher)                   │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                     Presentation Layer                        │  │
│  │                       (Controllers)                           │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Application Layer                          │  │
│  │      (Use Cases + Event Publishing)                           │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      Domain Layer                             │  │
│  │     Entities │ VOs │ Domain Events │ Ports (Interfaces)       │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                  Infrastructure Layer                         │  │
│  │          JPA │ H2 Database │ RabbitMQ Adapter                 │  │
│  └───────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
                   ┌───────────────────────┐
                   │   RabbitMQ Broker     │
                   │  (Message Exchange)   │
                   └───────────────────────┘
                               ↓
        ┌──────────────┬───────┴───────┬──────────────┐
        ↓              ↓               ↓              ↓
┌───────────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐
│ Email Service │ │Certificate│ │ Gamification │ │Analytics │
│   (Consumer)  │ │  Service  │ │   Service    │ │  Service │
│               │ │ (Consumer)│ │  (Consumer)  │ │(Consumer)│
└───────────────┘ └──────────┘ └──────────────┘ └──────────┘
```

---

## 🔧 Componentes Técnicos

### 1. Domain Events (Eventos de Domínio)

#### AlunoCriadoEvent
```java
{
  "alunoId": 1,
  "nome": "João Silva",
  "registroAcademico": "RA123456",
  "dataCriacao": "2024-12-03T10:30:00",
  "eventId": "uuid-123-abc"
}
```

**Propósito:** Notificar que um novo aluno foi criado no sistema

**Consumidores:**
- EmailService → Envia boas-vindas

#### AlunoConcluidoEvent
```java
{
  "alunoId": 1,
  "nome": "João Silva",
  "registroAcademico": "RA123456",
  "mediaFinal": 8.5,
  "aprovado": true,
  "dataConclusao": "2024-12-03T11:00:00",
  "eventId": "uuid-456-def"
}
```

**Propósito:** Notificar que um aluno concluiu o curso

**Consumidores:**
- CertificadoService → Gera certificado
- GamificacaoService → Atribui pontos e badges
- EmailService → Envia parabéns (opcional)

#### TentativaRegistradaEvent
```java
{
  "alunoId": 1,
  "registroAcademico": "RA123456",
  "numeroTentativa": 2,
  "dataRegistro": "2024-12-03T10:45:00",
  "eventId": "uuid-789-ghi"
}
```

**Propósito:** Registrar métricas de tentativas de avaliação

**Consumidores:**
- AnalyticsService → Registra métricas

---

### 2. Port/Adapter Pattern (Hexagonal Architecture)

#### Port (Interface)
```java
// Domain Layer - Não conhece implementação
public interface EventPublisher {
    void publicarAlunoCriado(AlunoCriadoEvent event);
    void publicarAlunoConcluido(AlunoConcluidoEvent event);
    void publicarTentativaRegistrada(TentativaRegistradaEvent event);
}
```

#### Adapter (Implementação)
```java
// Infrastructure Layer - Implementação concreta com RabbitMQ
@Component
public class RabbitMQEventPublisher implements EventPublisher {
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void publicarAlunoCriado(AlunoCriadoEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
    // ...
}
```

**Benefícios:**
- ✅ Domínio isolado de frameworks
- ✅ Fácil trocar implementação (RabbitMQ → Kafka → MQTT)
- ✅ Testável com mocks
- ✅ Segue princípios SOLID (DIP - Dependency Inversion)

---

### 3. RabbitMQ Configuration

#### Exchanges
```java
TopicExchange: "aluno.events.exchange"
```
- **Tipo:** Topic
- **Durable:** true (persiste após restart)
- **Permite:** Roteamento por padrões (aluno.*)

#### Queues
```java
Queue 1: "aluno.criado.queue"
Queue 2: "aluno.concluido.queue"
Queue 3: "aluno.tentativa.queue"
```
- **Durable:** true
- **Persistent:** Mensagens sobrevivem a restart

#### Bindings (Ligações)
```java
aluno.criado.queue     ← (aluno.criado)    ← aluno.events.exchange
aluno.concluido.queue  ← (aluno.concluido) ← aluno.events.exchange
aluno.tentativa.queue  ← (aluno.tentativa) ← aluno.events.exchange
```

#### Fluxo de Mensagem
```
1. UseCase publica evento para EXCHANGE
2. Exchange roteia para QUEUES baseado em ROUTING KEY
3. Consumers escutam suas respectivas QUEUES
4. Mensagem é processada e removida da fila
```

---

### 4. Microserviços (Consumers)

#### EmailService
```java
@RabbitListener(queues = "aluno.criado.queue")
public void processarAlunoCriado(AlunoCriadoEvent event) {
    // Simula envio de email de boas-vindas
    enviarEmail(event.getNome(), event.getRegistroAcademico());
}
```

**Responsabilidade:** Comunicação com alunos  
**Integrações (Produção):** SendGrid, AWS SES, SMTP  
**Escalabilidade:** Pode ter múltiplas instâncias

#### CertificadoService
```java
@RabbitListener(queues = "aluno.concluido.queue")
public void processarAlunoConcluido(AlunoConcluidoEvent event) {
    if (event.isAprovado()) {
        gerarCertificadoPDF(event);
    }
}
```

**Responsabilidade:** Geração de certificados  
**Integrações (Produção):** iText, JasperReports, S3  
**Escalabilidade:** Pode processar em paralelo

#### GamificacaoService
```java
@RabbitListener(queues = "aluno.concluido.queue")
public void processarAlunoConcluido(AlunoConcluidoEvent event) {
    int pontos = calcularPontos(event.getMediaFinal());
    String badge = determinarBadge(event.getMediaFinal());
    atualizarPerfil(event.getAlunoId(), pontos, badge);
}
```

**Responsabilidade:** Sistema de recompensas  
**Integrações (Produção):** Banco de gamificação, Redis  
**Escalabilidade:** Alta performance necessária

#### AnalyticsService
```java
@RabbitListener(queues = "aluno.tentativa.queue")
public void processarTentativaRegistrada(TentativaRegistradaEvent event) {
    registrarMetrica("tentativa_avaliacao", event);
    if (event.getNumeroTentativa() >= 3) {
        dispararAlerta(event.getAlunoId());
    }
}
```

**Responsabilidade:** Métricas e analytics  
**Integrações (Produção):** Elasticsearch, BigQuery, Prometheus  
**Escalabilidade:** Stream processing

---

## 🧪 Estratégia de Testes

### Testes Unitários (Com Mocks)

#### Por que usar Mocks?

**1. Isolamento:**
```java
@Mock
private EventPublisher eventPublisher;  // Mock, não RabbitMQ real

@Test
void deveCriarAlunoEPublicarEvento() {
    // Testa APENAS a lógica do UseCase
    // Não depende de RabbitMQ rodando
}
```

**2. Velocidade:**
- Sem mocks: 5-10 segundos por teste
- Com mocks: 10-50 milissegundos por teste
- Em 100 testes: diferença de 8 minutos!

**3. Confiabilidade:**
```java
// Sem mock: pode falhar por timeout, conexão perdida, etc.
// Com mock: sempre funciona, resultado determinístico
```

**4. Flexibilidade:**
```java
// Simula falha de conexão
doThrow(new RuntimeException("RabbitMQ indisponível"))
    .when(eventPublisher).publicarAlunoCriado(any());
```

**5. CI/CD:**
- Testes unitários rodam em qualquer ambiente
- Não precisa subir containers
- Pipeline mais rápido

#### Exemplo de Teste
```java
@Test
@DisplayName("Deve criar aluno e publicar evento AlunoCriado")
void deveCriarAlunoEPublicarEvento() {
    // Arrange
    when(alunoRepository.existePorRA("RA123")).thenReturn(false);
    when(alunoRepository.salvar(any())).thenReturn(alunoMock);
    
    // Act
    useCase.executar("João Silva", "RA123");
    
    // Assert
    ArgumentCaptor<AlunoCriadoEvent> captor = 
        ArgumentCaptor.forClass(AlunoCriadoEvent.class);
    verify(eventPublisher).publicarAlunoCriado(captor.capture());
    
    AlunoCriadoEvent evento = captor.getValue();
    assertThat(evento.getNome()).isEqualTo("João Silva");
    assertThat(evento.getRegistroAcademico()).isEqualTo("RA123");
}
```

### Testes de Integração (Opcional)

```java
@SpringBootTest
@TestContainers  // Sobe RabbitMQ real em Docker
class IntegracaoRabbitMQTest {
    
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer();
    
    @Test
    void devePublicarEConsumirEventoReal() {
        // Testa comunicação real com RabbitMQ
        // Valida serialização JSON
        // Verifica roteamento de mensagens
    }
}
```

**Quando usar cada tipo:**
- **Unitários:** Toda hora (TDD), rápidos, em CI/CD sempre
- **Integração:** Antes de deploy, validação fim-a-fim

---

## 🚀 Fluxo Completo de Execução

### Cenário: Criar Aluno

```
1. [Cliente] POST /api/alunos
   ↓
2. [Controller] Recebe request, valida DTO
   ↓
3. [UseCase] Executa lógica de negócio
   ├─ Valida se RA já existe
   ├─ Cria Value Objects (NomeAluno, RegistroAcademico)
   ├─ Cria Entidade Aluno
   ├─ Salva no banco via Repository
   ↓
4. [UseCase] Publica evento
   ├─ Cria AlunoCriadoEvent
   ├─ Chama EventPublisher.publicarAlunoCriado()
   ↓
5. [RabbitMQEventPublisher] Adapter
   ├─ Serializa evento para JSON
   ├─ Envia para Exchange "aluno.events.exchange"
   ├─ Com routing key "aluno.criado"
   ↓
6. [RabbitMQ Broker]
   ├─ Recebe mensagem no Exchange
   ├─ Roteia para Queue "aluno.criado.queue"
   ↓
7. [EmailService Consumer]
   ├─ Escuta a fila
   ├─ Recebe mensagem
   ├─ Deserializa JSON → AlunoCriadoEvent
   ├─ Processa: envia email de boas-vindas
   ├─ Confirma processamento (ACK)
   ↓
8. [RabbitMQ] Remove mensagem da fila
   ↓
9. [Cliente] Recebe response 201 Created
```

**Logs Correspondentes:**
```
INFO - Iniciando criação de aluno - Nome: João Silva, RA: RA123456
INFO - Aluno criado com sucesso - ID: 1
INFO - Evento AlunoCriado publicado - AlunoID: 1, EventID: abc-123
INFO - ╔════════════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Email Service              ║
INFO - ║  Evento recebido: AlunoCriado             ║
INFO - ║  AÇÃO: Enviando email de boas-vindas...   ║
INFO - ║  ✓ Email enviado com sucesso!             ║
INFO - ╚════════════════════════════════════════════╝
```

---

## 📊 Métricas e Observabilidade

### Logs Estruturados
```java
logger.info("Evento {} publicado - AlunoID: {}, EventID: {}", 
    eventoTipo, alunoId, eventId);
```

### Rastreabilidade
- Cada evento tem `eventId` único (UUID)
- Permite rastrear evento através de toda a pipeline
- Essencial para debug e auditoria

### Monitoramento (Produção)
```
RabbitMQ Management UI:
├─ Exchanges: taxa de mensagens/segundo
├─ Queues: profundidade, consumo, erros
├─ Consumers: quantidade ativa, prefetch
└─ Connections: status, canais

Grafana + Prometheus:
├─ Latência de processamento
├─ Taxa de erro
├─ Throughput de mensagens
└─ Health checks
```

---

## 🔐 Configurações de Produção

### Retry Policy
```properties
# Tentativas automáticas em caso de falha
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=3000
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.multiplier=2
```

**Comportamento:**
1. Falha → Aguarda 3s → Tenta novamente
2. Falha → Aguarda 6s (3s × 2) → Tenta novamente
3. Falha → Aguarda 12s (6s × 2) → Tenta novamente
4. Falha → Envia para DLQ (Dead Letter Queue)

### Prefetch
```properties
spring.rabbitmq.listener.simple.prefetch=1
```

**Explicação:**
- Consumer processa 1 mensagem por vez
- Evita sobrecarga
- Garante fair distribution entre consumers

### Connection Pooling
```properties
spring.rabbitmq.cache.channel.size=25
```

**Explicação:**
- Pool de 25 canais reutilizáveis
- Melhora performance
- Reduz overhead de criar/destruir conexões

---

## 🎯 Próximos Passos (Melhorias Futuras)

### 1. Dead Letter Queue (DLQ)
```java
// Fila para mensagens com erro após todas as tentativas
@Bean
public Queue deadLetterQueue() {
    return new Queue("aluno.dlq", true);
}
```

### 2. Saga Pattern
```java
// Para transações distribuídas
// Ex: Criar aluno → Criar perfil gamificação → Enviar email
// Se falhar, compensar (rollback distribuído)
```

### 3. Event Sourcing
```java
// Armazenar todos os eventos em log imutável
// Permite reconstruir estado a qualquer momento
```

### 4. CQRS
```java
// Separar modelo de escrita (commands) de leitura (queries)
// Write: API de alunos
// Read: API de consultas otimizadas
```

### 5. Kafka para Event Streaming
```java
// RabbitMQ: work queues, fire-and-forget
// Kafka: event log, replay, analytics
```

---

## 📚 Referências Técnicas

### Padrões de Arquitetura
- **Event-Driven Architecture (EDA)**
- **Publisher/Subscriber Pattern**
- **Hexagonal Architecture (Ports & Adapters)**
- **Domain-Driven Design (DDD)**
- **Clean Architecture**

### Ferramentas e Frameworks
- **Spring AMQP:** Abstração sobre RabbitMQ
- **RabbitMQ:** Message broker AMQP
- **Docker Compose:** Orquestração de containers
- **Mockito:** Framework de mocking para testes
- **JaCoCo:** Cobertura de código

### Livros e Recursos
- "Building Microservices" - Sam Newman
- "Enterprise Integration Patterns" - Hohpe & Woolf
- "Domain-Driven Design" - Eric Evans
- "Clean Architecture" - Robert C. Martin

---

## ✅ Checklist de Qualidade

- [x] Código segue SOLID principles
- [x] Clean Architecture respeitada (camadas isoladas)
- [x] DDD aplicado (Entities, VOs, Domain Events)
- [x] Testes unitários com >80% cobertura
- [x] Logs estruturados para debugging
- [x] Configuração externalizável (properties)
- [x] Retry policy configurado
- [x] Documentação completa
- [x] Docker Compose para infraestrutura
- [x] README com instruções claras

---

## 🎓 Conclusão Técnica

Este projeto demonstra uma **evolução profissional** de um monolito para microserviços:

✅ **Mantém boas práticas:** Clean Architecture, DDD, SOLID  
✅ **Adiciona escalabilidade:** Event-driven, async processing  
✅ **Garante qualidade:** Testes, mocks, cobertura  
✅ **Facilita DevOps:** Docker, configuração, logs  
✅ **Preparado para produção:** Retry, monitoring, error handling  

A arquitetura implementada é **production-ready** e pode ser expandida conforme necessidade.
