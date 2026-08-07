CREATE TABLE tb_reacoes (
    id          BIGSERIAL PRIMARY KEY,
    postagem_id BIGINT      NOT NULL REFERENCES tb_postagens (id) ON DELETE CASCADE,
    usuario_id  BIGINT      NOT NULL REFERENCES tb_usuarios (id) ON DELETE CASCADE,
    tipo        VARCHAR(20) NOT NULL,
    criado_em   TIMESTAMP   NOT NULL,

    -- Uma reacao por pessoa por artigo. E o banco que garante isso: dois
    -- cliques simultaneos passariam por qualquer verificacao feita no codigo.
    CONSTRAINT uk_reacoes_postagem_usuario UNIQUE (postagem_id, usuario_id)
);

CREATE INDEX idx_reacoes_postagem ON tb_reacoes (postagem_id);
CREATE INDEX idx_reacoes_usuario  ON tb_reacoes (usuario_id);
