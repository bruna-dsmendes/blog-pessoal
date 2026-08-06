package com.generation.blogpessoal.dto.usuario;

import com.generation.blogpessoal.model.Usuario;

/**
 * Não existe campo senha aqui. Esse é o ponto: a entidade Usuario nunca
 * chega ao JSON, então o hash não tem como vazar por descuido.
 */
public record UsuarioResponse(Long id, String nome, String usuario, String foto) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getUsuario(),
				usuario.getFoto());
	}

}
