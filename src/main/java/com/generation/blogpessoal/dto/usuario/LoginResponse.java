package com.generation.blogpessoal.dto.usuario;

import java.time.Instant;

public record LoginResponse(
		Long id,
		String nome,
		String usuario,
		String foto,
		String token,
		String tipo,
		Instant expiraEm) {
}
