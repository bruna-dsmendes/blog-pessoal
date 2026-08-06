package com.generation.blogpessoal.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

	public static RecursoNaoEncontradoException de(String recurso, Long id) {
		return new RecursoNaoEncontradoException(recurso + " de id " + id + " não encontrado(a)");
	}

}
