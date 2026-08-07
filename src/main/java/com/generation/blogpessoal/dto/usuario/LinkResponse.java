package com.generation.blogpessoal.dto.usuario;

import com.generation.blogpessoal.model.LinkPerfil;
import com.generation.blogpessoal.model.TipoLink;

public record LinkResponse(TipoLink tipo, String url) {

	public static LinkResponse de(LinkPerfil link) {
		return new LinkResponse(link.getTipo(), link.getUrl());
	}

}
