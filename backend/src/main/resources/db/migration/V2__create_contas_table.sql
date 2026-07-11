CREATE TABLE contas (
    id             UUID PRIMARY KEY,
    numero         VARCHAR(20) NOT NULL UNIQUE,
    agencia        VARCHAR(10) NOT NULL,
    saldo          NUMERIC(19,2) NOT NULL DEFAULT 0,
    usuario_id     UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    data_criacao   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_contas_usuario_id ON contas (usuario_id);
