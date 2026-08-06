package com.generation.blogpessoal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.tema.TemaRequest;
import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.exception.RegraDeNegocioException;
import com.generation.blogpessoal.model.Tema;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TemaRepository;

@Service
public class TemaService {

	private final TemaRepository temaRepository;
	private final PostagemRepository postagemRepository;

	public TemaService(TemaRepository temaRepository, PostagemRepository postagemRepository) {
		this.temaRepository = temaRepository;
		this.postagemRepository = postagemRepository;
	}

	@Transactional(readOnly = true)
	public Page<TemaResponse> listar(Pageable pageable) {
		return temaRepository.findAll(pageable).map(TemaResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<TemaResponse> buscarPorDescricao(String descricao, Pageable pageable) {
		return temaRepository.findAllByDescricaoContainingIgnoreCase(descricao, pageable).map(TemaResponse::de);
	}

	@Transactional(readOnly = true)
	public TemaResponse buscarPorId(Long id) {
		return TemaResponse.de(obterPorId(id));
	}

	@Transactional
	public TemaResponse criar(TemaRequest request) {
		Tema tema = new Tema();
		tema.setDescricao(request.descricao());
		return TemaResponse.de(temaRepository.save(tema));
	}

	@Transactional
	public TemaResponse atualizar(Long id, TemaRequest request) {
		Tema tema = obterPorId(id);
		tema.setDescricao(request.descricao());
		return TemaResponse.de(temaRepository.save(tema));
	}

	@Transactional
	public void excluir(Long id) {

		Tema tema = obterPorId(id);
		long vinculadas = postagemRepository.countByTemaId(id);

		if (vinculadas > 0) {
			throw new RegraDeNegocioException(
					"Não é possível excluir: existem " + vinculadas + " postagem(ns) usando esse tema");
		}

		temaRepository.delete(tema);
	}

	private Tema obterPorId(Long id) {
		return temaRepository.findById(id)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Tema", id));
	}

}
