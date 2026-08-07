package com.generation.blogpessoal.exception;

/** 403, e não 401: o token é válido, o que falta é permissão. */
public class OperacaoNaoPermitidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperacaoNaoPermitidaException(String mensagem) {
		super(mensagem);
	}

}
