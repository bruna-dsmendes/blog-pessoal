package com.generation.blogpessoal.dto.usuario;

import java.time.Instant;

/**
 * O token vai puro, sem o prefixo "Bearer ".
 * O tipo vem separado, no campo tipo, como manda o RFC 6750.
 */
public record LoginResponse(
		Long id,
		String nome,
		String usuario,
		String foto,
		String token,
		String tipo,
		Instant expiraEm) {
}
