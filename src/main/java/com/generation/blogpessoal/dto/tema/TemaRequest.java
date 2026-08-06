package com.generation.blogpessoal.dto.tema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TemaRequest(

		@NotBlank(message = "A descrição é obrigatória")
		@Size(min = 2, max = 255, message = "A descrição deve ter entre 2 e 255 caracteres")
		String descricao) {
}
