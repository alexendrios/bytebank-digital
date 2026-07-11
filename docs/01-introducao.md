# 01. Introdução

## O que é o ByteBank Digital

O **ByteBank Digital** é uma plataforma bancária digital construída como projeto de estudo e portfólio, com o objetivo de demonstrar, em um cenário próximo do real, a aplicação prática de:

- **Design Patterns (GoF)** — Strategy, Factory Method, Facade, Singleton, Builder — resolvendo problemas de negócio reais, não como exercício acadêmico isolado.
- **Arquitetura em camadas** com separação clara de responsabilidades (Controller → Service → Factory/Strategy → Repository).
- **Autenticação e autorização robustas** (JWT + RBAC + autorização por propriedade de recurso).
- **Qualidade via testes**: unitários (Mockito) e de integração de ponta a ponta com banco de dados real (Testcontainers).
- **Práticas de DevOps**: containerização, migrations versionadas (Flyway) e pipeline de CI/CD.

## Por que um "banco digital"?

O domínio bancário foi escolhido porque força decisões de design não triviais logo de início:

- Operações financeiras exigem **consistência transacional** (uma transferência não pode debitar da origem sem creditar no destino).
- Diferentes tipos de operação (depósito, saque, transferência, PIX) têm regras distintas, mas compartilham um fluxo comum — o cenário canônico para o padrão **Strategy**.
- Existem dois perfis de usuário com permissões bem diferentes (ADMIN e CLIENTE), o que exige controle de acesso em duas camadas: por papel (RBAC) e por propriedade do recurso (um cliente só vê a própria conta).

## Status atual do projeto

| Componente | Status |
|---|---|
| Backend (API REST) | ✅ MVP implementado e testado (49 testes) |
| Autenticação JWT + RBAC | ✅ Implementado |
| Operações bancárias (depósito/saque/transferência) | ✅ Implementado |
| CI/CD (GitHub Actions) | ✅ Implementado |
| Frontend (Angular) | ⏳ Ainda não iniciado |
| Módulos avançados (PIX, cartões, Kafka, Redis, WebSockets) | ⏳ Planejado, não iniciado |

## Como navegar esta documentação

Os documentos em `docs/` aprofundam tópicos que o `README.md` principal apresenta de forma resumida:

- **02 a 05**: visão geral, requisitos, arquitetura e modelo de domínio — o "porquê" das decisões.
- **06 a 10**: backend, banco de dados, segurança e API — o "como" foi implementado.
- **11**: design patterns em detalhe, com trechos de código reais do projeto.
- **12 e 13**: estratégia de testes e DevOps.
- **14 e 15**: casos de uso e diagramas (arquitetura, sequência, entidade-relacionamento).
