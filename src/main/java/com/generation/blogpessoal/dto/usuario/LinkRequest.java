package com.generation.blogpessoal.dto.usuario;

import com.generation.blogpessoal.model.TipoLink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LinkRequest(

		@NotNull(message = "O tipo do link é obrigatório")
		TipoLink tipo,

		@NotBlank(message = "A URL é obrigatória")
		@Size(max = 300, message = "A URL não pode passar de 300 caracteres")
		@Pattern(regexp = "^https?://.+", message = "A URL precisa começar com http:// ou https://")
		String url) {
}
