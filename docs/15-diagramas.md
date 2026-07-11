# 15. Diagramas

Esta página consolida os diagramas já apresentados nos demais documentos, para consulta rápida em um único lugar. Todos são renderizados nativamente pelo GitHub (Mermaid).

## Arquitetura em camadas

Ver `04-arquitetura.md` para a explicação completa do fluxo.

```mermaid
flowchart TB
    subgraph Cliente["Cliente HTTP"]
        C[Browser / curl / Postman]
    end

    subgraph API["Spring Boot API"]
        SEC[JwtAuthenticationFilter + SecurityConfig]
        CTRL[Controller]
        SVC[Service]
        FAC[OperacaoFactory]
        STR[Strategy: Deposito / Saque / Transferencia]
        FACADE[BancoFacade]
        REPO[Repository - Spring Data JPA]
    end

    DB[(PostgreSQL)]

    C -->|HTTP + Bearer JWT| SEC
    SEC --> CTRL
    CTRL --> SVC
    CTRL --> FACADE
    FACADE --> FAC
    FAC --> STR
    FACADE --> REPO
    SVC --> REPO
    REPO --> DB
```

## Modelo de dados (ER)

Ver `05-modelo-dominio.md` para a descrição campo a campo.

```mermaid
erDiagram
    USUARIO ||--o{ CONTA : possui
    CONTA ||--o{ MOVIMENTACAO : registra
    CONTA ||--o{ TRANSFERENCIA : origem
    CONTA ||--o{ TRANSFERENCIA : destino

    USUARIO {
        uuid id PK
        string nome
        string email UK
        string senha
        enum perfil
        datetime data_cadastro
    }
    CONTA {
        uuid id PK
        string numero UK
        string agencia
        decimal saldo
        uuid usuario_id FK
        datetime data_criacao
    }
    MOVIMENTACAO {
        uuid id PK
        uuid conta_id FK
        enum tipo
        decimal valor
        decimal saldo_anterior
        decimal saldo_atual
        datetime data
    }
    TRANSFERENCIA {
        uuid id PK
        uuid conta_origem_id FK
        uuid conta_destino_id FK
        decimal valor
        datetime data
    }
```

## Sequência: login com JWT

Ver `09-seguranca.md` para os detalhes de expiração e claims.

```mermaid
sequenceDiagram
    participant C as Cliente
    participant Auth as AuthController/Service
    participant Jwt as JwtService
    participant DB as PostgreSQL

    C->>Auth: POST /auth/login {email, senha}
    Auth->>DB: valida credenciais
    DB-->>Auth: usuário autenticado
    Auth->>Jwt: gera access + refresh token
    Jwt-->>Auth: dois JWTs
    Auth-->>C: { accessToken, refreshToken }
```

## Sequência: transferência entre contas

```mermaid
sequenceDiagram
    participant C as Cliente
    participant Ctrl as TransferenciaController
    participant Facade as BancoFacade
    participant CS as ContaService
    participant Strat as TransferenciaStrategy
    participant DB as PostgreSQL

    C->>Ctrl: POST /transferencias {origemId, destinoId, valor}
    Ctrl->>Facade: transferir(origemId, destinoId, valor)
    Facade->>CS: buscarEntidadePorId(origemId)
    Facade->>CS: validarPropriedade(origem)
    CS-->>Facade: OK (é o dono) ou 403
    Facade->>CS: buscarEntidadePorId(destinoId)
    Facade->>Strat: executar(origem, destino, valor)
    Strat-->>Facade: [movimentacaoOrigem, movimentacaoDestino]
    Facade->>DB: salva contas + movimentações + Transferencia (1 transação)
    Facade-->>Ctrl: TransferenciaResponse
    Ctrl-->>C: 201 Created
```

## Design patterns: Strategy + Factory + Facade

Ver `11-design-patterns.md` para o código real por trás deste diagrama.

```mermaid
classDiagram
    class BancoFacade {
        +depositar(contaId, valor)
        +sacar(contaId, valor)
        +transferir(origemId, destinoId, valor)
    }
    class OperacaoFactory {
        +resolver(tipo) OperacaoStrategy
    }
    class OperacaoStrategy {
        <<interface>>
        +executar(conta, valor) Movimentacao
        +getTipo() TipoMovimentacao
    }
    class DepositoStrategy
    class SaqueStrategy
    class TransferenciaStrategy

    BancoFacade --> OperacaoFactory
    BancoFacade --> TransferenciaStrategy
    OperacaoFactory --> OperacaoStrategy
    OperacaoStrategy <|.. DepositoStrategy
    OperacaoStrategy <|.. SaqueStrategy
```

## Casos de uso

Ver `14-casos-de-uso.md` para a descrição textual de cada fluxo.

```mermaid
flowchart LR
    Cliente((Cliente))
    Admin((Admin))

    Cliente --> UC1[Registrar-se]
    Cliente --> UC2[Login / Refresh token]
    Cliente --> UC3[Criar conta]
    Cliente --> UC4[Depositar]
    Cliente --> UC5[Sacar]
    Cliente --> UC6[Transferir]
    Cliente --> UC7[Consultar extrato]

    Admin --> UC2
    Admin --> UC8[Gerenciar usuários]
    Admin --> UC9[Consultar qualquer conta]
```
