package com.generation.blogpessoal.util;

import java.util.List;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.LoginResponse;

public class JwtHelper {

	private JwtHelper() {
	}

	public static String obterToken(TestRestTemplate testRestTemplate, String email, String senha) {

		LoginRequest login = TestBuilder.criarLogin(email, senha);
		HttpEntity<LoginRequest> requisicao = new HttpEntity<>(login);

		ResponseEntity<LoginResponse> resposta = testRestTemplate
				.exchange("/usuarios/logar", HttpMethod.POST, requisicao, LoginResponse.class);

		LoginResponse corpo = resposta.getBody();

		if (corpo != null && corpo.token() != null) {
			return corpo.token();
		}

		throw new IllegalStateException("Falha no login de teste: " + email);
	}

	public static <T> HttpEntity<T> comToken(T corpo, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(corpo, headers);
	}

	public static HttpEntity<Void> comToken(String token) {
		return comToken(null, token);
	}

	/** Faz login e devolve o valor bruto do cabeçalho Set-Cookie. */
	public static String obterCookie(TestRestTemplate testRestTemplate, String email, String senha) {

		LoginRequest login = TestBuilder.criarLogin(email, senha);

		ResponseEntity<LoginResponse> resposta = testRestTemplate
				.exchange("/usuarios/logar", HttpMethod.POST, new HttpEntity<>(login), LoginResponse.class);

		List<String> cookies = resposta.getHeaders().get(HttpHeaders.SET_COOKIE);

		if (cookies == null || cookies.isEmpty()) {
			throw new IllegalStateException("O login não devolveu Set-Cookie");
		}

		return cookies.get(0);
	}

	/** Monta a requisição mandando só o cookie, sem header Authorization. */
	public static <T> HttpEntity<T> comCookie(T corpo, String setCookie) {

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, setCookie.split(";", 2)[0]);

		return new HttpEntity<>(corpo, headers);
	}

	public static HttpEntity<Void> comCookie(String setCookie) {
		return comCookie(null, setCookie);
	}

}
