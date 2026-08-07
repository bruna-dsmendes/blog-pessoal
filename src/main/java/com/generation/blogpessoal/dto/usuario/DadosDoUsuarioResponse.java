package com.generation.blogpessoal.dto.usuario;

import java.time.Instant;
import java.util.List;

import com.generation.blogpessoal.dto.postagem.PostagemResponse;

/**
 * Exportação completa, para o direito de portabilidade do art. 18, V da LGPD.
 * Inclui rascunhos, que não aparecem em nenhuma resposta pública.
 */
public record DadosDoUsuarioResponse(
		Instant geradoEm,
		UsuarioResponse perfil,
		List<PostagemResponse> artigos,
		List<ReacaoRegistrada> reacoes) {

	public record ReacaoRegistrada(String artigo, String slug, String tipo, String data) {
	}

}
