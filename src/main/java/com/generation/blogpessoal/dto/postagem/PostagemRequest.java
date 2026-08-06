package com.generation.blogpessoal.dto.postagem;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * Não existe campo status aqui de propósito. Publicar não é editar: o status
 * muda por endpoints próprios, o que evita despublicar um artigo sem querer ao
 * salvar uma correção de vírgula.
 */
public record PostagemRequest(

		@NotBlank(message = "O título é obrigatório")
		@Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
		String titulo,

		@Size(max = 200, message = "O subtítulo não pode passar de 200 caracteres")
		String subtitulo,

		@NotBlank(message = "O conteúdo é obrigatório")
		@Size(min = 10, max = 50000, message = "O conteúdo deve ter entre 10 e 50000 caracteres")
		String conteudo,

		@Size(max = 1000, message = "O link da capa não pode passar de 1000 caracteres")
		String capaUrl,

		@Size(max = 5, message = "Use no máximo 5 tags")
		List<@NotBlank @Size(min = 2, max = 50) String> tags) {
}
