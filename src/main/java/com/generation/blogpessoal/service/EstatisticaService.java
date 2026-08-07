package com.generation.blogpessoal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.estatisticas.EstatisticasResponse;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.repository.PostagemRepository;

@Service
public class EstatisticaService {

	private final PostagemRepository postagemRepository;

	public EstatisticaService(PostagemRepository postagemRepository) {
		this.postagemRepository = postagemRepository;
	}

	@Transactional(readOnly = true)
	public EstatisticasResponse daPlataforma() {

		StatusPostagem publicado = StatusPostagem.PUBLICADO;

		return new EstatisticasResponse(
				postagemRepository.countByStatus(publicado),
				postagemRepository.contarAutoresComPublicacao(publicado),
				postagemRepository.contarTagsEmUso(publicado),
				postagemRepository.somarTempoLeituraDaPlataforma(publicado));
	}

}
