# Redefinição de senha

Fluxo de "esqueci minha senha" com envio de e-mail e invalidação das sessões
abertas.

## Configuração no Brevo

1. Crie a conta em brevo.com
2. Em **Senders, Domains & Dedicated IPs**, aba **Senders**, cadastre um
   remetente com um e-mail que você acessa. Confirme pelo link recebido.
3. Em **SMTP & API**, aba **SMTP**, copie o **login** e a **master password**.

Variáveis de ambiente no Render:

| Variável | Valor |
|---|---|
| `MAIL_ENABLED` | `true` |
| `MAIL_HOST` | `smtp-relay.brevo.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | o login do SMTP do Brevo |
| `MAIL_PASSWORD` | a master password do SMTP |
| `MAIL_FROM` | o remetente verificado |
| `MAIL_FROM_NAME` | `Simetria.Dev` |
| `FRONTEND_URL` | a URL do site na Vercel, sem barra no fim |

O `FRONTEND_URL` é o que monta o link do e-mail. Se ficar errado, a mensagem
chega com um endereço que não abre.

Sem `MAIL_ENABLED=true`, o envio fica desligado e o assunto é apenas registrado
no log. É assim que os testes rodam, e é útil em desenvolvimento: peça a
redefinição e leia o log para pegar o link.

## Decisões

**O token vai para o banco como hash SHA-256.** Ele é uma senha temporária.
Quem tiver acesso de leitura ao banco não deve conseguir usá-lo.

**A resposta é sempre 204, exista ou não a conta.** Responder diferente
transformaria o endpoint em uma ferramenta para descobrir quem tem cadastro. A
tela de confirmação também aparece igual nos dois casos.

**Um pedido novo apaga os anteriores.** Evita acumular links válidos.

**O token é de uso único e vale 30 minutos.**

**Trocar a senha derruba as sessões abertas.** Como a sessão é um JWT sem
estado, mudar a senha não invalidaria nada sozinho: quem já estava logado
continuaria por até uma hora. Isso esvazia o sentido de pedir redefinição quando
a conta foi invadida.

A solução é a coluna `senha_alterada_em`. O `JwtAuthFilter` compara a data de
emissão do token com essa marca e descarta o que for anterior. Vale também para
a troca de senha feita na tela de perfil.

**O envio é assíncrono e tolerante a falha.** A pessoa não espera o SMTP
responder, e uma indisponibilidade do Brevo não derruba a requisição nem revela
se a conta existe.

## Endpoints

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/usuarios/esqueci-a-senha` | pública |
| `POST` | `/usuarios/redefinir-senha` | pública |

## Telas

`/esqueci-a-senha` e `/redefinir-senha?token=...`, ambas com link a partir do
login.

## O que ficou de fora

Não há limite de tentativas. Alguém pode disparar muitos pedidos para o mesmo
e-mail e gerar spam na caixa de outra pessoa, ou queimar a cota diária do Brevo.
Um controle simples por e-mail e por IP resolve, e vale entrar junto com o
limite de tentativas de login, que também não existe.
