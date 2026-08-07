package com.generation.blogpessoal.dto.usuario;

import java.util.List;

import com.generation.blogpessoal.model.Usuario;

/** Sem campo senha: a entidade nunca é serializada, então o hash não vaza. */
public record UsuarioResponse(
		Long id,
		String nome,
		String username,
		String usuario,
		String foto,
		String bio,
		List<LinkResponse> links) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getUsername(),
				usuario.getUsuario(),
				usuario.getFoto(),
				usuario.getBio(),
				usuario.getLinks().stream().map(LinkResponse::de).toList());
	}

}
