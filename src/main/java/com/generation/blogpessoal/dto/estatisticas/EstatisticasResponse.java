package com.generation.blogpessoal.dto.estatisticas;

/** Conta apenas conteúdo publicado, que é o que o visitante consegue conferir. */
public record EstatisticasResponse(
		long artigosPublicados,
		long autores,
		long tags,
		long minutosDeConteudo) {
}
