package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.JwtHelper;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PostagemRepository postagemRepository;

	private static final String BASE_URL = "/usuarios";
	private static final String ADMIN = "root@root.com";
	private static final String SENHA = "rootroot";

	@BeforeAll
	void start() {
		postagemRepository.deleteAll();
		usuarioRepository.deleteAll();
		usuarioService.cadastrar(TestBuilder.criarUsuario("Root", ADMIN, SENHA));
	}

	@Test
	@DisplayName("01 - Deve cadastrar um novo usuário com sucesso")
	void deveCadastrarUsuario() {

		UsuarioRequest usuario = TestBuilder.criarUsuario("Paulo Antunes", "paulo_antunes@email.com.br", "12345678");

		ResponseEntity<UsuarioResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, new HttpEntity<>(usuario), UsuarioResponse.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertNotNull(resposta.getBody().id());
	}

	@Test
	@DisplayName("02 - Não deve permitir a duplicação do usuário")
	void naoDeveDuplicarUsuario() {

		UsuarioRequest usuario = TestBuilder.criarUsuario("Maria da Silva", "maria_silva@email.com.br", "12345678");
		usuarioService.cadastrar(usuario);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, new HttpEntity<>(usuario), String.class);

		assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
	}

	@Test
	@DisplayName("03 - Deve recusar cadastro com e-mail inválido")
	void deveRecusarEmailInvalido() {

		UsuarioRequest usuario = TestBuilder.criarUsuario("Sem Email", "nao-e-um-email", "12345678");

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, new HttpEntity<>(usuario), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}

	@Test
	@DisplayName("04 - Deve atualizar o próprio perfil com sucesso")
	void deveAtualizarOProprioPerfil() {

		usuarioService.cadastrar(TestBuilder.criarUsuario("Juliana Andrews", "ju_andrews@email.com.br", "12345678"));
		String token = JwtHelper.obterToken(testRestTemplate, "ju_andrews@email.com.br", "12345678");

		HttpEntity<?> requisicao = JwtHelper.comToken(
				TestBuilder.atualizarUsuario("Juliana Ramos", "ju_andrews@email.com.br", null), token);

		ResponseEntity<UsuarioResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/atualizar", HttpMethod.PUT, requisicao, UsuarioResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals("Juliana Ramos", resposta.getBody().nome());
	}

	@Test
	@DisplayName("05 - Deve continuar logando depois de atualizar sem enviar senha")
	void deveManterASenhaQuandoNaoEnviada() {

		usuarioService.cadastrar(TestBuilder.criarUsuario("Carlos Moura", "carlos_moura@email.com.br", "12345678"));
		String token = JwtHelper.obterToken(testRestTemplate, "carlos_moura@email.com.br", "12345678");

		testRestTemplate.exchange(BASE_URL + "/atualizar", HttpMethod.PUT,
				JwtHelper.comToken(TestBuilder.atualizarUsuario("Carlos M.", "carlos_moura@email.com.br", null), token),
				UsuarioResponse.class);

		ResponseEntity<LoginResponse> login = testRestTemplate.exchange(
				BASE_URL + "/logar", HttpMethod.POST,
				new HttpEntity<>(TestBuilder.criarLogin("carlos_moura@email.com.br", "12345678")),
				LoginResponse.class);

		assertEquals(HttpStatus.OK, login.getStatusCode());
	}

	@Test
	@DisplayName("06 - Deve listar usuários sem expor a senha")
	void deveListarUsuariosSemSenha() {

		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/all", HttpMethod.GET, JwtHelper.comToken(token), String.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		org.junit.jupiter.api.Assertions.assertFalse(resposta.getBody().contains("senha"),
				"A resposta não pode conter o campo senha");
	}

	@Test
	@DisplayName("07 - Deve autenticar e devolver um token")
	void deveAutenticarUsuario() {

		ResponseEntity<LoginResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/logar", HttpMethod.POST,
				new HttpEntity<>(TestBuilder.criarLogin(ADMIN, SENHA)), LoginResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertNotNull(resposta.getBody().token());
		assertEquals("Bearer", resposta.getBody().tipo());
	}

	@Test
	@DisplayName("08 - Não deve autenticar com senha errada")
	void naoDeveAutenticarComSenhaErrada() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/logar", HttpMethod.POST,
				new HttpEntity<>(TestBuilder.criarLogin(ADMIN, "senha-errada")), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
	}

}
