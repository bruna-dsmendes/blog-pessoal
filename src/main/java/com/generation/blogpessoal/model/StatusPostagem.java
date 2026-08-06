package com.generation.blogpessoal.model;

public enum StatusPostagem {

	/** Só o autor enxerga. */
	RASCUNHO,

	/** Visível no feed público. */
	PUBLICADO,

	/** Sai do feed, mas continua acessível pela URL direta para quem tem o link. */
	ARQUIVADO

}
