package com.generation.blogpessoal.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Monta e lê o cookie de autenticação.
 *
 * O ponto do httpOnly é que o JavaScript da página não consegue ler esse valor.
 * Com o token em localStorage, qualquer XSS rouba a sessão inteira. Aqui, mesmo
 * um script injetado não tem como extrair o token: ele consegue no máximo fazer
 * requisições enquanto a pessoa está na página.
 */
@Component
public class AuthCookieService {

	private final String nome;
	private final boolean secure;
	private final String sameSite;
	private final Duration duracao;

	public AuthCookieService(
			@Value("${app.cookie.name:blog_token}") String nome,
			@Value("${app.cookie.secure:true}") boolean secure,
			@Value("${app.cookie.same-site:Lax}") String sameSite,
			@Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {

		this.nome = nome;
		this.secure = secure;
		this.sameSite = sameSite;
		this.duracao = Duration.ofMinutes(expirationMinutes);
	}

	public ResponseCookie criar(String token) {
		return montar(token, duracao);
	}

	/** Cookie vazio com validade zero: é assim que se apaga um cookie. */
	public ResponseCookie limpar() {
		return montar("", Duration.ZERO);
	}

	public Optional<String> extrair(HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return Optional.empty();
		}

		return Arrays.stream(cookies)
				.filter(cookie -> nome.equals(cookie.getName()))
				.map(Cookie::getValue)
				.filter(valor -> valor != null && !valor.isBlank())
				.findFirst();
	}

	public String getNome() {
		return nome;
	}

	private ResponseCookie montar(String valor, Duration maxAge) {
		return ResponseCookie.from(nome, valor)
				.httpOnly(true)      // fora do alcance do document.cookie
				.secure(secure)      // só trafega em HTTPS (false apenas em dev)
				.sameSite(sameSite)  // Lax já barra POST vindo de outro site: é a defesa contra CSRF
				.path("/")
				.maxAge(maxAge)
				.build();
	}

}
