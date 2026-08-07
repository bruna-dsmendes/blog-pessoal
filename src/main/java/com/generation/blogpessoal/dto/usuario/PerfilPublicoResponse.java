package com.generation.blogpessoal.dto.usuario;

import java.util.List;

import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.model.Usuario;

/** Página aberta, por isso sem e-mail. */
public record PerfilPublicoResponse(
		String username,
		String nome,
		String foto,
		String bio,
		String linkGithub,
		String linkLinkedin,
		long artigosPublicados,
		long minutosEscritos,
		List<TagResponse> tagsMaisUsadas) {

	public static PerfilPublicoResponse de(Usuario usuario, long artigos, long minutos,
			List<TagResponse> tags) {

		return new PerfilPublicoResponse(
				usuario.getUsername(),
				usuario.getNome(),
				usuario.getFoto(),
				usuario.getBio(),
				usuario.getLinkGithub(),
				usuario.getLinkLinkedin(),
				artigos,
				minutos,
				tags);
	}

}
