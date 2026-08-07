# Redefinição de senha

Fluxo de "esqueci minha senha" com envio de e-mail e invalidação das sessões
abertas.

## Configuração no Brevo

1. Crie a conta em brevo.com
2. Em **Remetentes, domínio, IPs**, cadastre um remetente com um e-mail que você
   acessa. Confirme pelo link recebido, senão o envio falha em silêncio.
3. Em **SMTP & API**, aba **Chaves API**, gere uma chave de API.

Atenção: a **chave de API** é diferente da **chave SMTP**. O projeto usa a
primeira.

Variáveis de ambiente no Render:

| Variável | Valor |
|---|---|
| `MAIL_ENABLED` | `true` |
| `MAIL_API_KEY` | a chave de API do Brevo |
| `MAIL_FROM` | o remetente verificado |
| `MAIL_FROM_NAME` | `Simetria.Dev` |
| `FRONTEND_URL` | a URL do site na Vercel, sem barra no fim |

O `FRONTEND_URL` é o que monta o link do e-mail. Se ficar errado, a mensagem
chega com um endereço que não abre.

Sem `MAIL_ENABLED=true` ou sem a chave, o envio fica desligado e o assunto é
apenas registrado no log. É assim que os testes rodam, e é útil em
desenvolvimento.

## Por que API e não SMTP

A primeira versão usava `spring-boot-starter-mail` com SMTP na porta 587. Em
produção o envio estourava `Connection timed out`: a hospedagem bloqueia saída
SMTP nas portas comuns como medida antispam.

A API HTTP usa a 443, que é HTTPS comum e passa. O `spring-boot-starter-mail`
saiu do projeto, e com ele a configuração de `spring.mail` que o properties de
teste precisava carregar.

Vale como lição: envio de e-mail em hospedagem gratuita quase sempre precisa
ser por API, não por SMTP.

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
