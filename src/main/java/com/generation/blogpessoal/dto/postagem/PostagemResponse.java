package com.generation.blogpessoal.dto.postagem;

import java.time.LocalDateTime;

import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.model.Postagem;

public record PostagemResponse(
		Long id,
		String titulo,
		String texto,
		LocalDateTime data,
		LocalDateTime atualizadoEm,
		TemaResponse tema,
		AutorResponse autor) {

	public static PostagemResponse de(Postagem postagem) {
		return new PostagemResponse(
				postagem.getId(),
				postagem.getTitulo(),
				postagem.getTexto(),
				postagem.getData(),
				postagem.getAtualizadoEm(),
				postagem.getTema() == null ? null : TemaResponse.de(postagem.getTema()),
				AutorResponse.de(postagem.getUsuario()));
	}

}
