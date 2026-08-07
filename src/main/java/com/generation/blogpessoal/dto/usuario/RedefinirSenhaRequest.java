package com.generation.blogpessoal.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(

		@NotBlank(message = "Token obrigatório")
		String token,

		@NotBlank(message = "Informe a nova senha")
		@Size(min = 8, max = 100, message = "A senha deve ter no mínimo 8 caracteres")
		String novaSenha) {
}
