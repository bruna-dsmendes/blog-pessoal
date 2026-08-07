# Reações

Curtida por artigo, no modelo de uma reação por pessoa.

## Modelo

`tb_reacoes` liga postagem e usuário, com `tipo` e data. A constraint
`UNIQUE (postagem_id, usuario_id)` é o coração da tabela.

A migration é a **V6**. A contração que remove `texto`, `tema_id` e `tb_temas`
passa a ser a V7.

## Decisões

**Uma reação por pessoa, não claps ilimitados.** O número vira sinal de quantas
pessoas gostaram, e não de quem clicou mais. Também impede alguém inflar o
próprio artigo segurando o botão.

**A unicidade é garantida pelo banco.** Verificar antes de inserir resolve o
caso comum, mas dois cliques simultâneos passam pela verificação ao mesmo tempo.
A constraint barra o segundo, e o service trata a exceção como sucesso, porque o
estado final é o que a pessoa esperava.

**Reagir é idempotente.** Clicar duas vezes não dá erro nem conta duas vezes.
Um botão que responde 409 quando a rede engasga e a requisição sai repetida
transforma um problema invisível em erro na cara de quem usa.

**Rascunho não recebe reação.** Ele nem deveria estar visível para terceiros, e
a rota responde 404 em vez de 403 pelo mesmo motivo da leitura: um 403
confirmaria que o recurso existe.

**O tipo já nasce como enum.** Só existe `CURTIR`, mas acrescentar `SALVAR`
depois vira uma linha, sem migration de estrutura.

## Endpoints

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/postagens/{id}/reagir` | token |
| `DELETE` | `/postagens/{id}/reagir` | token |

Ambos devolvem `{ "total": 3, "reagi": true }`, que serve ao mesmo tempo para o
número e para o estado visual do botão.

`GET /postagens/{id}` e `/postagens/slug/{slug}` passam a trazer os mesmos dois
campos.

## O que ficou de fora

**O feed não traz contagem de reações.** Somar por artigo numa listagem traz o
problema N+1 de volta. Resolver isso pede `@Formula` do Hibernate ou uma coluna
de contador na postagem, e vale decidir quando a tela pedir, não antes.
