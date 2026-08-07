package com.generation.blogpessoal.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A senha é pedida de novo porque a ação é destrutiva e irreversível. */
public record ExclusaoDeContaRequest(

		@NotBlank(message = "Confirme sua senha para excluir a conta")
		String senha,

		@NotNull(message = "Escolha o que fazer com os artigos publicados")
		@Schema(description = "ANONIMIZAR mantém os artigos sem autor. EXCLUIR apaga tudo.")
		DestinoDosArtigos destinoDosArtigos) {

	public enum DestinoDosArtigos {
		ANONIMIZAR,
		EXCLUIR
	}

}
