# Fase 2 — Conteúdo em formato de artigo

Transforma a postagem do bootcamp em artigo publicável: markdown longo, slug,
subtítulo, capa, ciclo de rascunho e publicação, e tags N:N no lugar do tema.

## Modelo

`tb_postagens` ganhou `subtitulo`, `conteudo` (TEXT, markdown), `slug` único,
`capa_url`, `status`, `publicado_em` e `tempo_leitura`. O campo `data` virou a
data de criação e `atualizado_em` a da última edição, então passam a ser três
datas com papéis distintos.

`tb_tags` e `tb_postagem_tags` substituem `tb_temas`. Os 9 temas existentes
viraram tags na V3, e cada postagem herdou a tag equivalente ao tema que tinha.

## Expand and contract

As migrations **V3** e **V4** só expandem. As colunas `texto` e `tema_id`
continuam no banco, agora aceitando null, e `tb_temas` segue existindo.

A entidade `Postagem` ainda escreve em `texto` a cada save, embora não leia mais
esse campo. É escrita dupla proposital: se o deploy der errado e você voltar a
versão anterior da aplicação, ela encontra tudo que precisa, inclusive nas
postagens criadas depois da migração.

Os endpoints `/temas` continuam de pé, só que em modo leitura e marcados como
obsoletos no Swagger.

A **V5** é a fase de contração e ainda não existe neste repositório. Ela remove
`texto`, `tema_id`, `tb_temas`, a função `fn_slugify`, e junto com ela saem
`Tema`, `TemaService`, `TemaController`, `TemaRepository` e `TemaResponse`. Só
escreva a V5 depois que o front estiver rodando com tags em produção.

## Decisões

**Toda postagem nasce rascunho.** O status não está no corpo de criação nem de
atualização. Ele muda por endpoints próprios, o que evita despublicar um artigo
sem querer ao salvar uma correção de vírgula.

**A data de publicação congela.** Gravada só na primeira vez que o artigo é
publicado. Republicar depois de arquivar não joga o texto para o topo do feed.

**O slug congela junto.** Enquanto é rascunho, o slug acompanha o título. Depois
de publicado, mudar o título não muda a URL, porque o link já circulou.

**Rascunho de terceiro responde 404, não 403.** Um 403 confirmaria que o recurso
existe, e daria para varrer ids e descobrir quantos rascunhos alguém tem.

**Tags nascem com a postagem.** Como no dev.to, quem escreve digita as tags em
vez de escolher de um cadastro. O slug é a chave de deduplicação, então
"Spring Boot", "spring boot" e "Spring  Boot" viram a mesma tag. Limite de 5.

**O feed não carrega o markdown.** `PostagemResumoResponse` não tem o campo
`conteudo`. Um artigo pode ter 50 mil caracteres, e mandar isso para 10 itens de
uma listagem seriam megabytes de JSON que a tela do feed nem usa.

**Tags vêm por `@BatchSize`, não por `JOIN FETCH`.** Buscar coleção junto com
paginação faz o Hibernate trazer tudo para a memória e paginar em Java. Com
`@BatchSize`, uma página de 10 postagens resolve as tags em uma consulta extra.

## Endpoints

| Método | Rota | Auth |
|---|---|---|
| GET | `/postagens` | pública, só publicadas |
| GET | `/postagens/buscar?termo=` | pública |
| GET | `/postagens/tag/{slugTag}` | pública |
| GET | `/postagens/slug/{slug}` | pública |
| GET | `/postagens/{id}` | pública |
| GET | `/postagens/minhas?status=` | token, inclui rascunhos |
| POST | `/postagens` | token, cria rascunho |
| PUT | `/postagens/{id}` | token, só o autor |
| DELETE | `/postagens/{id}` | token, só o autor |
| PATCH | `/postagens/{id}/publicar` | token, só o autor |
| PATCH | `/postagens/{id}/arquivar` | token, só o autor |
| PATCH | `/postagens/{id}/rascunho` | token, só o autor |
| GET | `/tags`, `/tags/buscar?nome=`, `/tags/{slug}` | pública |
| GET | `/temas/**` | pública, obsoleto |

## Contrato da postagem

Criação e atualização:

```json
{
  "titulo": "Como usar Java 21 na prática",
  "subtitulo": "Records, pattern matching e virtual threads",
  "conteudo": "# Introdução\n\nTexto em **markdown**...",
  "capaUrl": "https://...",
  "tags": ["Java", "Spring Boot"]
}
```

Resposta completa traz `slug`, `status`, `tempoLeitura`, `criadoEm`,
`atualizadoEm`, `publicadoEm`, `autor` e `tags`. O feed traz o mesmo, sem
`conteudo` e sem `criadoEm`.

## O que ficou de fora

Sanitização do markdown ainda não está implementada. Enquanto o front renderizar
o conteúdo, `<img onerror=...>` dentro do markdown é XSS. Isso entra na Fase 3
junto com upload, usando CommonMark e Jsoup com whitelist. Até lá, o front deve
renderizar markdown com HTML desabilitado.

Também ficaram para depois: paginação por cursor no feed, busca full text com
`tsvector`, e contagem de visualizações.
