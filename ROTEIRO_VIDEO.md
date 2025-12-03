# 🎬 ROTEIRO PARA VÍDEO - DEMONSTRAÇÃO AF

## 📋 Preparação Antes de Gravar

### Checklist Pré-gravação
- [ ] Limpar histórico de terminal
- [ ] Fechar abas desnecessárias do navegador
- [ ] Aumentar fonte do terminal (Ctrl + para melhor visualização)
- [ ] Aumentar fonte do VS Code/IntelliJ
- [ ] Preparar Postman ou ter Swagger aberto
- [ ] Testar áudio e vídeo
- [ ] Fazer teste rápido de todo o fluxo

### Ambiente Preparado
```powershell
# Antes de gravar, executar:
docker-compose down           # Limpar containers antigos
docker system prune -f        # Limpar recursos
mvn clean                     # Limpar build anterior
Clear-Host                    # Limpar terminal
```

---

## 🎥 ROTEIRO DETALHADO (20 minutos)

### 🎬 ABERTURA (1-2 min)

**O que falar:**
```
"Olá! Eu sou [Seu Nome], e vou apresentar a implementação do Item 5 da AF.

Nosso projeto evoluiu de um MONOLITO com Clean Architecture e DDD
para uma ARQUITETURA DE MICROSERVIÇOS orientada a eventos,
usando RabbitMQ como sistema de mensageria.

Vou demonstrar:
1. A arquitetura implementada
2. O código seguindo Clean Architecture
3. A aplicação funcionando com eventos reais
4. Os testes unitários com mocks
5. E justificar por que mocks são importantes"
```

**O que mostrar:**
- Tela inicial
- Estrutura do projeto no VS Code

---

### 📐 PARTE 1: ARQUITETURA (3-4 min)

**O que mostrar:**
```
1. Diagrama no README_MICROSERVICES.md
   - Mostrar evolução: Antes (monolito) → Depois (event-driven)
   
2. Explicar componentes:
   - Publisher (UseCases)
   - RabbitMQ (Broker)
   - Consumers (4 microserviços)
```

**O que falar:**
```
"Antes tínhamos um fluxo direto:
Controller → UseCase → Repository → Database

Agora, quando algo importante acontece no domínio,
publicamos um EVENTO que múltiplos microserviços podem consumir.

Por exemplo, quando um aluno conclui um curso:
- CertificadoService gera o certificado
- GamificacaoService atribui pontos
- EmailService envia parabéns

Tudo isso acontece de forma ASSÍNCRONA e DESACOPLADA."
```

---

### 💻 PARTE 2: CÓDIGO (5-6 min)

#### 2.1 Domain Events (1 min)
**Arquivo:** `domain/events/AlunoConcluidoEvent.java`

**O que falar:**
```
"Primeiro, temos os EVENTOS DE DOMÍNIO.
Eles representam fatos que aconteceram no sistema.

Note que são classes simples, imutáveis (Lombok @Data),
com todos os dados necessários para os consumers processarem."
```

#### 2.2 Port (Interface) (1 min)
**Arquivo:** `domain/ports/EventPublisher.java`

**O que falar:**
```
"Seguindo Clean Architecture, criamos um PORT - uma interface.

O DOMÍNIO não conhece RabbitMQ, Kafka ou MQTT.
Ele apenas sabe que pode publicar eventos através desta interface.

Isso é o princípio da INVERSÃO DE DEPENDÊNCIAS (SOLID)."
```

#### 2.3 Adapter (1 min)
**Arquivo:** `infrastructure/messaging/adapters/RabbitMQEventPublisher.java`

**O que falar:**
```
"Aqui está o ADAPTER - a implementação concreta usando RabbitMQ.

Ele implementa a interface EventPublisher e usa o RabbitTemplate
para enviar mensagens para o broker.

Se amanhã quisermos trocar para Kafka, só mudamos este adapter.
O domínio não precisa saber!"
```

#### 2.4 UseCase Publicando Evento (1 min)
**Arquivo:** `application/usecases/ConcluirCursoUseCase.java`

**O que falar:**
```
"No UseCase, após persistir o aluno no banco,
publicamos o evento AlunoConcluidoEvent.

Note que o UseCase não sabe que está usando RabbitMQ.
Ele apenas chama eventPublisher.publicarAlunoConcluido().

Isso mantém o código limpo e testável!"
```

#### 2.5 Consumer (1 min)
**Arquivo:** `infrastructure/messaging/consumers/CertificadoServiceConsumer.java`

**O que falar:**
```
"Aqui está um CONSUMER - um microserviço simulado.

Ele escuta a fila 'aluno.concluido.queue' usando @RabbitListener.
Quando um evento chega, ele processa: gera o certificado.

Em produção, cada consumer seria uma aplicação separada,
possivelmente em linguagem diferente!"
```

#### 2.6 Configuração RabbitMQ (1 min)
**Arquivo:** `infrastructure/messaging/config/RabbitMQConfig.java`

**O que falar:**
```
"Esta classe configura o RabbitMQ:
- Exchange (roteador de mensagens)
- Queues (filas)
- Bindings (ligações com routing keys)

Spring AMQP cria tudo isso automaticamente na inicialização."
```

