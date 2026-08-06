package com.generation.blogpessoal.dto.tema;

import com.generation.blogpessoal.model.Tema;

public record TemaResponse(Long id, String descricao) {

	public static TemaResponse de(Tema tema) {
		return new TemaResponse(tema.getId(), tema.getDescricao());
	}

}
