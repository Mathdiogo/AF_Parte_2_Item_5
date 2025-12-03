# 🧪 Guia de Testes de API - Microserviços

Este guia mostra como testar a aplicação usando **Swagger UI**, **cURL** ou **Postman**.

---

## 📋 Pré-requisitos

1. ✅ RabbitMQ rodando: `docker-compose up -d`
2. ✅ Aplicação rodando: `mvn spring-boot:run`
3. ✅ Acesse Swagger: http://localhost:8080/swagger-ui.html

---

## 🎯 Cenários de Teste

### 📌 Cenário 1: Criar um Aluno

**Ação:** Cria um novo aluno e dispara evento `AlunoCriado`

**Consumidores ativados:**
- ✉️ EmailService (envia boas-vindas)

#### Via Swagger UI
1. Acesse: http://localhost:8080/swagger-ui.html
2. Encontre endpoint: `POST /api/alunos`
3. Clique em "Try it out"
4. Cole o JSON:
```json
{
  "nome": "João Silva",
  "ra": "RA123456"
}
```
5. Clique em "Execute"

#### Via cURL (PowerShell)
```powershell
curl -X POST "http://localhost:8080/api/alunos" `
  -H "Content-Type: application/json" `
  -d '{\"nome\": \"João Silva\", \"ra\": \"RA123456\"}'
```

#### Response Esperado
```json
{
  "id": 1,
  "nome": "João Silva",
  "ra": "RA123456",
  "mediaFinal": 0.0,
  "concluiu": false,
  "cursosAdicionais": 0,
  "tentativasAvaliacao": 0,
  "dataCriacao": "2024-12-03T10:30:00"
}
```

#### Logs Esperados (Console da Aplicação)
```
INFO - Iniciando criação de aluno - Nome: João Silva, RA: RA123456
INFO - Aluno criado com sucesso - ID: 1
INFO - Evento AlunoCriado publicado - AlunoID: 1, EventID: abc-123
INFO - ╔════════════════════════════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Email Service                              ║
INFO - ║  Evento recebido: AlunoCriado                             ║
INFO - ║  AÇÃO: Enviando email de boas-vindas...                   ║
INFO - ║  ✓ Email de boas-vindas enviado com sucesso!              ║
INFO - ╚════════════════════════════════════════════════════════════╝
```

#### Verificar no RabbitMQ
1. Acesse: http://localhost:15672
2. Login: `admin` / `admin123`
3. Vá em **Queues** → `aluno.criado.queue`
4. Clique em **Get messages** (deve estar vazia, pois foi consumida)

---

### 📌 Cenário 2: Concluir um Curso (Aprovado)

**Ação:** Conclui curso com média >= 6.0 (aprovado)

**Consumidores ativados:**
- 📜 CertificadoService (gera certificado)
- 🎮 GamificacaoService (atribui pontos e badges)

#### Via Swagger UI
1. Endpoint: `POST /api/alunos/{id}/concluir`
2. `id`: 1 (o aluno criado anteriormente)
3. Body:
```json
{
  "mediaFinal": 8.5
}
```
4. Execute

#### Via cURL (PowerShell)
```powershell
curl -X POST "http://localhost:8080/api/alunos/1/concluir" `
  -H "Content-Type: application/json" `
  -d '{\"mediaFinal\": 8.5}'
```

#### Response Esperado
```json
{
  "id": 1,
  "nome": "João Silva",
  "ra": "RA123456",
  "mediaFinal": 8.5,
  "concluiu": true,
  "cursosAdicionais": 0,
  "tentativasAvaliacao": 0,
  "dataConclusao": "2024-12-03T11:00:00"
}
```

#### Logs Esperados (3 Microserviços)
```
INFO - Curso concluído com sucesso - AlunoID: 1, Aprovado: true
INFO - Evento AlunoConcluido publicado

INFO - ╔════════════════════════════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Certificado Service                        ║
INFO - ║  Ação: Gerando certificado de conclusão...                ║
INFO - ║  ✓ Certificado gerado com sucesso!                        ║
INFO - ╚════════════════════════════════════════════════════════════╝

INFO - ╔════════════════════════════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Gamificação Service                        ║
INFO - ║  🎮 Pontos ganhos: 800                                    ║
INFO - ║  🏆 Badge conquistado: Alto Desempenho                    ║
INFO - ╚════════════════════════════════════════════════════════════╝
```