---

### 🚀 PARTE 3: DEMONSTRAÇÃO PRÁTICA (7-8 min)

#### 3.1 Subir Infraestrutura (1 min)
```powershell
# Terminal 1
docker-compose up -d
```

**O que falar:**
```
"Primeiro, vamos subir o RabbitMQ usando Docker Compose.
Isso cria um container com RabbitMQ e seu Management UI."
```

**Mostrar:**
- Container subindo no Docker Desktop
- Ou: `docker ps`

#### 3.2 Acessar RabbitMQ UI (1 min)
```
http://localhost:15672
Login: admin / admin123
```

**O que falar:**
```
"No Management UI, podemos ver:
- Exchanges configuradas
- Queues criadas
- Bindings (ligações)

Por enquanto, as filas estão vazias.
Vamos mudar isso!"
```

**Mostrar:**
- Aba "Exchanges" → `aluno.events.exchange`
- Aba "Queues" → 3 filas criadas

#### 3.3 Executar Aplicação (1 min)
```powershell
# Terminal 2
mvn spring-boot:run
```

**O que falar:**
```
"Agora vamos iniciar a aplicação Spring Boot.
Aguardando a inicialização..."
```

**Mostrar:**
- Logs de inicialização
- Conexão com RabbitMQ estabelecida
- Aplicação rodando na porta 8080

#### 3.4 Criar Aluno (2 min)
```
Swagger: http://localhost:8080/swagger-ui.html
POST /api/alunos
{"nome": "João Silva", "ra": "RA123456"}
```

**O que falar:**
```
"Vou criar um aluno via API REST.
Observe os logs no terminal..."
```

**Mostrar logs:**
```
INFO - Aluno criado com sucesso - ID: 1
INFO - Evento AlunoCriado publicado
INFO - ╔════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Email Service      ║
INFO - ║  Enviando email de boas-vindas... ║
INFO - ╚════════════════════════════════════╝
```

**O que falar:**
```
"Vejam! O UseCase criou o aluno, publicou o evento,
e o EmailService CONSUMIU automaticamente e 'enviou o email'.

Isso aconteceu de forma ASSÍNCRONA!"
```

#### 3.5 Verificar RabbitMQ (30 seg)
**Voltar ao Management UI → Queues → aluno.criado.queue**

**O que falar:**
```
"Voltando ao RabbitMQ, a fila está vazia.
Por quê? Porque a mensagem foi CONSUMIDA!

Se houvesse mensagens aqui, significaria que
o consumer não está processando."
```

#### 3.6 Concluir Curso (2 min)
```
POST /api/alunos/1/concluir
{"mediaFinal": 8.5}
```

**O que falar:**
```
"Agora vou concluir o curso deste aluno com média 8.5.
Vejam os logs..."
```

**Mostrar logs (3 consumers!):**
```
INFO - Curso concluído com sucesso
INFO - Evento AlunoConcluido publicado

INFO - ╔════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Certificado        ║
INFO - ║  Gerando certificado PDF...       ║
INFO - ╚════════════════════════════════════╝

INFO - ╔════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Gamificação        ║
INFO - ║  Pontos ganhos: 800               ║
INFO - ║  Badge: Alto Desempenho           ║
INFO - ╚════════════════════════════════════╝
```

**O que falar:**
```
"Incrível! UM único evento acionou DOIS microserviços:
- Certificado gerou o PDF
- Gamificação atribuiu 800 pontos

Isso é o poder da arquitetura orientada a eventos!
Escalável, desacoplada, assíncrona."
```

---

### 🧪 PARTE 4: TESTES COM MOCKS (3-4 min)

#### 4.1 Mostrar Teste (2 min)
**Arquivo:** `test/.../ConcluirCursoUseCaseTestComMensageria.java`

**O que falar:**
```
"Agora vou mostrar os TESTES UNITÁRIOS.

Veja que eu MOCKO o EventPublisher.
Por quê? Porque no teste unitário, quero testar
APENAS a lógica do UseCase, não o RabbitMQ.

Com ArgumentCaptor, eu verifico:
1. Que o evento FOI publicado
2. Que os DADOS estão corretos
3. Que o fluxo está funcionando

Tudo isso SEM precisar do RabbitMQ rodando!"
```

**Mostrar código:**
```java
@Mock
private EventPublisher eventPublisher;

verify(eventPublisher).publicarAlunoConcluido(captor.capture());
assertThat(evento.getMediaFinal()).isEqualTo(8.5);
```

#### 4.2 Executar Testes (1 min)
```powershell
mvn test
```

**O que falar:**
```
"Vou executar os testes agora.
Reparem que são RÁPIDOS - executam em segundos.

Se eu usasse RabbitMQ real, levariam minutos
e poderiam falhar por problemas de conexão."
```

**Mostrar:**
- Testes passando
- Tempo de execução
- Relatório JaCoCo (opcional)

#### 4.3 Justificar Mocks (1 min)
**Mostrar slide ou documento ARQUITETURA_TECNICA.md**

