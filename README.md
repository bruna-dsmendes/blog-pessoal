# Blog Pessoal — API

Back-end RESTful de um blog pessoal em **Java 21** e **Spring Boot 3.5**, com
autenticação JWT, schema versionado e cobertura de testes nos fluxos críticos.

Projeto originalmente desenvolvido no Bootcamp Full Stack Java da
[Generation Brasil](https://www.generation.org/brasil/) e em evolução contínua.

## Sobre

API de postagens e temas com autenticação de usuários. A base do bootcamp foi
refatorada para tratar as questões que um CRUD de exercício não cobre:
autorização por dono do recurso, separação entre entidade e contrato da API,
paginação, versionamento de schema e tratamento uniforme de erros.

O registro do que mudou e por quê está em [MIGRACAO-FASE-1.md](MIGRACAO-FASE-1.md).

## Decisões de projeto

- **DTOs separados das entidades.** A entidade nunca é serializada. Isso impede
  vazamento de campo sensível e mass assignment: o cliente não consegue definir
  o autor de uma postagem mandando `usuarioId` no corpo.
- **Autoria verificada no service.** Editar ou apagar postagem exige ser o autor.
  Só checar se o id existe deixa qualquer autenticado alterar conteúdo alheio.
- **Flyway.** Schema versionado em SQL, com histórico. `ddl-auto=update` funciona
  até a primeira mudança que o Hibernate não sabe aplicar sozinho.
- **Mesmo banco em dev e prod.** PostgreSQL nos dois. Diferença de dialeto entre
  ambientes é bug que só aparece no deploy.
- **Leitura pública, escrita autenticada.** Blog serve para ser lido sem login.
- **`LEFT JOIN FETCH` nas listagens.** Sem isso, uma página de 10 postagens
  dispara 21 consultas.

## Tecnologias

Java 21 · Spring Boot 3.5 · Spring Security + JWT (jjwt) · Spring Data JPA ·
Flyway · PostgreSQL · H2 (testes) · springdoc-openapi · Maven · Docker

## Rodando localmente

```bash
git clone https://github.com/bruna-dsmendes/blog-pessoal.git
cd blog-pessoal

# sobe o Postgres local
docker compose up -d

# roda a aplicação no perfil de desenvolvimento
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080` e o Swagger em `/swagger-ui/index.html`.

```bash
./mvnw test
```

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `JWT_SECRET` | sim em prod | Chave Base64 de no mínimo 32 bytes |
| `JWT_EXPIRATION_MINUTES` | não | Validade do token, padrão 60 |
| `CORS_ALLOWED_ORIGINS` | não | Origens permitidas, separadas por vírgula |
| `POSTGRESHOST`, `POSTGRESPORT`, `POSTGRESDATABASE`, `POSTGRESUSER`, `POSTGRESPASSWORD` | sim em prod | Conexão com o banco |

Gerando um secret:

```bash
openssl rand -base64 48
```

## Roadmap

- [x] **Fase 1** — DTOs, autorização por autor, paginação, Flyway, tratamento de erros
- [ ] **Fase 2** — Artigos em markdown, slug, rascunho e publicação, tags N:N
- [ ] **Fase 3** — Upload de imagens e embed de vídeo
- [ ] **Fase 4** — Comentários e reações
