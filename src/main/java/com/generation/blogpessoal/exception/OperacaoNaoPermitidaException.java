package com.generation.blogpessoal.exception;

public class OperacaoNaoPermitidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperacaoNaoPermitidaException(String mensagem) {
		super(mensagem);
	}

}