**O que falar:**
```
"Por que mocks são importantes? 5 razões:

1. ISOLAMENTO: Testa só a lógica, não infraestrutura
2. VELOCIDADE: 10ms vs 5 segundos por teste
3. CONFIABILIDADE: Não falha por timeout ou conexão
4. FLEXIBILIDADE: Simula cenários difíceis (falhas)
5. CI/CD: Roda em qualquer ambiente sem containers

Mocks testam LÓGICA DE NEGÓCIO.
Testes de integração testam INFRAESTRUTURA.
Ambos são importantes!"
```

---

### 🎓 CONCLUSÃO (2-3 min)

**O que falar:**
```
"Recapitulando o que implementamos:

✅ Evolução de MONOLITO para MICROSERVIÇOS
✅ ARQUITETURA ORIENTADA A EVENTOS com RabbitMQ
✅ CLEAN ARCHITECTURE mantida (ports/adapters)
✅ DDD preservado (eventos de domínio)
✅ 4 MICROSERVIÇOS simulados
✅ TESTES UNITÁRIOS com mocks justificados
✅ DEVOPS com Docker Compose
✅ QUALIDADE com JaCoCo e cobertura

Esta arquitetura é:
- ESCALÁVEL: Pode adicionar mais consumers
- RESILIENTE: Se um consumer cai, outros continuam
- FLEXÍVEL: Fácil trocar tecnologia (Kafka, MQTT)
- TESTÁVEL: Mocks para testes rápidos
- PRODUCTION-READY: Retry, logs, monitoramento

A evolução foi feita mantendo as boas práticas de
Clean Architecture e DDD que aprendemos na AC2.

Obrigado pela atenção!
Estou à disposição para perguntas."
```

---

## 🎬 DICAS DE GRAVAÇÃO

### 🎙️ Áudio
- Grave em ambiente silencioso
- Use microfone decente (headset já ajuda)
- Fale pausadamente e com clareza
- Evite "éééé", "hummm"

### 🖥️ Vídeo
- Resolução mínima: 1080p
- Grave apenas a tela (não precisa aparecer)
- Ou use câmera + tela (picture-in-picture)
- Zoom nas partes importantes do código

### ⏱️ Timing
- Não corra: explique bem
- Mas não seja repetitivo
- Pausas estratégicas entre seções
- 20 minutos é ideal, até 25 ok

### 📝 Preparação
- **PRATIQUE antes!** Grave teste
- Tenha um "roteiro mental" claro
- Prepare ambiente antes de gravar
- Teste todo o fluxo funciona

### ✂️ Edição
- Corte erros e pausas longas
- Adicione legendas (opcional mas legal)
- Adicione marcadores de tempo na descrição
- Ex: "0:00 Introdução, 2:00 Arquitetura..."

---

## 📊 CHECKLIST DE GRAVAÇÃO

### Antes de Apertar "Rec"
- [ ] Docker Compose funcionando
- [ ] Aplicação compila sem erros
- [ ] Testes passando
- [ ] Swagger acessível
- [ ] RabbitMQ Management acessível
- [ ] Fonte aumentada (terminal + IDE)
- [ ] Abas desnecessárias fechadas
- [ ] Áudio testado
- [ ] Roteiro revisado

### Durante a Gravação
- [ ] Falar pausadamente
- [ ] Mostrar código relevante
- [ ] Demonstrar execução real
- [ ] Mostrar logs
- [ ] Explicar conceitos
- [ ] Justificar decisões

### Após Gravação
- [ ] Revisar vídeo completo
- [ ] Verificar áudio claro
- [ ] Verificar tela legível
- [ ] Adicionar introdução/conclusão
- [ ] Exportar em boa qualidade
- [ ] Upload para YouTube/Drive

---

## 💡 FRASES PRONTAS (Use quando apropriado)

### Sobre Arquitetura
- "Isso demonstra o princípio de separação de responsabilidades"
- "Este é um exemplo clássico de Inversão de Dependências"
- "Clean Architecture mantém o domínio isolado de frameworks"

### Sobre Eventos
- "Eventos representam fatos que aconteceram no passado"
- "A comunicação assíncrona melhora a resiliência do sistema"
- "Múltiplos serviços podem reagir ao mesmo evento"

### Sobre Testes
- "Mocks garantem que testamos apenas uma unidade de código"
- "Testes rápidos permitem TDD e feedback imediato"
- "Em produção, teríamos também testes de integração"

### Sobre DevOps
- "Docker garante que todos rodamos o mesmo ambiente"
- "Esta configuração está pronta para CI/CD"
- "Logs estruturados facilitam debugging em produção"

---

## 🎯 OBJETIVO FINAL

Ao final do vídeo, os professores devem entender:

✅ Que você **evoluiu** o monolito para microserviços  
✅ Que **manteve** Clean Architecture e DDD  
✅ Que a implementação é **profissional** e **production-ready**  
✅ Que você **entende** os conceitos (não só copiou código)  
✅ Que sabe **justificar** decisões técnicas (mocks, arquitetura)  

**Você consegue! Boa sorte! 🚀**
