CREATE TABLE usuarios (
    id             UUID PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    email          VARCHAR(180) NOT NULL UNIQUE,
    senha          VARCHAR(255) NOT NULL,
    perfil         VARCHAR(20)  NOT NULL,
    data_cadastro  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_usuarios_email ON usuarios (email);
