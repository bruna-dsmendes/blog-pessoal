package com.generation.blogpessoal.dto.postagem;

/**
 * Estado das reações de um artigo para quem está pedindo.
 *
 * O campo reagi evita que o front precise adivinhar se deve pintar o botão:
 * a mesma resposta serve para o número e para o estado visual.
 */
public record ReacaoResponse(long total, boolean reagi) {
}
