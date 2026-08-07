package com.generation.blogpessoal.dto.postagem;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.StatusPostagem;

/** Resposta completa, com o markdown. Usada na leitura de um artigo. */
public record PostagemResponse(
		Long id,
		String titulo,
		String subtitulo,
		String conteudo,
		String slug,
		String capaUrl,
		StatusPostagem status,
		Integer tempoLeitura,
		LocalDateTime criadoEm,
		LocalDateTime atualizadoEm,
		LocalDateTime publicadoEm,
		AutorResponse autor,
		List<TagResponse> tags,
		long reacoes,
		boolean reagi) {

	/** Usada onde o estado das reações não é relevante, como logo após salvar. */
	public static PostagemResponse de(Postagem p) {
		return de(p, 0, false);
	}

	public static PostagemResponse de(Postagem p, long reacoes, boolean reagi) {
		return new PostagemResponse(
				p.getId(),
				p.getTitulo(),
				p.getSubtitulo(),
				p.getConteudo(),
				p.getSlug(),
				p.getCapaUrl(),
				p.getStatus(),
				p.getTempoLeitura(),
				p.getCriadoEm(),
				p.getAtualizadoEm(),
				p.getPublicadoEm(),
				AutorResponse.de(p.getUsuario()),
				p.getTags().stream()
						.map(TagResponse::de)
						.sorted(Comparator.comparing(TagResponse::nome))
						.toList(),
				reacoes,
				reagi);
	}

}
