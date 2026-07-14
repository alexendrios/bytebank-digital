-- Adiciona controle de versão (locking otimista) à tabela contas.
-- Funciona como camada extra de proteção contra atualizações
-- concorrentes de saldo além do locking pessimista aplicado em
-- código (SELECT ... FOR UPDATE) nas operações financeiras.
ALTER TABLE contas
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
