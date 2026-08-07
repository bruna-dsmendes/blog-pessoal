CREATE TABLE tb_links (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT       NOT NULL REFERENCES tb_usuarios (id) ON DELETE CASCADE,
    tipo       VARCHAR(20)  NOT NULL,
    url        VARCHAR(300) NOT NULL,
    ordem      INT          NOT NULL DEFAULT 0,

    -- Um link por rede por pessoa.
    CONSTRAINT uk_links_usuario_tipo UNIQUE (usuario_id, tipo)
);

CREATE INDEX idx_links_usuario ON tb_links (usuario_id);

INSERT INTO tb_links (usuario_id, tipo, url, ordem)
SELECT id, 'GITHUB', link_github, 0
  FROM tb_usuarios
 WHERE link_github IS NOT NULL AND trim(link_github) <> '';

INSERT INTO tb_links (usuario_id, tipo, url, ordem)
SELECT id, 'LINKEDIN', link_linkedin, 1
  FROM tb_usuarios
 WHERE link_linkedin IS NOT NULL AND trim(link_linkedin) <> '';

ALTER TABLE tb_usuarios DROP COLUMN link_github;
ALTER TABLE tb_usuarios DROP COLUMN link_linkedin;
