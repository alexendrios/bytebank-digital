-- Suporte a bloqueio temporário de conta após múltiplas tentativas
-- de login malsucedidas (proteção contra força bruta).
ALTER TABLE usuarios
    ADD COLUMN tentativas_falhas INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_ate TIMESTAMP NULL;
