CREATE TABLE movimentacoes (
    id              UUID PRIMARY KEY,
    conta_id        UUID NOT NULL REFERENCES contas (id) ON DELETE CASCADE,
    tipo            VARCHAR(30) NOT NULL,
    valor           NUMERIC(19,2) NOT NULL,
    saldo_anterior  NUMERIC(19,2) NOT NULL,
    saldo_atual     NUMERIC(19,2) NOT NULL,
    descricao       VARCHAR(255),
    data            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimentacoes_conta_id ON movimentacoes (conta_id);
CREATE INDEX idx_movimentacoes_data ON movimentacoes (data);
