package com.generation.blogpessoal.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(

		@NotBlank(message = "O nome da tag é obrigatório")
		@Size(min = 2, max = 50, message = "A tag deve ter entre 2 e 50 caracteres")
		String nome) {
}
