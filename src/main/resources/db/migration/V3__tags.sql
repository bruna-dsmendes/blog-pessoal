-- Funcao de slug usada aqui e na V4. Sem extensao unaccent porque nem todo
-- provedor gerenciado permite CREATE EXTENSION.
CREATE OR REPLACE FUNCTION fn_slugify(texto TEXT) RETURNS TEXT AS $$
    SELECT trim(BOTH '-' FROM
        regexp_replace(
            lower(translate(texto,
                'áàâãäéèêëíìîïóòôõöúùûüçñÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ',
                'aaaaaeeeeiiiiooooouuuucnAAAAAEEEEIIIIOOOOOUUUUCN')),
            '[^a-z0-9]+', '-', 'g'));
$$ LANGUAGE SQL IMMUTABLE;

CREATE TABLE tb_tags (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    slug VARCHAR(60) NOT NULL,
    CONSTRAINT uk_tags_slug UNIQUE (slug)
);

CREATE TABLE tb_postagem_tags (
    postagem_id BIGINT NOT NULL REFERENCES tb_postagens (id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES tb_tags (id),
    PRIMARY KEY (postagem_id, tag_id)
);

CREATE INDEX idx_postagem_tags_tag ON tb_postagem_tags (tag_id);

-- Cada tema vira uma tag. O DISTINCT ON evita colisao caso dois temas
-- diferentes gerem o mesmo slug ("Java" e "java ").
INSERT INTO tb_tags (nome, slug)
SELECT DISTINCT ON (fn_slugify(descricao)) descricao, fn_slugify(descricao)
  FROM tb_temas
 WHERE fn_slugify(descricao) <> ''
 ORDER BY fn_slugify(descricao), id;

-- Cada postagem herda a tag correspondente ao tema que ela ja tinha.
INSERT INTO tb_postagem_tags (postagem_id, tag_id)
SELECT p.id, t.id
  FROM tb_postagens p
  JOIN tb_temas tm ON tm.id = p.tema_id
  JOIN tb_tags  t  ON t.slug = fn_slugify(tm.descricao)
ON CONFLICT DO NOTHING;
