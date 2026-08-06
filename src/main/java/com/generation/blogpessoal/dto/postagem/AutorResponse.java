package com.generation.blogpessoal.dto.postagem;

import com.generation.blogpessoal.model.Usuario;

public record AutorResponse(Long id, String nome, String foto) {

	public static AutorResponse de(Usuario usuario) {
		if (usuario == null) {
			return null;
		}
		return new AutorResponse(usuario.getId(), usuario.getNome(), usuario.getFoto());
	}

}
