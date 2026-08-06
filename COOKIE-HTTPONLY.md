# Sessão em cookie httpOnly

Fase intermediária entre a Fase 2 e a migração do front. Move a sessão do corpo
da resposta para um cookie que o JavaScript não consegue ler.

## Por que

Com o token em `localStorage`, qualquer XSS rouba a sessão inteira e ela
continua válida fora do navegador da vítima. Com `httpOnly`, um script injetado
não tem como extrair o token: no máximo faz requisições enquanto a pessoa está
com a página aberta.

Isso importa especialmente aqui, porque o front vai renderizar markdown, que é
justamente um vetor de XSS.

## Arquivos alterados

| Arquivo | O que mudou |
|---|---|
| `security/AuthCookieService.java` | **novo**. Monta, apaga e lê o cookie |
| `security/JwtAuthFilter.java` | Lê o cookie primeiro, header como alternativa |
| `security/SecurityConfig.java` | Libera `/usuarios/deslogar` |
| `controller/UsuarioController.java` | Login envia `Set-Cookie`, novo `POST /usuarios/deslogar` |
| `dto/usuario/LoginResponse.java` | Campo `token` marcado como obsoleto |
| `application.properties` | `app.cookie.name`, `.secure`, `.same-site` |
| `application-dev.properties` | `secure=false`, porque localhost não é HTTPS |
| testes | 4 testes novos, e o `JwtHelper` ganha suporte a cookie |

## Decisões

**Cookie primeiro, header depois.** O navegador autentica por cookie. O header
`Authorization` continua aceito para Swagger, Insomnia e os testes, que não são
navegadores. Sem isso, o Swagger deixaria de funcionar e os 20 testes existentes
precisariam ser reescritos.

**`SameSite=Lax` é a defesa contra CSRF.** Com o token em header, CSRF não
existia: nenhum site consegue mandar seu header. Com cookie, o navegador anexa
sozinho, e aí a proteção precisa vir de outro lugar. O `Lax` impede o envio do
cookie em `POST` originado de outro site, que é exatamente o cenário de CSRF.
Um token anti-CSRF só passa a ser necessário se algum dia for preciso
`SameSite=None`.

**`Secure=true` por padrão.** Só o perfil `dev` desliga. Em `http://localhost`
não existe HTTPS e o navegador descartaria o cookie em silêncio, sem erro
nenhum no console.

**Logout precisa existir no servidor.** O front não enxerga o cookie httpOnly,
então não consegue apagá-lo. Só quem escreveu pode sobrescrever com validade
zero.

**O campo `token` continua na resposta.** Mesmo padrão de expand and contract
das fases anteriores: o front migra, e depois o campo sai. O front novo não deve
guardar esse valor em lugar nenhum.

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `COOKIE_NAME` | `blog_token` | Nome do cookie |
| `COOKIE_SECURE` | `true` | Exige HTTPS |
| `COOKIE_SAME_SITE` | `Lax` | Política de envio entre sites |

## O que falta do lado do front

Front na Vercel e API no Render são domínios diferentes, e nesse cenário o
cookie da API é cookie de terceiro. O Safari bloqueia, e o Chrome está na mesma
transição.

A solução é fazer os dois parecerem o mesmo site, com um rewrite no
`vercel.json`:

```json
{
  "rewrites": [
    { "source": "/api/:path*", "destination": "https://SEU-BACKEND.onrender.com/:path*" }
  ]
}
```

O front passa a chamar `/api/postagens` em vez da URL do Render. O navegador
enxerga tudo no domínio da Vercel, o cookie vira primeiro, e o CORS deixa de ser
necessário.

Com um domínio próprio no futuro, `blog.dominio.dev` e `api.dominio.dev` contam
como o mesmo site e o rewrite deixa de ser preciso.
