package com.generation.blogpessoal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generation.blogpessoal.dto.usuario.RedefinirSenhaRequest;
import com.generation.blogpessoal.exception.RegraDeNegocioException;
import com.generation.blogpessoal.model.TokenRedefinicaoSenha;
import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.TokenRedefinicaoSenhaRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;

@Service
public class RedefinicaoSenhaService {

	private static final SecureRandom ALEATORIO = new SecureRandom();
	private static final int BYTES_DO_TOKEN = 32;

	private final UsuarioRepository usuarioRepository;
	private final TokenRedefinicaoSenhaRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final String urlDoFront;
	private final long validadeEmMinutos;

	public RedefinicaoSenhaService(UsuarioRepository usuarioRepository,
			TokenRedefinicaoSenhaRepository tokenRepository, PasswordEncoder passwordEncoder,
			EmailService emailService,
			@Value("${app.frontend-url}") String urlDoFront,
			@Value("${app.senha.token-validade-minutos:30}") long validadeEmMinutos) {
		this.usuarioRepository = usuarioRepository;
		this.tokenRepository = tokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
		this.urlDoFront = urlDoFront;
		this.validadeEmMinutos = validadeEmMinutos;
	}

	/**
	 * Não devolve nada e nunca falha por e-mail inexistente.
	 *
	 * Responder diferente para conta existente e inexistente transformaria este
	 * endpoint em uma ferramenta para descobrir quem tem cadastro na plataforma.
	 */
	@Transactional
	public void solicitar(String email) {

		usuarioRepository.findByUsuario(email).ifPresent(usuario -> {

			// Um pedido novo invalida os anteriores.
			tokenRepository.excluirDoUsuario(usuario.getId());

			String token = gerarToken();

			tokenRepository.save(new TokenRedefinicaoSenha(
					usuario,
					hash(token),
					LocalDateTime.now().plusMinutes(validadeEmMinutos)));

			emailService.enviar(
					usuario.getUsuario(),
					"Redefinição de senha",
					montarEmail(usuario.getNome(), token));
		});
	}

	@Transactional
	public void redefinir(RedefinirSenhaRequest request) {

		TokenRedefinicaoSenha token = tokenRepository.buscarPorHash(hash(request.token()))
				.filter(TokenRedefinicaoSenha::estaValido)
				.orElseThrow(() -> new RegraDeNegocioException(
						"Este link é inválido ou já expirou. Solicite um novo."));

		Usuario usuario = token.getUsuario();
		usuario.setSenha(passwordEncoder.encode(request.novaSenha()));

		/*
		 * Como a sessão é um JWT sem estado, trocar a senha não derrubaria quem
		 * já está logado. Esta marca invalida todo token emitido antes daqui,
		 * que é o ponto de pedir redefinição quando a conta foi invadida.
		 */
		usuario.setSenhaAlteradaEm(LocalDateTime.now());

		token.marcarComoUsado();

		usuarioRepository.save(usuario);
		tokenRepository.save(token);
	}

	private String gerarToken() {
		byte[] bytes = new byte[BYTES_DO_TOKEN];
		ALEATORIO.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
		}
	}

	private String montarEmail(String nome, String token) {

		String link = urlDoFront + "/redefinir-senha?token=" + token;

		return """
				<p>Olá, %s.</p>
				<p>Recebemos um pedido para redefinir a senha da sua conta no Simetria.Dev.</p>
				<p><a href="%s">Clique aqui para criar uma nova senha</a></p>
				<p>O link vale por %d minutos e só pode ser usado uma vez.</p>
				<p>Se não foi você quem pediu, ignore este e-mail. Sua senha continua a mesma.</p>
				""".formatted(nome, link, validadeEmMinutos);
	}

}