---

### 📌 Cenário 3: Concluir um Curso (Reprovado)

**Ação:** Conclui curso com média < 6.0 (reprovado)

**Consumidores ativados:**
- 🎮 GamificacaoService (registra conclusão, mas sem badge premium)
- 📜 CertificadoService (não gera certificado)

#### Via Swagger UI
1. Crie outro aluno primeiro (RA diferente, ex: RA999999)
2. Endpoint: `POST /api/alunos/{id}/concluir`
3. Body:
```json
{
  "mediaFinal": 4.0
}
```

#### Logs Esperados
```
INFO - Curso concluído com sucesso - AlunoID: 2, Aprovado: false
INFO - ⚠ Aluno reprovado - Certificado não será gerado
INFO - 🎮 Pontos ganhos: 200
INFO - ✓ Concluído (sem badge especial)
```

---

### 📌 Cenário 4: Registrar Tentativa

**Ação:** Registra tentativa de avaliação

**Consumidores ativados:**
- 📊 AnalyticsService (registra métricas)

#### Via Swagger UI
1. Endpoint: `POST /api/alunos/{id}/tentativas`
2. Body:
```json
{
  "nota": 7.0
}
```

#### Via cURL (PowerShell)
```powershell
curl -X POST "http://localhost:8080/api/alunos/1/tentativas" `
  -H "Content-Type: application/json" `
  -d '{\"nota\": 7.0}'
```

#### Response Esperado
```json
{
  "id": 1,
  "nome": "João Silva",
  "ra": "RA123456",
  "mediaFinal": 7.0,
  "tentativasAvaliacao": 1,
  ...
}
```

#### Logs Esperados
```
INFO - Tentativa registrada - AlunoID: 1, Total tentativas: 1
INFO - ╔════════════════════════════════════════════════════════════╗
INFO - ║  MICROSERVIÇO: Analytics Service                          ║
INFO - ║  📊 Registrando métricas de analytics...                  ║
INFO - ╚════════════════════════════════════════════════════════════╝
```

#### Testando Limite de Tentativas
- Registre 3 tentativas seguidas
- Na 3ª tentativa, verá alerta nos logs:
```
WARN - ⚠️ ALERTA: Aluno 1 atingiu o limite de tentativas!
```

---

### 📌 Cenário 5: Listar Todos os Alunos

**Ação:** Lista todos os alunos cadastrados

#### Via Swagger UI
1. Endpoint: `GET /api/alunos`
2. Execute

#### Via cURL (PowerShell)
```powershell
curl -X GET "http://localhost:8080/api/alunos"
```

#### Response Esperado
```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "ra": "RA123456",
    "mediaFinal": 8.5,
    "concluiu": true,
    ...
  },
  {
    "id": 2,
    "nome": "Maria Santos",
    "ra": "RA999999",
    "mediaFinal": 4.0,
    "concluiu": false,
    ...
  }
]
```

---

### 📌 Cenário 6: Buscar Aluno por ID

**Ação:** Busca aluno específico

#### Via Swagger UI
1. Endpoint: `GET /api/alunos/{id}`
2. `id`: 1
3. Execute

#### Via cURL (PowerShell)
```powershell
curl -X GET "http://localhost:8080/api/alunos/1"
```

---

### 📌 Cenário 7: Obter Ranking de Alunos

**Ação:** Lista alunos ordenados por média

#### Via Swagger UI
1. Endpoint: `GET /api/alunos/ranking`
2. Execute

#### Response Esperado
```json
[
  {
    "posicao": 1,
    "nome": "João Silva",
    "ra": "RA123456",
    "mediaFinal": 8.5
  },
  {
    "posicao": 2,
    "nome": "Maria Santos",
    "ra": "RA999999",
    "mediaFinal": 4.0
  }
]
```

