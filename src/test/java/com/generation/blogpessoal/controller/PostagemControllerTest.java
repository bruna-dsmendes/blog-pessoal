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

import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.postagem.PostagemResponse;
import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TemaRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.TemaService;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.JwtHelper;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PostagemControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private TemaService temaService;

	@Autowired
	private PostagemRepository postagemRepository;

	@Autowired
	private TemaRepository temaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	private static final String BASE_URL = "/postagens";

	private static final String AUTORA = "autora@email.com";
	private static final String INTRUSA = "intrusa@email.com";
	private static final String SENHA = "12345678";

	private Long temaId;

	@BeforeAll
	void start() {
		postagemRepository.deleteAll();
		temaRepository.deleteAll();
		usuarioRepository.deleteAll();

		usuarioService.cadastrar(TestBuilder.criarUsuario("Autora", AUTORA, SENHA));
		usuarioService.cadastrar(TestBuilder.criarUsuario("Intrusa", INTRUSA, SENHA));

		TemaResponse tema = temaService.criar(TestBuilder.criarTema("Java"));
		temaId = tema.id();
	}

	@Test
	@DisplayName("01 - Deve criar uma postagem com o autor vindo do token")
	void deveCriarPostagemComAutorDoToken() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);

		ResponseEntity<PostagemResponse> resposta = criar("Primeira postagem", token);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertNotNull(resposta.getBody().autor());
		assertEquals("Autora", resposta.getBody().autor().nome());
	}

	@Test
	@DisplayName("02 - Não deve criar postagem sem token")
	void naoDeveCriarSemToken() {

		PostagemRequest request = TestBuilder.criarPostagem("Sem token aqui", "Texto qualquer da postagem", temaId);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL, HttpMethod.POST, new HttpEntity<>(request), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
	}

	@Test
	@DisplayName("03 - Não deve permitir editar postagem de outra pessoa")
	void naoDeveEditarPostagemDeOutraPessoa() {

		String tokenAutora = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		Long postagemId = criar("Postagem da autora", tokenAutora).getBody().id();

		String tokenIntrusa = JwtHelper.obterToken(testRestTemplate, INTRUSA, SENHA);
		PostagemRequest tentativa = TestBuilder.criarPostagem("Titulo sequestrado", "Texto alterado por terceiro", temaId);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + postagemId, HttpMethod.PUT,
				JwtHelper.comToken(tentativa, tokenIntrusa), String.class);

		assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
	}

	@Test
	@DisplayName("04 - Não deve permitir excluir postagem de outra pessoa")
	void naoDeveExcluirPostagemDeOutraPessoa() {

		String tokenAutora = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		Long postagemId = criar("Postagem protegida", tokenAutora).getBody().id();

		String tokenIntrusa = JwtHelper.obterToken(testRestTemplate, INTRUSA, SENHA);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + postagemId, HttpMethod.DELETE,
				JwtHelper.comToken(tokenIntrusa), String.class);

		assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
		assertEquals(true, postagemRepository.existsById(postagemId));
	}

	@Test
	@DisplayName("05 - Deve permitir que a autora edite a própria postagem")
	void deveEditarAPropriaPostagem() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		Long postagemId = criar("Postagem para editar", token).getBody().id();

		PostagemRequest atualizacao = TestBuilder.criarPostagem("Titulo atualizado", "Texto atualizado da postagem", temaId);

		ResponseEntity<PostagemResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + postagemId, HttpMethod.PUT,
				JwtHelper.comToken(atualizacao, token), PostagemResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals("Titulo atualizado", resposta.getBody().titulo());
	}

	@Test
	@DisplayName("06 - Deve recusar postagem com tema inexistente")
	void deveRecusarTemaInexistente() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		PostagemRequest request = TestBuilder.criarPostagem("Tema fantasma", "Texto valido da postagem", 999999L);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL, HttpMethod.POST, JwtHelper.comToken(request, token), String.class);

		assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
	}

	@Test
	@DisplayName("07 - Deve listar postagens sem exigir autenticação")
	void deveListarPostagensSemToken() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "?page=0&size=5", HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	private ResponseEntity<PostagemResponse> criar(String titulo, String token) {
		PostagemRequest request = TestBuilder.criarPostagem(titulo, "Texto da postagem de teste", temaId);
		return testRestTemplate.exchange(
				BASE_URL, HttpMethod.POST, JwtHelper.comToken(request, token), PostagemResponse.class);
	}

}
