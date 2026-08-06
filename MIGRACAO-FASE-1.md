# Fase 1 — Fundação

Refatoração da base antes de qualquer feature nova. Nenhuma funcionalidade foi
adicionada aqui: o objetivo é fechar as brechas e preparar o terreno para o
modelo de conteúdo da Fase 2.

## O que foi corrigido

| Problema | Correção |
|---|---|
| `GET /usuarios/all` devolvia o hash da senha no JSON | DTOs de resposta. A entidade `Usuario` nunca chega ao Jackson |
| Qualquer autenticado editava ou apagava postagem alheia | `validarAutoria()` no `PostagemService`, retorna 403 |
| O autor da postagem vinha no corpo da requisição | Autor extraído do token via `@AuthenticationPrincipal` |
| `postagem.getTema().getId()` estourava NPE → 500 | `temaId` obrigatório no DTO, tema resolvido no service |
| Secret do JWT hardcoded e commitado | `JWT_SECRET` por variável de ambiente, validada no boot |
| Listagens sem paginação | `Pageable` + envelope `PageResponse` em todos os `GET` de coleção |
| Update de usuário re-encodava a senha sempre | Senha opcional no `UsuarioAtualizarRequest` |
| `@Pattern("^[^0-9].*")` rejeitava texto começando com número | Regra removida |
| `texto` limitado a 1000 caracteres | Coluna virou `TEXT`, limite de 20000 na validação |
| `CascadeType.REMOVE` apagava postagens junto com o tema | Exclusão bloqueada se houver postagens vinculadas |
| `@UpdateTimestamp` no campo `data` mudava a data a cada edição | `data` virou `@CreationTimestamp`, `atualizadoEm` criado |
| Erros sem formato definido, stack trace vazando | `@RestControllerAdvice` com payload único de erro |
| `@CrossOrigin("*")` repetido em cada controller | `CorsConfig` central, origens por variável de ambiente |
| Exceção no filtro JWT virava 500 | Filtro não lança mais, deixa o SecurityFilterChain responder 401 |
| Schema criado por `ddl-auto=update` | Flyway com migrations versionadas |
| N+1 ao listar postagens | `LEFT JOIN FETCH` de tema e usuário na mesma query |

## Antes de subir para produção

O banco de produção já existe com dados. O Flyway está configurado com
`baseline-on-migrate=true`, então ele marca o estado atual como V1 e executa
apenas a V2.

A V2 cria uma constraint `UNIQUE` no e-mail. **Rode isso antes:**

```sql
SELECT usuario, COUNT(*) FROM tb_usuarios GROUP BY usuario HAVING COUNT(*) > 1;
```

Se retornar linhas, resolva os duplicados. Se a migration falhar, a aplicação
não sobe.

Variáveis de ambiente novas no Render:

```
JWT_SECRET=<string Base64 com no mínimo 32 bytes>
CORS_ALLOWED_ORIGINS=https://seu-front.vercel.app
JWT_EXPIRATION_MINUTES=60   (opcional, padrão 60)
```

Para gerar o secret:

```bash
openssl rand -base64 48
```

O secret antigo está no histórico do Git, então precisa ser trocado por um novo,
não reaproveitado.

## Rodando local

O ambiente de desenvolvimento agora usa PostgreSQL, o mesmo da produção. O
driver do MySQL foi removido: manter dois bancos diferentes entre dev e prod
esconde bugs que só aparecem no deploy.

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Os testes continuam em H2, com Flyway desligado e schema gerado pelo Hibernate.

```bash
./mvnw test
```

## Mudanças que quebram o front

Essas quatro precisam de ajuste no blog em React.

**1. Login devolve o token sem o prefixo `Bearer`**

```json
{ "id": 1, "nome": "Bruna", "usuario": "...", "foto": "...",
  "token": "eyJhbGciOi...", "tipo": "Bearer", "expiraEm": "2026-08-06T18:00:00Z" }
```

No interceptor do Axios: `Authorization: \`Bearer ${token}\``.

**2. Listagens vêm paginadas**

```json
{ "conteudo": [ ... ], "pagina": 0, "tamanho": 10,
  "totalElementos": 42, "totalPaginas": 5, "primeira": true, "ultima": false }
```

Onde o front fazia `response.data.map(...)`, agora é `response.data.conteudo.map(...)`.

**3. `PUT /postagens` virou `PUT /postagens/{id}`**

O corpo não tem mais `id`, nem `usuario`. Só `titulo`, `texto` e `temaId`:

```json
{ "titulo": "...", "texto": "...", "temaId": 3 }
```

O mesmo vale para `PUT /temas/{id}`.

**4. Cadastro duplicado retorna 409, não 400**

## Endpoints

| Método | Rota | Auth |
|---|---|---|
| POST | `/usuarios/cadastrar` | pública |
| POST | `/usuarios/logar` | pública |
| GET | `/usuarios/all` | token |
| GET | `/usuarios/me` | token |
| GET | `/usuarios/{id}` | token |
| PUT | `/usuarios/atualizar` | token, atualiza o próprio perfil |
| GET | `/postagens` | pública, paginada |
| GET | `/postagens/{id}` | pública |
| GET | `/postagens/titulo/{titulo}` | pública, paginada |
| POST | `/postagens` | token |
| PUT | `/postagens/{id}` | token, só o autor |
| DELETE | `/postagens/{id}` | token, só o autor |
| GET | `/temas`, `/temas/{id}`, `/temas/descricao/{descricao}` | pública |
| POST | `/temas` | token |
| PUT | `/temas/{id}` | token |
| DELETE | `/temas/{id}` | token, bloqueado se houver postagens |

Leitura ficou pública de propósito: um blog existe para ser lido sem login.

## Arquivos removidos

- `src/main/java/com/generation/blogpessoal/model/UsuarioLogin.java`

Substituído por `LoginRequest` e `LoginResponse`. Era um model que não era
entidade e servia de request e response ao mesmo tempo, carregando o campo
`senha` na resposta.

## O que ficou de fora

Papéis e permissões (`ADMIN` / `AUTOR`), rate limiting no login e refresh token.
Nenhum é necessário para a Fase 2 e todos podem entrar depois sem retrabalho.
