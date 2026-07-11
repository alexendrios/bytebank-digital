# 08. Banco de Dados

## Estratégia de versionamento de schema

Todo o schema é gerenciado via **Flyway**, com `spring.jpa.hibernate.ddl-auto: validate` — ou seja, o Hibernate nunca altera o schema automaticamente; ele apenas valida que as entidades JPA batem com as tabelas criadas pelas migrations. Isso garante que o schema em produção seja sempre idêntico ao schema testado em CI (via Testcontainers) e em desenvolvimento local.

## Migrations

| Versão | Arquivo | O que cria |
|---|---|---|
| V1 | `V1__create_usuarios_table.sql` | Tabela `usuarios` + índice em `email` |
| V2 | `V2__create_contas_table.sql` | Tabela `contas` + FK para `usuarios` + índice em `usuario_id` |
| V3 | `V3__create_transferencias_table.sql` | Tabela `transferencias` + FKs para `contas` (origem/destino) + `CHECK` garantindo origem ≠ destino |
| V4 | `V4__create_movimentacoes_table.sql` | Tabela `movimentacoes` + FK para `contas` + índices em `conta_id` e `data` |

## Schema (resumido)

```sql
usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    data_cadastro TIMESTAMP NOT NULL DEFAULT now()
)

contas (
    id UUID PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    agencia VARCHAR(10) NOT NULL,
    saldo NUMERIC(19,2) NOT NULL DEFAULT 0,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    data_criacao TIMESTAMP NOT NULL DEFAULT now()
)

transferencias (
    id UUID PRIMARY KEY,
    conta_origem_id UUID NOT NULL REFERENCES contas(id),
    conta_destino_id UUID NOT NULL REFERENCES contas(id),
    valor NUMERIC(19,2) NOT NULL,
    data TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_contas_distintas CHECK (conta_origem_id <> conta_destino_id)
)

movimentacoes (
    id UUID PRIMARY KEY,
    conta_id UUID NOT NULL REFERENCES contas(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    saldo_anterior NUMERIC(19,2) NOT NULL,
    saldo_atual NUMERIC(19,2) NOT NULL,
    descricao VARCHAR(255),
    data TIMESTAMP NOT NULL DEFAULT now()
)
```

## Por que `ON DELETE CASCADE` em `contas.usuario_id` e `movimentacoes.conta_id`?

Remover um usuário remove suas contas (regra de negócio: uma conta não existe sem dono); remover uma conta remove seu histórico de movimentações. Isso é aceitável no estágio atual do projeto — em um sistema bancário real, provavelmente haveria **soft delete** em vez de exclusão física, para preservar histórico auditável mesmo após "encerramento" de conta. Vale revisitar essa decisão se o projeto avançar para os módulos de compliance/auditoria.

## Índices

| Tabela | Índice | Motivo |
|---|---|---|
| `usuarios` | `email` | Login busca por e-mail (`findByEmail`) |
| `contas` | `usuario_id` | Listagem de contas por dono (`findByUsuarioId`) |
| `transferencias` | `conta_origem_id`, `conta_destino_id` | Consultas futuras de histórico de transferências por conta |
| `movimentacoes` | `conta_id`, `data` | Extrato paginado ordenado por data (`findByContaIdOrderByDataDesc`) |

## UUID em vez de identificadores sequenciais

Todas as chaves primárias usam `UUID` gerado em memória pelo Hibernate (`@UuidGenerator`), sem depender de extensões do PostgreSQL como `pgcrypto`/`uuid-ossp`. Isso evita que um ID sequencial revele quantidade de registros ou permita enumeração de recursos (ex.: tentar `/contas/1`, `/contas/2`, ...).
