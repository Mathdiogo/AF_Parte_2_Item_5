# 🎓 Sistema de Gestão de Alunos - Projeto DevOps AC2

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![JaCoCo](https://img.shields.io/badge/Coverage-JaCoCo-red.svg)](https://www.jacoco.org/)

## 📋 Sobre o Projeto

Sistema de gestão acadêmica desenvolvido aplicando **TDD**, **DDD** e **Clean Architecture**, com foco em qualidade de código, cobertura de testes e boas práticas de engenharia de software.

### 🎯 Objetivos

- ✅ **TDD (Test-Driven Development)**: Testes escritos antes da implementação
- ✅ **DDD (Domain-Driven Design)**: Modelagem rica do domínio
- ✅ **Clean Architecture**: Separação de responsabilidades em camadas
- ✅ **Cobertura de Código**: Análise linha a linha com JaCoCo
- ✅ **CI/CD**: Integração com Jenkins para análise automática

---

## 🏗️ Arquitetura

```
src/main/java/com/devops/projeto_ac2/
│
├── domain/                          # Camada de Domínio (DDD)
│   ├── entities/                    # Entidades ricas com comportamento
│   │   └── Aluno.java
│   ├── valueobjects/                # Value Objects imutáveis
│   │   ├── RegistroAcademico.java
│   │   ├── NomeAluno.java
│   │   ├── MediaFinal.java
│   │   └── NumeroTentativas.java
│   ├── repositories/                # Interfaces (Ports)
│   │   └── AlunoRepository.java
│   └── exceptions/                  # Exceções de domínio
│       ├── DomainException.java
│       ├── AlunoNotFoundException.java
│       ├── InvalidValueObjectException.java
│       └── TentativasEsgotadasException.java
│
├── application/                     # Camada de Aplicação (Use Cases)
│   └── usecases/
│       ├── CriarAlunoUseCase.java
│       ├── ConcluirCursoUseCase.java
│       ├── RegistrarTentativaUseCase.java
│       ├── BuscarAlunoPorIdUseCase.java
│       ├── ListarAlunosUseCase.java
│       └── ObterRankingAlunosUseCase.java
│
├── infrastructure/                  # Camada de Infraestrutura
│   ├── persistence/                 # Implementação JPA
│   │   ├── AlunoJpaRepository.java
│   │   └── AlunoRepositoryImpl.java
│   └── web/                         # Controllers REST
│       ├── controller/
│       │   └── AlunoController.java
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           └── ErrorResponse.java
│
└── shared/                          # Camada Compartilhada
    ├── dto/                         # Data Transfer Objects
    │   ├── AlunoResponseDTO.java
    │   ├── CriarAlunoRequestDTO.java
    │   ├── ConcluirCursoRequestDTO.java
    │   ├── RegistrarTentativaRequestDTO.java
    │   └── RankingResponseDTO.java
    └── mapper/
        └── AlunoMapper.java
```

---

## 🚀 Funcionalidades

### 1️⃣ Gestão de Alunos

#### **Criar Aluno**
```http
POST /api/alunos
Content-Type: application/json

{
  "nome": "João Silva",
  "ra": "12345ABC"
}
```

#### **Buscar Aluno por ID**
```http
GET /api/alunos/{id}
```

#### **Listar Todos os Alunos**
```http
GET /api/alunos
GET /api/alunos?concluido=true
GET /api/alunos?concluido=false
```

---

### 2️⃣ Sistema de Tentativas 

Alunos têm até **3 tentativas** para realizar avaliações.

#### **Registrar Tentativa de Avaliação**
```http
POST /api/alunos/{id}/tentativas
Content-Type: application/json

{
  "nota": 7.5
}
```

**Regras de Negócio:**
- ✅ Máximo de 3 tentativas por aluno
- ✅ Cada tentativa atualiza a média
- ✅ Validação automática de limites
- ⚠️ Exceção `TentativasEsgotadasException` se exceder

---

### 3️⃣ Conclusão de Curso

#### **Concluir Curso**
```http
PATCH /api/alunos/{id}/concluir
Content-Type: application/json

{
  "mediaFinal": 8.5
}
```

**Regras de Negócio:**
- ✅ **Média >= 9.0**: Ganha **5 cursos extras** (bônus excelência)
- ✅ **Média >= 7.0**: Ganha **3 cursos extras**
- ✅ Aluno deve ter pelo menos **1 tentativa** registrada
- ⚠️ Não pode concluir duas vezes

---

### 4️⃣ Sistema de Ranking 

#### **Obter Ranking Completo**
```http
GET /api/alunos/ranking
```

#### **Top N Alunos**
```http
GET /api/alunos/ranking?top=10
```

#### **Ranking Apenas Aprovados**
```http
GET /api/alunos/ranking?apenasAprovados=true
```

**Critérios de Ordenação:**
1. Média final (decrescente)
2. Cursos adicionais (decrescente)

**Resposta:**
```json
[
  {
    "posicao": 1,
    "alunoId": 2,
    "nome": "Maria Santos",
    "ra": "67890",
    "mediaFinal": 9.5,
    "cursosAdicionais": 5,
    "concluiu": true,
    "situacao": "APROVADO"
  },
  {
    "posicao": 2,
    "alunoId": 1,
    "nome": "João Silva",
    "ra": "12345ABC",
    "mediaFinal": 8.5,
    "cursosAdicionais": 3,
    "concluiu": true,
    "situacao": "APROVADO"
  }
]
```

---

## 🎯 Regras de Negócio Implementadas

| Regra | Descrição | Status |
|-------|-----------|--------|
| **RN01** | Aluno deve ter nome entre 3 e 100 caracteres | ✅ |
| **RN02** | RA deve ter entre 5 e 20 caracteres alfanuméricos | ✅ |
| **RN03** | Média final entre 0.0 e 10.0 | ✅ |
| **RN04** | Aprovado: média >= 7.0 | ✅ |
| **RN05** | Reprovado: média < 5.0 | ✅ |
| **RN06** | Em recuperação: 5.0 <= média < 7.0 | ✅ |
| **RN07** | Bônus de 3 cursos para média >= 7.0 | ✅ |
| **RN08** | Bônus de 5 cursos para média >= 9.0 | ✅ |
| **RN09** | Máximo de 3 tentativas por aluno | ✅ |
| **RN10** | Não pode concluir sem tentativas | ✅ |
| **RN11** | RA único no sistema | ✅ |

---

## 🧪 Testes

### Estrutura de Testes

```
src/test/java/com/devops/projeto_ac2/
│
├── domain/
│   ├── entities/
│   │   └── AlunoTest.java                    # Testes da entidade
│   └── valueobjects/
│       ├── RegistroAcademicoTest.java        # Testes de VO
│       ├── NomeAlunoTest.java
│       ├── MediaFinalTest.java
│       └── NumeroTentativasTest.java
│
├── application/usecases/
│   ├── CriarAlunoUseCaseTest.java            # Testes com Mockito
│   ├── ConcluirCursoUseCaseTest.java
│   ├── RegistrarTentativaUseCaseTest.java
│   └── ObterRankingAlunosUseCaseTest.java
│
└── infrastructure/web/controller/
    └── AlunoControllerIntegrationTest.java   # Testes com MockMvc
```

### Tipos de Testes

1. **Testes Unitários** (Value Objects, Entities, Use Cases)
   - Isolamento total com Mockito
   - Cobertura de regras de negócio
   - Casos de sucesso e falha

2. **Testes de Integração** (Controllers)
   - Contexto Spring completo
   - Banco H2 em memória
   - Validação de endpoints REST

### Executar Testes

```bash
# Rodar todos os testes
./mvnw clean test

# Rodar testes com relatório JaCoCo
./mvnw clean verify

# Ver relatório de cobertura
open target/site/jacoco/index.html
```

### Cobertura Esperada

- **Mínimo configurado**: 70% de cobertura de linhas
- **Meta do projeto**: > 85% de cobertura
- **Análise**: JaCoCo gera relatório linha a linha

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.7 | Framework base |
| Spring Data JPA | 3.5.5 | Persistência |
| H2 Database | 2.3.232 | Banco em memória |
| Lombok | 1.18.30 | Redução de boilerplate |
| JaCoCo | 0.8.11 | Cobertura de código |
| JUnit 5 | 5.12.2 | Framework de testes |
| Mockito | 5.17.0 | Mocks para testes |
| AssertJ | 3.27.6 | Assertions fluentes |
| Maven | 3.9+ | Gerenciamento de build |

---

## 📦 Como Executar

### Pré-requisitos

- ☕ Java 21 (JDK)
- 📦 Maven 3.9+
- 🐳 Docker 

### Executar Aplicação

```bash
# 1. Clonar repositório
git clone https://github.com/seu-usuario/pratica_4.git
cd pratica_4

# 2. Compilar e testar
./mvnw clean install

# 3. Executar aplicação
./mvnw spring-boot:run

# 4. Acessar
# API: http://localhost:8080/api/alunos
# H2 Console: http://localhost:8080/h2-console
```

### Configurações de Profile

```bash
# Desenvolvimento (application-dev.properties)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Testes (application-test.properties)
./mvnw test -Ptest
```

---

## 📊 Análise de Qualidade

### JaCoCo - Cobertura de Código

```bash
# Gerar relatório
./mvnw clean verify

# Relatório em: target/site/jacoco/index.html
```

### Métricas do Projeto

- **Complexidade Ciclomática**: Mantida baixa com métodos pequenos
- **Acoplamento**: Reduzido com injeção de dependências
- **Coesão**: Alta através de SRP (Single Responsibility Principle)
- **Testabilidade**: 100% testável com Mockito

---

## 🔍 Padrões e Boas Práticas

### Design Patterns Utilizados

1. **Factory Method**: Criação de Value Objects e Entities
2. **Repository Pattern**: Abstração de persistência
3. **Strategy Pattern**: Diferentes estratégias de ranking
4. **Builder Pattern**: DTOs e respostas
5. **Singleton**: Services gerenciados pelo Spring

### Princípios SOLID

- ✅ **S**ingle Responsibility Principle
- ✅ **O**pen/Closed Principle
- ✅ **L**iskov Substitution Principle
- ✅ **I**nterface Segregation Principle
- ✅ **D**ependency Inversion Principle

---

## 📝 Endpoints Resumidos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/alunos` | Criar novo aluno |
| `GET` | `/api/alunos` | Listar todos |
| `GET` | `/api/alunos/{id}` | Buscar por ID |
| `POST` | `/api/alunos/{id}/tentativas` | Registrar tentativa ⭐ |
| `PATCH` | `/api/alunos/{id}/concluir` | Concluir curso |
| `GET` | `/api/alunos/ranking` | Obter ranking ⭐ |

---

## 🤝 Contribuindo

Este projeto é parte de uma atividade acadêmica. Sugestões e melhorias são bem-vindas!

---

## 👨‍💻 Autor

Matheus Diogo - 190435
Gustavo Valadares Fukui - 234719
---