---

## 🔍 Verificando Mensagens no RabbitMQ

### Passo a Passo

1. **Acesse Management UI:** http://localhost:15672
2. **Login:** `admin` / `admin123`
3. **Vá em "Queues"**

### Filas Disponíveis
```
aluno.criado.queue     → Eventos de criação
aluno.concluido.queue  → Eventos de conclusão
aluno.tentativa.queue  → Eventos de tentativas
```

### Verificar Mensagens
1. Clique na fila desejada
2. Vá em **"Get messages"**
3. Clique em **"Get Message(s)"**
4. Se estiver vazia = mensagem foi consumida ✅
5. Se houver mensagens = consumers não processaram ainda

### Verificar Exchange
1. Vá em **"Exchanges"**
2. Encontre: `aluno.events.exchange`
3. Veja bindings (ligações com queues)

---

## 🎬 Fluxo Completo de Teste

### Roteiro Sugerido para Vídeo/Apresentação

```
1. Mostrar RabbitMQ Management UI (filas vazias)
   
2. Criar Aluno via Swagger
   → Mostrar logs do EmailService no console
   
3. Verificar fila no RabbitMQ (vazia = consumida)
   
4. Registrar 2 tentativas
   → Mostrar logs do AnalyticsService
   
5. Concluir curso com média 9.0
   → Mostrar logs de 2 consumers (Certificado + Gamificação)
   
6. Listar alunos e ver ranking
   
7. Criar mais alunos e mostrar escalabilidade
   
8. Mostrar código:
   - Evento de domínio
   - Publisher (UseCase)
   - Consumer (Microserviço)
   - Testes com mocks
```

---

## 🐛 Troubleshooting

### Erro: "Connection refused"
```powershell
# Verificar se RabbitMQ está rodando
docker ps

# Se não estiver, subir novamente
docker-compose up -d
```

### Erro: "Aluno não encontrado"
- Verifique se você está usando o ID correto
- Liste todos os alunos: `GET /api/alunos`

### Consumers não processam eventos
- Verifique logs da aplicação
- Verifique se RabbitMQ está conectado
- Verifique filas no Management UI

### Mensagens ficam presas na fila
- Verifique se consumers estão rodando
- Veja erros nos logs
- Mensagens com erro vão para DLQ (se configurado)

---

## 📊 Dados de Teste Sugeridos

### Alunos para Criar
```json
{"nome": "João Silva", "ra": "RA001"}
{"nome": "Maria Santos", "ra": "RA002"}
{"nome": "Pedro Costa", "ra": "RA003"}
{"nome": "Ana Oliveira", "ra": "RA004"}
{"nome": "Carlos Mendes", "ra": "RA005"}
```

### Médias para Testar
- **9.5** → Badge "Excelência Máxima", 1000 pontos
- **8.5** → Badge "Alto Desempenho", 800 pontos
- **7.0** → Badge "Bom Desempenho", 600 pontos
- **6.0** → Badge "Aprovado", 400 pontos
- **4.0** → Reprovado, 200 pontos

---

## ✅ Checklist de Demonstração

- [ ] RabbitMQ rodando e acessível
- [ ] Aplicação rodando sem erros
- [ ] Swagger acessível
- [ ] Console com logs visíveis
- [ ] Criar aluno e ver logs do EmailService
- [ ] Concluir curso e ver 2 consumers (Certificado + Gamificação)
- [ ] Registrar tentativa e ver Analytics
- [ ] Mostrar filas no RabbitMQ
- [ ] Mostrar código (eventos, publisher, consumer)
- [ ] Mostrar testes com mocks

---

## 🎓 Conclusão

Este guia permite demonstrar **completamente** a arquitetura de microserviços implementada, mostrando:

✅ **Event-Driven Architecture** em ação  
✅ **Publisher/Consumer Pattern** funcionando  
✅ **Clean Architecture** mantida  
✅ **Múltiplos microserviços** processando eventos  
✅ **Mensageria assíncrona** com RabbitMQ  
✅ **Logs claros** para demonstração  

**Boa sorte na apresentação!** 🚀
