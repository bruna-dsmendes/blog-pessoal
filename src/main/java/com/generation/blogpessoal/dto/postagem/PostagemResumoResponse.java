package com.generation.blogpessoal.dto.postagem;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.StatusPostagem;

/*
 * Resposta do feed, sem o campo conteudo.
 *
 * Um artigo pode ter 50 mil caracteres. Mandar isso para 10 itens de uma
 * listagem seriam megabytes de JSON que a tela do feed nem usa.
 */
public record PostagemResumoResponse(
		Long id,
		String titulo,
		String subtitulo,
		String slug,
		String capaUrl,
		StatusPostagem status,
		Integer tempoLeitura,
		LocalDateTime publicadoEm,
		LocalDateTime atualizadoEm,
		AutorResponse autor,
		List<TagResponse> tags) {

	public static PostagemResumoResponse de(Postagem p) {
		return new PostagemResumoResponse(
				p.getId(),
				p.getTitulo(),
				p.getSubtitulo(),
				p.getSlug(),
				p.getCapaUrl(),
				p.getStatus(),
				p.getTempoLeitura(),
				p.getPublicadoEm(),
				p.getAtualizadoEm(),
				AutorResponse.de(p.getUsuario()),
				p.getTags().stream()
						.map(TagResponse::de)
						.sorted(Comparator.comparing(TagResponse::nome))
						.toList());
	}

}
