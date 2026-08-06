# Perfil público de autor

Adiciona bio, links e uma página de autor aberta, no modelo do Medium e do dev.to.

## O que mudou no banco

A migration **V5** acrescenta `username`, `bio`, `link_github` e `link_linkedin`
em `tb_usuarios`, e gera o `username` de quem já existe a partir do nome,
reaproveitando a função `fn_slugify` criada na V3. Homônimos são desempatados
com sufixo numérico antes da constraint `UNIQUE` entrar.

A contração que remove `texto`, `tema_id` e `tb_temas` passa a ser a **V6**.

## Endpoints novos

| Método | Rota | Acesso |
|---|---|---|
| `GET` | `/usuarios/perfil/{username}` | pública |
| `GET` | `/postagens/autor/{username}` | pública, só publicados |

O perfil devolve nome, foto, bio, links, contagem de artigos publicados, total de
minutos escritos e as cinco tags mais usadas pelo autor.

## Decisões

**O perfil público não tem e-mail.** O endereço não diz nada sobre quem escreve
e, numa página aberta, vira endereço exposto para quem coleta e-mail. Ele
continua em `/usuarios/me`, que só o dono acessa.

**As estatísticas contam só o que está publicado.** Quantos rascunhos alguém tem
é informação privada, e apareceria como número na página de quem visita.

**O username é gerado no cadastro, não pedido no formulário.** Sai do nome pelo
mesmo `SlugService` que gera slug de postagem. Quem quiser trocar, troca na
edição do perfil, onde a validação exige letras minúsculas, números e hífen.

**O `AutorResponse` ganhou `username`.** É o que permite ao card do feed e à
página do artigo montarem o link para o perfil sem uma requisição extra.

## Tests

Quatro testes novos: geração de username no cadastro, perfil acessível sem token
e sem e-mail no corpo, 404 para perfil inexistente, e 409 ao tentar usar um
username já ocupado.
