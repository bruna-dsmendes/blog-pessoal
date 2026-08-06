package com.generation.blogpessoal.dto.postagem;

import com.generation.blogpessoal.model.Usuario;

/** O username permite ao front montar o link para o perfil público. */
public record AutorResponse(Long id, String nome, String username, String foto) {

	public static AutorResponse de(Usuario usuario) {
		if (usuario == null) {
			return null;
		}
		return new AutorResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getUsername(),
				usuario.getFoto());
	}

}
