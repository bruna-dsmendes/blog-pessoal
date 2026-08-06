package com.generation.blogpessoal.dto.usuario;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A autenticação do navegador acontece pelo cookie httpOnly enviado no
 * cabeçalho Set-Cookie da resposta.
 *
 * O campo {@code token} é obsoleto. Ele existe durante a migração do front e
 * para clientes que não são navegadores (Swagger, Insomnia, testes). O front
 * novo não deve guardar esse valor em lugar nenhum: é o cookie que sustenta a
 * sessão. Sai quando a migração terminar.
 */
public record LoginResponse(
		Long id,
		String nome,
		String usuario,
		String foto,

		@Schema(deprecated = true,
				description = "Obsoleto. Use o cookie de sessão. Será removido.")
		String token,

		String tipo,
		Instant expiraEm) {
}
