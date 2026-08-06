-- Estado do banco como o Hibernate criou no bootcamp.
-- Em bancos que ja existem, o Flyway faz baseline nesta versao e nao executa este script.

CREATE TABLE IF NOT EXISTS tb_usuarios (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(255) NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    senha   VARCHAR(255) NOT NULL,
    foto    VARCHAR(5000)
);

CREATE TABLE IF NOT EXISTS tb_temas (
    id        BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_postagens (
    id         BIGSERIAL PRIMARY KEY,
    titulo     VARCHAR(100) NOT NULL,
    texto      VARCHAR(1000) NOT NULL,
    data       TIMESTAMP,
    tema_id    BIGINT REFERENCES tb_temas (id),
    usuario_id BIGINT REFERENCES tb_usuarios (id)
);
