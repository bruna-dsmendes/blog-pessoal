package com.generation.blogpessoal.dto.postagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostagemRequest(

		@NotBlank(message = "O título é obrigatório")
		@Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
		String titulo,

		@NotBlank(message = "O texto é obrigatório")
		@Size(min = 10, max = 20000, message = "O texto deve ter entre 10 e 20000 caracteres")
		String texto,

		@NotNull(message = "O tema é obrigatório")
		Long temaId) {
}
