package com.generation.blogpessoal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.repository.TemaRepository;

/**
 * Os temas viraram tags na V3. Isto existe para clientes antigos continuarem
 * lendo durante a migração, e sai junto com a V7.
 *
 * @deprecated use {@link TagService}.
 */
@Deprecated(forRemoval = true)
@Service
public class TemaService {

	private final TemaRepository temaRepository;

	public TemaService(TemaRepository temaRepository) {
		this.temaRepository = temaRepository;
	}

	@Transactional(readOnly = true)
	public Page<TemaResponse> listar(Pageable pageable) {
		return temaRepository.findAll(pageable).map(TemaResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<TemaResponse> buscarPorDescricao(String descricao, Pageable pageable) {
		return temaRepository.findAllByDescricaoContainingIgnoreCase(descricao, pageable)
				.map(TemaResponse::de);
	}

	@Transactional(readOnly = true)
	public TemaResponse buscarPorId(Long id) {
		return temaRepository.findById(id)
				.map(TemaResponse::de)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Tema", id));
	}

}
