package com.generation.blogpessoal.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.postagem.PostagemResponse;
import com.generation.blogpessoal.dto.usuario.DadosDoUsuarioResponse;
import com.generation.blogpessoal.dto.usuario.DadosDoUsuarioResponse.ReacaoRegistrada;
import com.generation.blogpessoal.dto.usuario.ExclusaoDeContaRequest;
import com.generation.blogpessoal.dto.usuario.ExclusaoDeContaRequest.DestinoDosArtigos;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.exception.CredenciaisInvalidasException;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.ReacaoRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;

/**
 * Direitos do titular previstos no art. 18 da LGPD: portabilidade dos dados e
 * eliminação da conta.
 */
@Service
public class ContaService {

	private final UsuarioRepository usuarioRepository;
	private final PostagemRepository postagemRepository;
	private final ReacaoRepository reacaoRepository;
	private final UsuarioService usuarioService;
	private final PasswordEncoder passwordEncoder;

	public ContaService(UsuarioRepository usuarioRepository, PostagemRepository postagemRepository,
			ReacaoRepository reacaoRepository, UsuarioService usuarioService,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.postagemRepository = postagemRepository;
		this.reacaoRepository = reacaoRepository;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public DadosDoUsuarioResponse exportar(String emailLogado) {

		Usuario usuario = usuarioService.obterPorEmail(emailLogado);

		List<PostagemResponse> artigos = postagemRepository.buscarTodasDoUsuario(usuario.getId())
				.stream()
				.map(PostagemResponse::de)
				.toList();

		List<ReacaoRegistrada> reacoes = reacaoRepository.findAllByUsuarioId(usuario.getId())
				.stream()
				.map(reacao -> new ReacaoRegistrada(
						reacao.getPostagem().getTitulo(),
						reacao.getPostagem().getSlug(),
						reacao.getTipo().name(),
						reacao.getCriadoEm() == null
								? null
								: reacao.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
				.toList();

		return new DadosDoUsuarioResponse(Instant.now(), UsuarioResponse.de(usuario), artigos, reacoes);
	}

	@Transactional
	public void excluir(String emailLogado, ExclusaoDeContaRequest request) {

		Usuario usuario = usuarioService.obterPorEmail(emailLogado);

		if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
			throw new CredenciaisInvalidasException("Senha incorreta");
		}

		Long id = usuario.getId();

		// As reações que a pessoa deu somem em qualquer um dos dois caminhos.
		reacaoRepository.excluirDoUsuario(id);

		if (request.destinoDosArtigos() == DestinoDosArtigos.EXCLUIR) {
			List<Postagem> artigos = postagemRepository.buscarTodasDoUsuario(id);

			/*
			 * deleteAll em vez de DELETE em massa: só assim o Hibernate limpa as
			 * linhas da tabela de junção com as tags.
			 */
			reacaoRepository.excluirDasPostagensDoUsuario(id);
			postagemRepository.deleteAll(artigos);

		} else {
			postagemRepository.desvincularAutor(id);
		}

		usuarioRepository.delete(usuario);
	}

}
