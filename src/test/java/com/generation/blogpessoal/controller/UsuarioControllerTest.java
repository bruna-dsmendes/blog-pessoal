package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
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

	private static final String BASE_URL = "/usuarios";
	private static final String ADMIN = "root@root.com";
	private static final String SENHA = "rootroot";

	@BeforeAll
	void start() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Root", ADMIN, SENHA));
	}

	@Test
	@DisplayName("✔ 01 - Deve cadastrar um novo usuário com sucesso")
	void deveCadastrarUsuario() {

		Usuario usuario = TestBuilder.criarUsuario(null, "Paulo Antunes", "paulo_antunes@email.com.br", "12345678");

		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL + "/cadastrar", HttpMethod.POST,
				requisicao, Usuario.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	@Test
	@DisplayName("✔ 02 - Não deve permitir a duplicação do usuário")
	void naoDeveDuplicarUsuario() {

		// Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Maria da Silva", "maria_silva@email.com.br", "12345678");
		usuarioService.cadastrarUsuario(usuario);

		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL + "/cadastrar", HttpMethod.POST,
				requisicao, Usuario.class);

		// Then
		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}

	@Test
	@DisplayName("✔ 03 - Deve atualizar os dados de um usuário com sucesso")
	void deveAtualizarUmUsuario() {

		Usuario usuario = TestBuilder.criarUsuario(null, "Juliana Andrews", "ju_andrews@email.com.br", "12345678");
		Optional<Usuario> cadastrado = usuarioService.cadastrarUsuario(usuario);

		Usuario usuarioUpdate = TestBuilder.criarUsuario(cadastrado.get().getId(), "Juliana Ramos",
				"ju_ramos@email.com.br", "12345678");

		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Usuario> requisicao = JwtHelper.criarRequisicaoComToken(usuarioUpdate, token);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL + "/atualizar", HttpMethod.PUT,
				requisicao, Usuario.class);

		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	@Test
	@DisplayName("✔ 04 - Deve listar todos os usuários com sucesso")
	void deveListarTodosUsuarios() {

		// Given
		usuarioService.cadastrarUsuario(
				TestBuilder.criarUsuario(null, "Bruna Mendes", "bruna_mendes@email.com.br", "12345678"));
		usuarioService.cadastrarUsuario(
				TestBuilder.criarUsuario(null, "Carlos Moura", "carlos_moura@email.com.br", "12345678"));

		// When
		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);
		ResponseEntity<Usuario[]> resposta = testRestTemplate.exchange(BASE_URL + "/all", HttpMethod.GET, requisicao,
				Usuario[].class);

		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	@Test
	@DisplayName("✔ 05 - Deve listar um usuário específico pelo id")
	void deveListarUmUsuarioPorId() throws Exception {

		Usuario usuarioSalvo = usuarioService
				.cadastrarUsuario(TestBuilder.criarUsuario(null, "Ana Marques Id", "ana_id@email.com.br", "12345678"))
				.orElseThrow(() -> new RuntimeException("Erro ao cadastrar usuário de teste!"));

		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);

		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL + "/" + usuarioSalvo.getId(),
				HttpMethod.GET, requisicao, Usuario.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals(usuarioSalvo.getId(), resposta.getBody().getId());
		assertEquals("Ana Marques Id", resposta.getBody().getNome());
	}

	@Test
	@DisplayName("✔ 06 - Deve autenticar um usuário com sucesso")
	void deveAutenticarUsuario() {

		Usuario usuarioParaLogar = TestBuilder.criarUsuario(null, "Bruna Mendes", "bruna_mendes@email.com.br",
				"12345678");
		usuarioService.cadastrarUsuario(usuarioParaLogar);

		// Cria o objeto de login com o mesmo e-mail e senha cadastrados
		Usuario login = TestBuilder.criarUsuario(null, null, "bruna_mendes@email.com.br", "12345678");

		// Faz o POST para o endpoint de login
		HttpEntity<Usuario> requisicao = new HttpEntity<>(login);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL + "/logar", HttpMethod.POST, requisicao,
				Usuario.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());

	}
}
