package com.generation.blogpessoal.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.dto.usuario.LinkRequest;
import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioAtualizarRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;
import com.generation.blogpessoal.dto.usuario.PerfilPublicoResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.exception.ConflitoException;
import com.generation.blogpessoal.exception.CredenciaisInvalidasException;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.model.LinkPerfil;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.model.TipoLink;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.security.JwtService;

@Service
public class UsuarioService {

	private static final int LIMITE_DE_TAGS_NO_PERFIL = 5;

	private final UsuarioRepository usuarioRepository;
	private final PostagemRepository postagemRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final SlugService slugService;

	public UsuarioService(UsuarioRepository usuarioRepository, PostagemRepository postagemRepository,
			JwtService jwtService, AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder, SlugService slugService) {
		this.usuarioRepository = usuarioRepository;
		this.postagemRepository = postagemRepository;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
		this.slugService = slugService;
	}

	@Transactional(readOnly = true)
	public Page<UsuarioResponse> listar(Pageable pageable) {
		return usuarioRepository.findAll(pageable).map(UsuarioResponse::de);
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(Long id) {
		return usuarioRepository.findById(id)
				.map(UsuarioResponse::de)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorEmail(String email) {
		return UsuarioResponse.de(obterPorEmail(email));
	}

	@Transactional
	public UsuarioResponse cadastrar(UsuarioRequest request) {

		if (usuarioRepository.existsByUsuario(request.usuario())) {
			throw new ConflitoException("Já existe uma conta cadastrada com esse e-mail");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(request.nome());
		usuario.setUsuario(request.usuario());
		usuario.setSenha(passwordEncoder.encode(request.senha()));
		usuario.setFoto(request.foto());

		/*
		 * O username sai do nome em vez de virar mais um campo no cadastro.
		 * Quem quiser trocar, troca depois na edição do perfil.
		 */
		usuario.setUsername(slugService.gerarUnico(request.nome(), usuarioRepository::existsByUsername));

		return UsuarioResponse.de(usuarioRepository.save(usuario));
	}

	/**
	 * Atualiza sempre o usuário do token, nunca um id vindo do corpo.
	 * É o que impede alguém autenticado de editar o perfil de outra pessoa.
	 */
	@Transactional
	public UsuarioResponse atualizar(String emailLogado, UsuarioAtualizarRequest request) {

		Usuario usuario = obterPorEmail(emailLogado);

		boolean trocandoEmail = !usuario.getUsuario().equalsIgnoreCase(request.usuario());

		if (trocandoEmail && usuarioRepository.existsByUsuario(request.usuario())) {
			throw new ConflitoException("Já existe uma conta cadastrada com esse e-mail");
		}

		boolean trocandoUsername = !usuario.getUsername().equals(request.username());

		if (trocandoUsername && usuarioRepository.existsByUsername(request.username())) {
			throw new ConflitoException("Esse nome de usuário já está em uso");
		}

		usuario.setNome(request.nome());
		usuario.setUsuario(request.usuario());
		usuario.setUsername(request.username());
		usuario.setFoto(request.foto());
		usuario.setBio(request.bio());

		aplicarLinks(usuario, request.links());

		// Senha só é re-encodada quando a pessoa realmente enviou uma nova.
		if (request.senha() != null && !request.senha().isBlank()) {
			usuario.setSenha(passwordEncoder.encode(request.senha()));
		}

		return UsuarioResponse.de(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public LoginResponse autenticar(LoginRequest request) {

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.usuario(), request.senha()));
		} catch (AuthenticationException e) {
			// Mensagem genérica de propósito: não revela se o e-mail existe.
			throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
		}

		Usuario usuario = obterPorEmail(request.usuario());

		return new LoginResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getUsuario(),
				usuario.getFoto(),
				jwtService.generateToken(usuario.getUsuario()),
				"Bearer",
				jwtService.calcularExpiracao());
	}

	/**
	 * Perfil de autor, aberto ao público.
	 *
	 * As estatísticas contam só o que está publicado: rascunho é privado, e o
	 * número de rascunhos de alguém não é informação de quem visita.
	 */
	@Transactional(readOnly = true)
	public PerfilPublicoResponse perfilPublico(String username) {

		Usuario autor = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado: " + username));

		long artigos = postagemRepository.countByUsuarioIdAndStatus(autor.getId(), StatusPostagem.PUBLICADO);
		long minutos = postagemRepository.somarTempoLeitura(autor.getId(), StatusPostagem.PUBLICADO);

		List<TagResponse> tags = postagemRepository
				.buscarTagsMaisUsadas(autor.getId(), StatusPostagem.PUBLICADO,
						PageRequest.of(0, LIMITE_DE_TAGS_NO_PERFIL))
				.stream()
				.map(TagResponse::de)
				.toList();

		return PerfilPublicoResponse.de(autor, artigos, minutos, tags);
	}

	/*
	 * Substitui a lista inteira em vez de comparar item a item. Com
	 * orphanRemoval, o que sai da coleção é apagado no flush.
	 */
	private void aplicarLinks(Usuario usuario, List<LinkRequest> novos) {

		usuario.getLinks().clear();

		if (novos == null) {
			return;
		}

		Set<TipoLink> jaAdicionados = new HashSet<>();
		int ordem = 0;

		for (LinkRequest link : novos) {
			if (link.url() != null && !link.url().isBlank() && jaAdicionados.add(link.tipo())) {
				usuario.getLinks().add(new LinkPerfil(usuario, link.tipo(), link.url().trim(), ordem++));
			}
		}
	}

	@Transactional(readOnly = true)
	public Usuario obterPorEmail(String email) {
		return usuarioRepository.findByUsuario(email)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + email));
	}

}
