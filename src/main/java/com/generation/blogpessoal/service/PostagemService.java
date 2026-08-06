package com.generation.blogpessoal.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.postagem.PostagemResponse;
import com.generation.blogpessoal.dto.postagem.PostagemResumoResponse;
import com.generation.blogpessoal.exception.OperacaoNaoPermitidaException;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.exception.RegraDeNegocioException;
import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.PostagemRepository;

@Service
public class PostagemService {

	private static final int PALAVRAS_POR_MINUTO = 200;

	private final PostagemRepository postagemRepository;
	private final UsuarioService usuarioService;
	private final TagService tagService;
	private final SlugService slugService;

	public PostagemService(PostagemRepository postagemRepository, UsuarioService usuarioService,
			TagService tagService, SlugService slugService) {
		this.postagemRepository = postagemRepository;
		this.usuarioService = usuarioService;
		this.tagService = tagService;
		this.slugService = slugService;
	}

	// ---------------------------------------------------------------- leitura

	@Transactional(readOnly = true)
	public Page<PostagemResumoResponse> feed(Pageable pageable) {
		return postagemRepository.buscarPorStatus(StatusPostagem.PUBLICADO, pageable)
				.map(PostagemResumoResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<PostagemResumoResponse> porTag(String slugTag, Pageable pageable) {
		return postagemRepository.buscarPorTag(slugTag, StatusPostagem.PUBLICADO, pageable)
				.map(PostagemResumoResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<PostagemResumoResponse> buscar(String termo, Pageable pageable) {
		return postagemRepository.buscarPorTermo(termo, StatusPostagem.PUBLICADO, pageable)
				.map(PostagemResumoResponse::de);
	}

	/** Listagem do autor, incluindo rascunhos. Exige autenticação no controller. */
	@Transactional(readOnly = true)
	public Page<PostagemResumoResponse> minhas(String emailAutor, StatusPostagem status, Pageable pageable) {

		Page<Postagem> pagina = status == null
				? postagemRepository.buscarDoAutor(emailAutor, pageable)
				: postagemRepository.buscarDoAutorPorStatus(emailAutor, status, pageable);

		return pagina.map(PostagemResumoResponse::de);
	}

	@Transactional(readOnly = true)
	public PostagemResponse porId(Long id, String emailLogado) {
		Postagem postagem = postagemRepository.buscarPorIdCompleta(id)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Postagem", id));

		validarVisibilidade(postagem, emailLogado);
		return PostagemResponse.de(postagem);
	}

	@Transactional(readOnly = true)
	public PostagemResponse porSlug(String slug, String emailLogado) {
		Postagem postagem = postagemRepository.buscarPorSlugCompleta(slug)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Postagem não encontrada: " + slug));

		validarVisibilidade(postagem, emailLogado);
		return PostagemResponse.de(postagem);
	}

	// ------------------------------------------------------------------ escrita

	/** Toda postagem nasce como rascunho. Publicar é um ato explícito. */
	@Transactional
	public PostagemResponse criar(PostagemRequest request, String emailAutor) {

		Usuario autor = usuarioService.obterPorEmail(emailAutor);

		Postagem postagem = new Postagem();
		postagem.setUsuario(autor);
		postagem.setStatus(StatusPostagem.RASCUNHO);
		postagem.setSlug(slugService.gerarUnico(request.titulo(), postagemRepository::existsBySlug));

		aplicarConteudo(postagem, request);

		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public PostagemResponse atualizar(Long id, PostagemRequest request, String emailLogado) {

		Postagem postagem = obterDoAutor(id, emailLogado);

		/*
		 * O slug só acompanha o título enquanto a postagem é rascunho.
		 * Depois de publicada, mudar o slug quebraria o link que já circulou.
		 */
		if (postagem.getStatus() == StatusPostagem.RASCUNHO
				&& !postagem.getTitulo().equals(request.titulo())) {
			postagem.setSlug(slugService.gerarUnico(request.titulo(), postagemRepository::existsBySlug));
		}

		aplicarConteudo(postagem, request);

		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public void excluir(Long id, String emailLogado) {
		postagemRepository.delete(obterDoAutor(id, emailLogado));
	}

	// ------------------------------------------------------------ ciclo de vida

	@Transactional
	public PostagemResponse publicar(Long id, String emailLogado) {

		Postagem postagem = obterDoAutor(id, emailLogado);

		if (postagem.getStatus() == StatusPostagem.PUBLICADO) {
			throw new RegraDeNegocioException("Esta postagem já está publicada");
		}

		postagem.setStatus(StatusPostagem.PUBLICADO);

		// Congela na primeira publicação: republicar não reordena o feed.
		if (postagem.getPublicadoEm() == null) {
			postagem.setPublicadoEm(LocalDateTime.now());
		}

		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public PostagemResponse arquivar(Long id, String emailLogado) {
		Postagem postagem = obterDoAutor(id, emailLogado);
		postagem.setStatus(StatusPostagem.ARQUIVADO);
		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	@Transactional
	public PostagemResponse voltarParaRascunho(Long id, String emailLogado) {
		Postagem postagem = obterDoAutor(id, emailLogado);
		postagem.setStatus(StatusPostagem.RASCUNHO);
		return PostagemResponse.de(postagemRepository.save(postagem));
	}

	// -------------------------------------------------------------------- apoio

	private void aplicarConteudo(Postagem postagem, PostagemRequest request) {
		postagem.setTitulo(request.titulo());
		postagem.setSubtitulo(request.subtitulo());
		postagem.setConteudo(request.conteudo());
		postagem.setCapaUrl(request.capaUrl());
		postagem.setTempoLeitura(calcularTempoLeitura(request.conteudo()));
		postagem.setTags(tagService.resolver(request.tags()));
	}

	private int calcularTempoLeitura(String conteudo) {

		if (conteudo == null || conteudo.isBlank()) {
			return 1;
		}

		int palavras = conteudo.trim().split("\\s+").length;
		return Math.max(1, (int) Math.ceil((double) palavras / PALAVRAS_POR_MINUTO));
	}

	private Postagem obterDoAutor(Long id, String emailLogado) {

		Postagem postagem = postagemRepository.buscarPorIdCompleta(id)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Postagem", id));

		Usuario autor = postagem.getUsuario();

		if (autor == null || !autor.getUsuario().equalsIgnoreCase(emailLogado)) {
			throw new OperacaoNaoPermitidaException("Você só pode alterar as suas próprias postagens");
		}

		return postagem;
	}

	/*
	 * Rascunho responde 404 para quem não é o autor, e não 403.
	 * Um 403 confirmaria que o recurso existe, o que já é informação: daria para
	 * varrer ids e descobrir quantos rascunhos alguém tem.
	 */
	private void validarVisibilidade(Postagem postagem, String emailLogado) {

		if (postagem.getStatus() != StatusPostagem.RASCUNHO) {
			return;
		}

		Usuario autor = postagem.getUsuario();
		boolean ehAutor = emailLogado != null && autor != null
				&& autor.getUsuario().equalsIgnoreCase(emailLogado);

		if (!ehAutor) {
			throw new RecursoNaoEncontradoException("Postagem não encontrada");
		}
	}

}
