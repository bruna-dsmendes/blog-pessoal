-- Campos de perfil publico. A funcao fn_slugify foi criada na V3.

ALTER TABLE tb_usuarios
    ADD COLUMN username       VARCHAR(30),
    ADD COLUMN bio            VARCHAR(280),
    ADD COLUMN link_github    VARCHAR(200),
    ADD COLUMN link_linkedin  VARCHAR(200);

-- Gera username a partir do nome. O ROW_NUMBER desempata homonimos, que
-- gerariam o mesmo valor e quebrariam a constraint UNIQUE abaixo.
WITH base AS (
    SELECT id,
           left(fn_slugify(nome), 30) AS u,
           ROW_NUMBER() OVER (PARTITION BY left(fn_slugify(nome), 30) ORDER BY id) AS rn
      FROM tb_usuarios
)
UPDATE tb_usuarios usr
   SET username = CASE
                      WHEN b.u = ''  THEN 'usuario-' || usr.id
                      WHEN b.rn = 1  THEN b.u
                      ELSE left(b.u, 26) || '-' || b.rn
                  END
  FROM base b
 WHERE usr.id = b.id;

ALTER TABLE tb_usuarios ALTER COLUMN username SET NOT NULL;
ALTER TABLE tb_usuarios ADD CONSTRAINT uk_usuarios_username UNIQUE (username);
