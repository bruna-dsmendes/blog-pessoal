-- O e-mail do usuario passa a ser unico no banco, e nao so na regra de negocio.
-- ANTES DE SUBIR, confira duplicados:
--   SELECT usuario, COUNT(*) FROM tb_usuarios GROUP BY usuario HAVING COUNT(*) > 1;
ALTER TABLE tb_usuarios ADD CONSTRAINT uk_usuarios_usuario UNIQUE (usuario);

-- Texto da postagem deixa de ser limitado a 1000 caracteres.
ALTER TABLE tb_postagens ALTER COLUMN texto TYPE TEXT;

-- Data de criacao e data de edicao passam a ser campos distintos.
ALTER TABLE tb_postagens ADD COLUMN IF NOT EXISTS atualizado_em TIMESTAMP;

-- Toda postagem precisa de tema. Se sobrar alguma orfa sem tema disponivel
-- para adotar, o NOT NULL e adiado em vez de quebrar a migration.
DO $$
BEGIN
    UPDATE tb_postagens
       SET tema_id = (SELECT MIN(id) FROM tb_temas)
     WHERE tema_id IS NULL;

    IF NOT EXISTS (SELECT 1 FROM tb_postagens WHERE tema_id IS NULL) THEN
        ALTER TABLE tb_postagens ALTER COLUMN tema_id SET NOT NULL;
    ELSE
        RAISE NOTICE 'Existem postagens sem tema. NOT NULL em tema_id nao aplicado.';
    END IF;
END $$;

-- Indices nas chaves estrangeiras e na ordenacao padrao do feed.
CREATE INDEX IF NOT EXISTS idx_postagens_tema    ON tb_postagens (tema_id);
CREATE INDEX IF NOT EXISTS idx_postagens_usuario ON tb_postagens (usuario_id);
CREATE INDEX IF NOT EXISTS idx_postagens_data    ON tb_postagens (data DESC);
