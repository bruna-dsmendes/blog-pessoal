package com.generation.blogpessoal.util;

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

}
