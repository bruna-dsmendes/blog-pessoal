package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.PerfilPublicoResponse;
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

	@Test
	@DisplayName("09 - Login deve devolver cookie httpOnly")
	void loginDeveDevolverCookieHttpOnly() {

		String setCookie = JwtHelper.obterCookie(testRestTemplate, ADMIN, SENHA);

		assertTrue(setCookie.startsWith("blog_token="), "O cookie deve se chamar blog_token");
		assertTrue(setCookie.contains("HttpOnly"), "O cookie precisa ser HttpOnly");
		assertTrue(setCookie.contains("SameSite=Lax"), "O cookie precisa declarar SameSite");
		assertTrue(setCookie.contains("Path=/"), "O cookie precisa valer para toda a API");
	}

	@Test
	@DisplayName("10 - Deve autenticar só com o cookie, sem header Authorization")
	void deveAutenticarSomenteComCookie() {

		String setCookie = JwtHelper.obterCookie(testRestTemplate, ADMIN, SENHA);

		ResponseEntity<UsuarioResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/me", HttpMethod.GET, JwtHelper.comCookie(setCookie), UsuarioResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals(ADMIN, resposta.getBody().usuario());
	}

	@Test
	@DisplayName("11 - Cookie inválido não deve autenticar")
	void cookieInvalidoNaoAutentica() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/me", HttpMethod.GET,
				JwtHelper.comCookie("blog_token=nao-e-um-jwt"), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
	}

	@Test
	@DisplayName("12 - Logout deve devolver cookie com validade zero")
	void logoutDeveApagarOCookie() {

		ResponseEntity<Void> resposta = testRestTemplate.exchange(
				BASE_URL + "/deslogar", HttpMethod.POST, HttpEntity.EMPTY, Void.class);

		assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());

		String setCookie = resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

		assertNotNull(setCookie);
		assertTrue(setCookie.contains("Max-Age=0"), "O logout precisa expirar o cookie");
	}

	@Test
	@DisplayName("13 - Cadastro deve gerar username a partir do nome")
	void cadastroDeveGerarUsername() {

		UsuarioRequest usuario = TestBuilder.criarUsuario("Ana Clara Souza", "ana_clara@email.com.br", "12345678");

		ResponseEntity<UsuarioResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, new HttpEntity<>(usuario), UsuarioResponse.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertEquals("ana-clara-souza", resposta.getBody().username());
	}

	@Test
	@DisplayName("14 - Perfil público deve ser acessível sem token e sem expor e-mail")
	void perfilPublicoNaoExigeToken() {

		usuarioService.cadastrar(TestBuilder.criarUsuario("Autora Publica", "autora_publica@email.com.br", "12345678"));

		ResponseEntity<String> bruto = testRestTemplate.exchange(
				BASE_URL + "/perfil/autora-publica", HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.OK, bruto.getStatusCode());
		assertFalse(bruto.getBody().contains("autora_publica@email.com.br"),
				"O perfil público não pode expor o e-mail");

		ResponseEntity<PerfilPublicoResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/perfil/autora-publica", HttpMethod.GET, HttpEntity.EMPTY,
				PerfilPublicoResponse.class);

		assertEquals(0, resposta.getBody().artigosPublicados());
		assertEquals(0, resposta.getBody().minutosEscritos());
	}

	@Test
	@DisplayName("15 - Perfil inexistente deve responder 404")
	void perfilInexistenteResponde404() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/perfil/nao-existe", HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
	}

	@Test
	@DisplayName("16 - Não deve permitir username já usado por outra pessoa")
	void naoDeveDuplicarUsername() {

		usuarioService.cadastrar(TestBuilder.criarUsuario("Nome Disputado", "disputa_um@email.com.br", "12345678"));
		usuarioService.cadastrar(TestBuilder.criarUsuario("Outra Pessoa", "disputa_dois@email.com.br", "12345678"));

		String token = JwtHelper.obterToken(testRestTemplate, "disputa_dois@email.com.br", "12345678");

		HttpEntity<?> requisicao = JwtHelper.comToken(
				TestBuilder.atualizarUsuario("Outra Pessoa", "disputa_dois@email.com.br", null, "nome-disputado"),
				token);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/atualizar", HttpMethod.PUT, requisicao, String.class);

		assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
	}

}
