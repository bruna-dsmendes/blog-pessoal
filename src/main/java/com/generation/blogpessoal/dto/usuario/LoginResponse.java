package com.generation.blogpessoal.dto.usuario;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

/** A sessão vem no cookie httpOnly do cabeçalho Set-Cookie. */
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
