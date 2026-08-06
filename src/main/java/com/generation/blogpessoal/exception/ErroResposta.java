package com.generation.blogpessoal.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Formato único de erro da API. Todo erro sai assim, o que deixa o
 * tratamento no front previsível.
 */
public record ErroResposta(
		Instant timestamp,
		int status,
		String erro,
		String mensagem,
		String caminho,
		Map<String, String> campos) {

	public static ErroResposta de(int status, String erro, String mensagem, String caminho) {
		return new ErroResposta(Instant.now(), status, erro, mensagem, caminho, null);
	}

	public static ErroResposta comCampos(int status, String erro, String mensagem,
			String caminho, Map<String, String> campos) {
		return new ErroResposta(Instant.now(), status, erro, mensagem, caminho, campos);
	}

}
