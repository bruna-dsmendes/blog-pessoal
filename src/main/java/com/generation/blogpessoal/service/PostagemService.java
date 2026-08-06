package com.generation.blogpessoal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.postagem.PostagemResponse;
import com.generation.blogpessoal.exception.OperacaoNaoPermitidaException;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.Tema;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TemaRepository;

@Service
public class PostagemService {

	private final PostagemRepository postagemRepository;
	private final TemaRepository temaRepository;
	private final UsuarioService usuarioService;

	public PostagemService(PostagemRepository postagemRepository, TemaRepository temaRepository,
			UsuarioService usuarioService) {
		this.postagemRepository = postagemRepository;
		this.temaRepository = temaRepository;
		this.usuarioService = usuarioService;
	}

	@Transactional(readOnly = true)
	public Page<PostagemResponse> listar(Pageable pageable) {
		return postagemRepository.buscarTodasComRelacionamentos(pageable).map(PostagemResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<PostagemResponse> buscarPorTitulo(String titulo, Pageable pageable) {
		return postagemRepository.buscarPorTitulo(titulo, pageable).map(PostagemResponse::de);
	}

	@Transactional(readOnly = true)
	public PostagemResponse buscarPorId(Long id) {
		return PostagemResponse.de(obterPorId(id));
	}

	@Transactional
	public PostagemResponse criar(PostagemRequest request, String emailAutor) {

		Postagem postagem = new Postagem();
		postagem.setTitulo(request.titulo());
		postagem.setTexto(request.texto());
		postagem.setTema(obterTema(request.temaId()));
		postagem.setUsuario(usuarioService.obterPorEmail(emailAutor));

		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public PostagemResponse atualizar(Long id, PostagemRequest request, String emailLogado) {

		Postagem postagem = obterPorId(id);
		validarAutoria(postagem, emailLogado);

		postagem.setTitulo(request.titulo());
		postagem.setTexto(request.texto());
		postagem.setTema(obterTema(request.temaId()));

		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public void excluir(Long id, String emailLogado) {
		Postagem postagem = obterPorId(id);
		validarAutoria(postagem, emailLogado);
		postagemRepository.delete(postagem);
	}

	private void validarAutoria(Postagem postagem, String emailLogado) {

		Usuario autor = postagem.getUsuario();

		if (autor == null || !autor.getUsuario().equalsIgnoreCase(emailLogado)) {
			throw new OperacaoNaoPermitidaException("Você só pode alterar as suas próprias postagens");
		}
	}

	private Postagem obterPorId(Long id) {
		return postagemRepository.buscarPorIdComRelacionamentos(id)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Postagem", id));
	}

	private Tema obterTema(Long temaId) {
		return temaRepository.findById(temaId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Tema", temaId));
	}

}
