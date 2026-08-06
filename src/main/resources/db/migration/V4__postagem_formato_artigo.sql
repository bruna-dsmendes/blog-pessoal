ALTER TABLE tb_postagens
    ADD COLUMN subtitulo     VARCHAR(200),
    ADD COLUMN conteudo      TEXT,
    ADD COLUMN slug          VARCHAR(160),
    ADD COLUMN capa_url      VARCHAR(1000),
    ADD COLUMN status        VARCHAR(20),
    ADD COLUMN publicado_em  TIMESTAMP,
    ADD COLUMN tempo_leitura INT;

-- O conteudo antigo vira markdown. Nada se perde: texto continua populado
-- ate a V5, que so roda depois que o front estiver migrado.
UPDATE tb_postagens SET conteudo = texto WHERE conteudo IS NULL;

-- Tudo que ja existia estava publicado por definicao.
UPDATE tb_postagens SET status = 'PUBLICADO' WHERE status IS NULL;
UPDATE tb_postagens SET publicado_em = COALESCE(data, now()) WHERE publicado_em IS NULL;

-- Tempo de leitura a 200 palavras por minuto, minimo de 1.
UPDATE tb_postagens
   SET tempo_leitura = GREATEST(1, CEIL(
       COALESCE(array_length(regexp_split_to_array(trim(COALESCE(conteudo, '')), '\s+'), 1), 1)::numeric / 200))
 WHERE tempo_leitura IS NULL;

-- Slug a partir do titulo. O ROW_NUMBER resolve titulos repetidos, que
-- gerariam slug repetido e quebrariam a constraint UNIQUE logo abaixo.
WITH base AS (
    SELECT id,
           left(fn_slugify(titulo), 120) AS s,
           ROW_NUMBER() OVER (PARTITION BY left(fn_slugify(titulo), 120) ORDER BY id) AS rn
      FROM tb_postagens
)
UPDATE tb_postagens p
   SET slug = CASE
                  WHEN b.s = ''   THEN 'postagem-' || p.id
                  WHEN b.rn = 1   THEN b.s
                  ELSE b.s || '-' || b.rn
              END
  FROM base b
 WHERE p.id = b.id;

ALTER TABLE tb_postagens
    ALTER COLUMN conteudo      SET NOT NULL,
    ALTER COLUMN slug          SET NOT NULL,
    ALTER COLUMN status        SET NOT NULL,
    ALTER COLUMN tempo_leitura SET NOT NULL;

ALTER TABLE tb_postagens ADD CONSTRAINT uk_postagens_slug UNIQUE (slug);

-- Fase de expansao: as colunas antigas passam a aceitar null para que a
-- aplicacao nova nao dependa delas. Elas so somem na V5.
ALTER TABLE tb_postagens ALTER COLUMN texto   DROP NOT NULL;
ALTER TABLE tb_postagens ALTER COLUMN tema_id DROP NOT NULL;

-- Indice do feed: filtra por status e ordena por data de publicacao.
CREATE INDEX idx_postagens_feed ON tb_postagens (status, publicado_em DESC);
