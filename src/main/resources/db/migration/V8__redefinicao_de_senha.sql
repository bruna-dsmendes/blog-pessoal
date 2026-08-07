-- Marca o momento da ultima troca de senha. Token JWT emitido antes disso
-- deixa de valer, o que derruba sessoes abertas quando a senha e redefinida.
ALTER TABLE tb_usuarios ADD COLUMN senha_alterada_em TIMESTAMP;

CREATE TABLE tb_tokens_senha (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES tb_usuarios (id) ON DELETE CASCADE,

    -- Guardado como hash: quem le o banco nao consegue usar o token.
    token_hash  VARCHAR(64)  NOT NULL,

    criado_em   TIMESTAMP    NOT NULL,
    expira_em   TIMESTAMP    NOT NULL,
    usado_em    TIMESTAMP,

    CONSTRAINT uk_tokens_senha_hash UNIQUE (token_hash)
);

CREATE INDEX idx_tokens_senha_usuario ON tb_tokens_senha (usuario_id);
