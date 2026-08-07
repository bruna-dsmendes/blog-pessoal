package com.generation.blogpessoal.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.postagem.ReacaoResponse;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.Reacao;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.model.TipoReacao;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.ReacaoRepository;

@Service
public class ReacaoService {

	private final ReacaoRepository reacaoRepository;
	private final PostagemRepository postagemRepository;
	private final UsuarioService usuarioService;

	public ReacaoService(ReacaoRepository reacaoRepository, PostagemRepository postagemRepository,
			UsuarioService usuarioService) {
		this.reacaoRepository = reacaoRepository;
		this.postagemRepository = postagemRepository;
		this.usuarioService = usuarioService;
	}

	@Transactional
	public ReacaoResponse reagir(Long postagemId, String emailLogado) {

		Postagem postagem = obterPublicada(postagemId);
		Usuario usuario = usuarioService.obterPorEmail(emailLogado);

		if (!reacaoRepository.existsByPostagemIdAndUsuarioId(postagemId, usuario.getId())) {
			try {
				reacaoRepository.save(new Reacao(postagem, usuario, TipoReacao.CURTIR));
			} catch (DataIntegrityViolationException e) {
				
			}
		}

		return estado(postagemId, usuario.getId());
	}

	@Transactional
	public ReacaoResponse desfazer(Long postagemId, String emailLogado) {

		Usuario usuario = usuarioService.obterPorEmail(emailLogado);

		reacaoRepository.findByPostagemIdAndUsuarioId(postagemId, usuario.getId())
				.ifPresent(reacaoRepository::delete);

		return estado(postagemId, usuario.getId());
	}

	@Transactional(readOnly = true)
	public ReacaoResponse estado(Long postagemId, Long usuarioId) {

		long total = reacaoRepository.countByPostagemId(postagemId);

		boolean reagi = usuarioId != null
				&& reacaoRepository.existsByPostagemIdAndUsuarioId(postagemId, usuarioId);

		return new ReacaoResponse(total, reagi);
	}

	/** Rascunho não recebe reação: ele nem deveria ser visível para terceiros. */
	private Postagem obterPublicada(Long postagemId) {

		Postagem postagem = postagemRepository.findById(postagemId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Postagem", postagemId));

		if (postagem.getStatus() == StatusPostagem.RASCUNHO) {
			throw new RecursoNaoEncontradoException("Postagem não encontrada");
		}

		return postagem;
	}

}
