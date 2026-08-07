package com.generation.blogpessoal.model;

public enum StatusPostagem {

	RASCUNHO,

	/** Único status visível no feed público. */
	PUBLICADO,

	/** Sai do feed, mas segue acessível pela URL direta. */
	ARQUIVADO

}
