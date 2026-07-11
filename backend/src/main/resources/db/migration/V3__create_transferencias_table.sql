CREATE TABLE transferencias (
    id               UUID PRIMARY KEY,
    conta_origem_id  UUID NOT NULL REFERENCES contas (id),
    conta_destino_id UUID NOT NULL REFERENCES contas (id),
    valor            NUMERIC(19,2) NOT NULL,
    data             TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_contas_distintas CHECK (conta_origem_id <> conta_destino_id)
);

CREATE INDEX idx_transferencias_origem ON transferencias (conta_origem_id);
CREATE INDEX idx_transferencias_destino ON transferencias (conta_destino_id);
