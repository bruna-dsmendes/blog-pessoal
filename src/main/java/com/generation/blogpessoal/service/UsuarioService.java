package com.generation.blogpessoal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioAtualizarRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.exception.ConflitoException;
import com.generation.blogpessoal.exception.CredenciaisInvalidasException;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.security.JwtService;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, JwtService jwtService,
			AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
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

		usuario.setNome(request.nome());
		usuario.setUsuario(request.usuario());
		usuario.setFoto(request.foto());

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

	@Transactional(readOnly = true)
	public Usuario obterPorEmail(String email) {
		return usuarioRepository.findByUsuario(email)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + email));
	}

}
