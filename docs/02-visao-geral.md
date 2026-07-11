# 02. Visão Geral

## Proposta de valor

O ByteBank Digital simula as operações essenciais de um banco digital: abertura de conta, depósito, saque, transferência entre contas e consulta de extrato — tudo protegido por autenticação JWT e controle de acesso baseado em papéis (RBAC).

## Perfis de usuário (personas)

| Perfil | O que pode fazer |
|---|---|
| **CLIENTE** | Criar/gerenciar suas próprias contas, depositar, sacar, transferir para qualquer conta, consultar seu próprio extrato. Não enxerga dados de outros clientes. |
| **ADMIN** | Tudo que o CLIENTE faz, mais: gerenciar (CRUD) qualquer usuário do sistema via `/usuarios`. Não há hoje um "modo Deus" que dispense as checagens de propriedade sobre contas de terceiros além do CRUD de usuários — isso é intencional, para manter o escopo de acesso administrativo restrito ao que o README original definia. |

## Módulos do sistema

### Núcleo bancário (implementado)

- **Autenticação**: registro, login, refresh token.
- **Gestão de usuários**: CRUD restrito a ADMIN.
- **Gestão de contas**: criação, consulta, atualização, remoção — com regra de propriedade.
- **Operações bancárias**: depósito, saque, transferência entre contas.
- **Extrato**: histórico paginado de movimentações por conta.

### Módulos avançados (planejados, não implementados)

Descritos originalmente na especificação de arquitetura do projeto, ainda não têm código associado:

- **PIX**: simulação de transações via chave (CPF/CNPJ, e-mail, telefone, aleatória).
- **Cartão de crédito e cartão virtual**: limite, fatura, tokenização.
- **Notificações em tempo real**: WebSockets para alertas de transação/segurança.
- **Processamento assíncrono**: Apache Kafka como *message broker* para transações pesadas.
- **Cache distribuído**: Redis para consultas de extrato de alta frequência.
- **Observabilidade**: Prometheus + Grafana para métricas técnicas e de negócio.

### Frontend (planejado, não implementado)

SPA em Angular 20 consumindo a API REST descrita neste documento — ver `07-frontend.md` para o desenho planejado.

## Fora de escopo (por ora)

- Integração com sistemas bancários reais (SPB, Bacen, etc.) — este é um projeto de simulação/estudo.
- Multi-moeda / operações internacionais.
- Multi-tenancy (o sistema assume uma única instituição financeira).
