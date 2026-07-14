# 🏦 ByteBank Digital

![Backend CI](https://github.com/alexendrios/bytebank-digital/actions/workflows/backend-ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![Angular](https://img.shields.io/badge/Angular-20-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

> Sistema Bancário Digital Full Stack desenvolvido para demonstrar a aplicação prática de **Design Patterns (GoF)**, **Spring Boot 3**, **Angular 20**, arquitetura em camadas, autenticação JWT e alta cobertura de testes.

---

## 📑 Sumário

- [🏦 ByteBank Digital](#-bytebank-digital)
  - [📑 Sumário](#-sumário)
  - [🎯 Objetivo do Projeto](#-objetivo-do-projeto)
  - [🏗️ Arquitetura Geral](#️-arquitetura-geral)
  - [📂 Estrutura de Pastas](#-estrutura-de-pastas)
  - [☕ Backend](#-backend)
    - [Tecnologias e Bibliotecas](#tecnologias-e-bibliotecas)
    - [Organização de Pacotes](#organização-de-pacotes)
  - [📐 Modelo de Domínio (Entidades)](#-modelo-de-domínio-entidades)
  - [🧩 Design Patterns (GoF) Aplicados](#-design-patterns-gof-aplicados)
  - [🔌 API REST — Endpoints](#-api-rest--endpoints)
    - [Autenticação](#autenticação)
    - [Usuários (ADMIN)](#usuários-admin)
    - [Contas](#contas)
    - [Operações Bancárias](#operações-bancárias)
  - [🛡️ Segurança \& Banco de Dados](#️-segurança--banco-de-dados)
  - [🧪 Estratégia de Testes](#-estratégia-de-testes)
  - [💻 Frontend](#-frontend)
    - [Tecnologias e Conceitos](#tecnologias-e-conceitos)
    - [Estrutura do SPA](#estrutura-do-spa)
    - [Fluxos Visuais (Telas)](#fluxos-visuais-telas)
  - [🐳 Docker \& Orquestração](#-docker--orquestração)
  - [🔄 Pipeline CI/CD (GitHub Actions)](#-pipeline-cicd-github-actions)
  - [🚀 Módulos de Negócio Avançados](#-módulos-de-negócio-avançados)
    - [PIX](#pix)
    - [Cartão de Crédito e Cartão Virtual](#cartão-de-crédito-e-cartão-virtual)
  - [🏛️ Infraestrutura Avançada](#️-infraestrutura-avançada)
    - [Notificações em Tempo Real](#notificações-em-tempo-real)
    - [Processamento Assíncrono Orientado a Eventos](#processamento-assíncrono-orientado-a-eventos)
    - [Camada de Cache Distribuído](#camada-de-cache-distribuído)
  - [📊 Monitoramento e Observabilidade](#-monitoramento-e-observabilidade)
  - [▶️ Como Executar Localmente](#️-como-executar-localmente)
  - [📄 Licença](#-licença)

---

## 🎯 Objetivo do Projeto

O **ByteBank Digital** é uma plataforma bancária corporativa de estudo composta por uma API REST robusta e uma aplicação web responsiva. O projeto foi desenhado sob os pilares do **SOLID**, **Clean Architecture** e boas práticas de desenvolvimento de software de grande porte, servindo como portfólio para as seguintes tecnologias:

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 21 · Spring Boot 3 · Spring Security (JWT) · PostgreSQL |
| **Frontend** | Angular 20 · Angular Material · Signals |
| **DevOps & Infra** | Docker · Docker Compose · GitHub Actions (CI/CD) · Flyway |

---

## 🏗️ Arquitetura Geral

O fluxo de dados segue o modelo clássico de arquitetura em camadas, garantindo baixo acoplamento e separação de responsabilidades:

```text
┌───────────────────────────────────────────────────────────┐
│                        Angular 20 (SPA)                    │
│   Components → Pages → Services → Interceptors → Guards    │
└───────────────────────────────┬────────────────────────────┘
                                 │
                          HTTP REST (JSON)
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────┐
│                       Spring Boot API                      │
│                                                             │
│   Controller → Service → Factory → Strategy                │
│                     │                                       │
│                     ▼                                       │
│                Repository (Spring Data JPA)                 │
│                     │                                       │
│                     ▼                                       │
│                     PostgreSQL                              │
└───────────────────────────────────────────────────────────┘
```

> 💡 A seta `Service → Factory → Strategy` reflete o uso combinado dos padrões **Factory Method** e **Strategy** para resolver dinamicamente a regra de negócio correta em cada operação bancária (depósito, saque, transferência).

---

## 📂 Estrutura de Pastas

```text
bytebank-digital/
├── backend/                  # Código-fonte da API Spring Boot
├── frontend/                 # Código-fonte do SPA Angular 20
├── docker-compose.yml        # Orquestração local (App, Banco, Nginx)
├── README.md                 # Documentação principal
└── docs/                     # Documentação arquitetural detalhada
    ├── 01-introducao.md
    ├── 02-visao-geral.md
    ├── 03-requisitos.md
    ├── 04-arquitetura.md
    ├── 05-modelo-dominio.md
    ├── 06-backend.md
    ├── 07-frontend.md
    ├── 08-banco-dados.md
    ├── 09-seguranca.md
    ├── 10-api.md
    ├── 11-design-patterns.md
    ├── 12-testes.md
    ├── 13-devops.md
    ├── 14-casos-de-uso.md
    └── 15-diagramas.md
```

---

## ☕ Backend

### Tecnologias e Bibliotecas

| Categoria | Stack |
|---|---|
| **Core** | Java 21 / Spring Boot 3 |
| **Segurança** | Spring Security / JWT (JSON Web Token) |
| **Dados** | Spring Data JPA / PostgreSQL / Flyway (Migrations) |
| **Produtividade & Validação** | Lombok / MapStruct / Bean Validation |
| **Documentação** | OpenAPI 3 / Swagger UI (`/api/swagger-ui.html`) |
| **Testes** | JUnit 5 / Mockito / Testcontainers |

### Organização de Pacotes

```text
backend/src/main/java/com/bytebank/
├── config/         # Configurações globais (CORS, Swagger, Beans)
├── controller/     # Endpoints da API REST
├── dto/            # Data Transfer Objects (Request/Response)
├── entity/         # Entidades mapeadas pelo JPA
├── exception/      # Handler global e exceções de negócio
├── factory/        # Fábricas para seleção de estratégias
├── mapper/         # Interfaces do MapStruct (Entity ↔ DTO)
├── repository/     # Interfaces de persistência de dados
├── security/       # Filtros JWT, PasswordEncoder e UserDetails
├── service/        # Regras de negócio e casos de uso
├── strategy/       # Implementações específicas de transações
├── util/           # Classes utilitárias helper
└── validation/     # Validadores customizados de regras de negócio
```

---

## 📐 Modelo de Domínio (Entidades)

| Entidade | Atributos principais |
|---|---|
| **Usuário** | `id`, `nome`, `email`, `senha`, `perfil` (ADMIN/CLIENTE), `dataCadastro` |
| **Conta** | `id`, `numero`, `agencia`, `saldo`, `usuario` (dono da conta) |
| **Transferência** | `id`, `origem` (Conta), `destino` (Conta), `valor`, `data` |
| **Movimentação** | `id`, `tipo` (DEPOSITO/SAQUE/PIX), `valor`, `saldoAnterior`, `saldoAtual`, `data` |

---

## 🧩 Design Patterns (GoF) Aplicados

| Padrão | Aplicação no projeto |
|---|---|
| **Singleton** | Escopo padrão gerenciado pelo Spring IoC para `Service`, `Repository` e `Component` |
| **Strategy** | Encapsula as regras de transações bancárias em `DepositoStrategy`, `SaqueStrategy` e `TransferenciaStrategy` |
| **Factory Method** | `OperacaoFactory` resolve dinamicamente a estratégia correta com base no tipo de movimentação recebida |
| **Facade** | `BancoFacade` centraliza interações complexas entre múltiplos serviços (validar conta, processar taxa e registrar histórico em uma única chamada) |
| **Builder & Mapper** | Criação fluida de objetos de transferência e conversão automatizada de Entity ↔ DTO via MapStruct |

---

## 🔌 API REST — Endpoints

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Autentica usuário e retorna tokens |
| `POST` | `/auth/register` | Cria novo acesso no sistema |
| `POST` | `/auth/refresh-token` | Renova o Access Token usando o Refresh Token |

### Usuários (ADMIN)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/usuarios` | Lista todos os usuários |
| `GET` | `/usuarios/{id}` | Detalha um usuário |
| `POST` | `/usuarios` | Cria usuário |
| `PUT` | `/usuarios/{id}` | Atualiza usuário |
| `DELETE` | `/usuarios/{id}` | Remove usuário |

### Contas

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/contas` | Lista contas cadastradas |
| `GET` | `/contas/{id}` | Consulta detalhes da conta |
| `POST` | `/contas` | Cria nova conta |
| `PUT` | `/contas/{id}` | Atualiza dados da conta |
| `DELETE` | `/contas/{id}` | Remove conta |

### Operações Bancárias

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/contas/{id}/deposito` | Realiza aporte de saldo |
| `POST` | `/contas/{id}/saque` | Retira valores (valida saldo e limites) |
| `POST` | `/transferencias` | Envia saldo entre contas distintas |
| `GET` | `/contas/{id}/extrato` | Retorna o histórico cronológico de movimentações |

---

## 🛡️ Segurança & Banco de Dados

- **Criptografia:** senhas armazenadas com hashing **BCrypt**.
- **RBAC (Role Based Access Control):** rotas protegidas conforme o perfil — `ADMIN` (gerenciamento total) e `CLIENTE` (operações restritas à própria conta).
- **Versionamento de schema:** migrations gerenciadas via **Flyway**, garantindo reprodutibilidade entre ambientes de desenvolvimento, teste e produção.

---

## 🧪 Estratégia de Testes

- **Unitários:** regras de negócio das camadas `Service` validadas isoladamente com **Mockito**.
- **Integração:** `Controller` e `Repository` testados de ponta a ponta com **Testcontainers**, subindo uma instância real e isolada do PostgreSQL em Docker durante o build.
- **API end-to-end:** a suíte de testes em **KarateDSL** cobre autenticação, criação de contas, depósito, saque, transferência, extrato e cenários de exceção contra a API real do backend.

---

## 💻 Frontend

### Tecnologias e Conceitos

- **Framework:** Angular 20
- **UI Components:** Angular Material
- **Estilização:** SCSS modularizado
- **Reatividade:** RxJS integrado com **Signals** para gerenciamento de estado performático

### Estrutura do SPA

```text
frontend/src/app/
├── core/         # Guards, Interceptors e serviços globais (Singleton)
├── shared/       # Componentes visuais, pipes e diretivas reaproveitáveis
├── pages/        # Views principais roteadas (Login, Dashboard, Perfil)
├── components/   # Componentes internos de apoio (Navbar, Sidebar)
├── services/     # Clientes HTTP para consumo da API do ByteBank
├── models/       # Interfaces e classes de modelos de dados TypeScript
└── layouts/      # Templates de estrutura de página (Autenticado vs Público)
```

### Fluxos Visuais (Telas)

| Área | Telas |
|---|---|
| **Públicas** | Login, Cadastro de Usuário |
| **Privadas (Cliente)** | Dashboard Financeiro, Detalhes da Conta, Depósito/Saque/Transferência, Extrato Detalhado, Edição de Perfil |
| **Privadas (Admin)** | Administração do Sistema, Lista Global de Contas, Criação de Novas Contas |

---

## 🐳 Docker & Orquestração

O `docker-compose.yml` orquestra três serviços principais:

| Serviço | Responsabilidade |
|---|---|
| `backend` | Aplicação Spring Boot, configurada para aguardar o banco estar pronto (*healthcheck*) |
| `frontend` | Servido via Nginx, otimizado para Single Page Applications (SPA) |
| `postgres` | Banco de dados relacional oficial do projeto |

---

## 🔄 Pipeline CI/CD (GitHub Actions)

A cada push nas branches principais, o pipeline executa automaticamente:

1. Compilação e execução dos testes integrados do backend.
2. Build produtivo do ecossistema Angular.
3. Geração das imagens Docker e publicação automatizada no Docker Hub / GHCR.

---

## 🚀 Módulos de Negócio Avançados

> Extensões planejadas de escopo, voltadas a aproximar o projeto de um ambiente bancário real.

### PIX

- **Escopo:** simulação de transações PIX.
- **Funcionamento:** fluxo completo de envio, recebimento e validação de chaves (CPF/CNPJ, e-mail, telefone e chave aleatória).

### Cartão de Crédito e Cartão Virtual

- **Módulo de Crédito:** gerenciamento de limite, fatura e autorização de compras.
- **Cartões Virtuais:** geração de cartões temporários ou recorrentes para compras online seguras, com regras de expiração e tokenização.

---

## 🏛️ Infraestrutura Avançada

### Notificações em Tempo Real

- **Tecnologia:** WebSockets.
- **Objetivo:** entrega imediata de alertas ao usuário (confirmação de transferências, alertas de segurança, atualizações de saldo) sem necessidade de *polling* constante.

### Processamento Assíncrono Orientado a Eventos

- **Tecnologia:** Apache Kafka.
- **Objetivo:** desacoplamento de microsserviços e processamento assíncrono de transações pesadas, atuando como *message broker* principal para garantir resiliência e ordem dos eventos financeiros.

### Camada de Cache Distribuído

- **Tecnologia:** Redis.
- **Objetivo:** otimizar a leitura de dados de alta demanda e baixa mutabilidade, como consultas a extratos frequentes e dados de sessão do usuário.

---

## 📊 Monitoramento e Observabilidade

Para garantir saúde, alta disponibilidade e rastreabilidade do ambiente produtivo:

| Ferramenta | Função |
|---|---|
| **Prometheus** | Coleta e armazenamento de métricas de séries temporais da aplicação e da infraestrutura |
| **Grafana** | Dashboards em tempo real para indicadores técnicos (CPU, memória, latência) e de negócio (volume de transações por minuto, taxa de erro do PIX) |

---

## ▶️ Como Executar Localmente

```bash
# Clonar o repositório
git clone https://github.com/<seu-usuario>/bytebank-digital.git
cd bytebank-digital

# Subir toda a stack (backend + frontend + postgres)
docker-compose up -d --build

# Acessar
# Frontend:      http://localhost:4200
# API Swagger:   http://localhost:8080/api/swagger-ui.html
```

> ⚠️ Certifique-se de que as portas `4200`, `8080` e `5432` estejam livres antes de subir os containers.

---

## 📄 Licença

Este projeto é distribuído sob a licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.