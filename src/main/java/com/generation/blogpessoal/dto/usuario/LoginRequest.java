package com.generation.blogpessoal.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@Schema(example = "email@email.com.br")
		@NotBlank(message = "O e-mail é obrigatório")
		String usuario,

		@NotBlank(message = "A senha é obrigatória")
		String senha) {
}
