package com.generation.blogpessoal.dto.usuario;

import com.generation.blogpessoal.model.Usuario;

/**
 * Não existe campo senha aqui. Esse é o ponto: a entidade Usuario nunca
 * chega, então o hash não tem como vazar por descuido.
 *
 * Inclui o e-mail porque só o próprio dono vê essa resposta.
 */
public record UsuarioResponse(
		Long id,
		String nome,
		String username,
		String usuario,
		String foto,
		String bio,
		String linkGithub,
		String linkLinkedin) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getUsername(),
				usuario.getUsuario(),
				usuario.getFoto(),
				usuario.getBio(),
				usuario.getLinkGithub(),
				usuario.getLinkLinkedin());
	}

}
