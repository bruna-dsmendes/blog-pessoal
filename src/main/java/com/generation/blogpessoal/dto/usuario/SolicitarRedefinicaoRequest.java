package com.generation.blogpessoal.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarRedefinicaoRequest(

		@NotBlank(message = "Informe o e-mail da conta")
		@Email(message = "Informe um e-mail válido")
		String usuario) {
}
