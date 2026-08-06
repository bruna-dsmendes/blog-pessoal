package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TagRepository;
import com.generation.blogpessoal.repository.UsuarioRepository;
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
	private PostagemRepository postagemRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	private static final String BASE_URL = "/postagens";
	private static final String AUTORA = "autora@email.com";
	private static final String INTRUSA = "intrusa@email.com";
	private static final String SENHA = "12345678";

	@BeforeAll
	void start() {
		postagemRepository.deleteAll();
		tagRepository.deleteAll();
		usuarioRepository.deleteAll();

		usuarioService.cadastrar(TestBuilder.criarUsuario("Autora", AUTORA, SENHA));
		usuarioService.cadastrar(TestBuilder.criarUsuario("Intrusa", INTRUSA, SENHA));
	}

	@Test
	@DisplayName("01 - Deve criar a postagem como rascunho, com slug e tempo de leitura")
	void deveCriarComoRascunho() {

		PostagemResponse corpo = criar("Como usar Java 21 na prática!").getBody();

		assertNotNull(corpo);
		assertEquals(StatusPostagem.RASCUNHO, corpo.status());
		assertEquals("como-usar-java-21-na-pratica", corpo.slug());
		assertTrue(corpo.tempoLeitura() >= 1);
		assertEquals(null, corpo.publicadoEm());
	}

	@Test
	@DisplayName("02 - Deve gerar slug diferente para títulos iguais")
	void deveGerarSlugUnico() {

		String primeiro = criar("Titulo repetido de teste").getBody().slug();
		String segundo = criar("Titulo repetido de teste").getBody().slug();

		assertNotEquals(primeiro, segundo);
		assertTrue(segundo.startsWith(primeiro));
	}

	@Test
	@DisplayName("03 - Rascunho não deve aparecer no feed público")
	void rascunhoNaoApareceNoFeed() {

		String slug = criar("Rascunho invisivel no feed").getBody().slug();

		ResponseEntity<String> feed = testRestTemplate.exchange(
				BASE_URL, HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.OK, feed.getStatusCode());
		assertTrue(!feed.getBody().contains(slug), "O feed público não pode listar rascunho");
	}

	@Test
	@DisplayName("04 - Rascunho de outra pessoa deve responder 404, não 403")
	void rascunhoDeOutraPessoaResponde404() {

		Long id = criar("Rascunho protegido da autora").getBody().id();
		String tokenIntrusa = JwtHelper.obterToken(testRestTemplate, INTRUSA, SENHA);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + id, HttpMethod.GET, JwtHelper.comToken(tokenIntrusa), String.class);

		assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
	}

	@Test
	@DisplayName("05 - Deve publicar e gravar a data de publicação")
	void devePublicar() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		Long id = criar("Artigo que sera publicado").getBody().id();

		ResponseEntity<PostagemResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + id + "/publicar", HttpMethod.PATCH,
				JwtHelper.comToken(token), PostagemResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals(StatusPostagem.PUBLICADO, resposta.getBody().status());
		assertNotNull(resposta.getBody().publicadoEm());
	}

	@Test
	@DisplayName("06 - Slug deve congelar depois de publicado")
	void slugCongelaAposPublicar() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		PostagemResponse criada = criar("Titulo original antes de publicar").getBody();

		testRestTemplate.exchange(BASE_URL + "/" + criada.id() + "/publicar", HttpMethod.PATCH,
				JwtHelper.comToken(token), PostagemResponse.class);

		PostagemRequest novoTitulo = TestBuilder.criarPostagem("Titulo trocado depois de publicar",
				"Conteudo atualizado com palavras suficientes para o corpo.", List.of("Java"));

		ResponseEntity<PostagemResponse> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + criada.id(), HttpMethod.PUT,
				JwtHelper.comToken(novoTitulo, token), PostagemResponse.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals(criada.slug(), resposta.getBody().slug());
		assertEquals("Titulo trocado depois de publicar", resposta.getBody().titulo());
	}

	@Test
	@DisplayName("07 - Deve reaproveitar a mesma tag para variações de escrita")
	void deveReaproveitarTag() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);

		PostagemRequest primeira = TestBuilder.criarPostagem("Primeiro artigo sobre spring",
				"Conteudo com palavras suficientes para contar o tempo de leitura.", List.of("Spring Boot"));
		PostagemRequest segunda = TestBuilder.criarPostagem("Segundo artigo sobre spring",
				"Outro conteudo com palavras suficientes para o corpo do artigo.", List.of("spring boot"));

		testRestTemplate.exchange(BASE_URL, HttpMethod.POST,
				JwtHelper.comToken(primeira, token), PostagemResponse.class);

		ResponseEntity<PostagemResponse> resposta = testRestTemplate.exchange(BASE_URL, HttpMethod.POST,
				JwtHelper.comToken(segunda, token), PostagemResponse.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertEquals("spring-boot", resposta.getBody().tags().get(0).slug());
		assertEquals(1, tagRepository.findAllBySlugIn(List.of("spring-boot")).size());
	}

	@Test
	@DisplayName("08 - Não deve permitir publicar postagem de outra pessoa")
	void naoDevePublicarPostagemAlheia() {

		Long id = criar("Artigo que a intrusa vai tentar publicar").getBody().id();
		String tokenIntrusa = JwtHelper.obterToken(testRestTemplate, INTRUSA, SENHA);

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + id + "/publicar", HttpMethod.PATCH,
				JwtHelper.comToken(tokenIntrusa), String.class);

		assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
	}

	@Test
	@DisplayName("09 - Deve recusar mais de cinco tags")
	void deveRecusarExcessoDeTags() {

		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		PostagemRequest request = TestBuilder.criarPostagem("Artigo com tags demais",
				"Conteudo com palavras suficientes para o corpo do artigo.",
				List.of("um", "dois", "tres", "quatro", "cinco", "seis"));

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL, HttpMethod.POST, JwtHelper.comToken(request, token), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}

	@Test
	@DisplayName("10 - Feed público não deve exigir token")
	void feedEhPublico() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "?page=0&size=5", HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
	}

	@Test
	@DisplayName("11 - Listagem de rascunhos deve exigir token")
	void minhasExigeToken() {

		ResponseEntity<String> resposta = testRestTemplate.exchange(
				BASE_URL + "/minhas", HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
	}

	private ResponseEntity<PostagemResponse> criar(String titulo) {
		String token = JwtHelper.obterToken(testRestTemplate, AUTORA, SENHA);
		return testRestTemplate.exchange(BASE_URL, HttpMethod.POST,
				JwtHelper.comToken(TestBuilder.criarPostagem(titulo), token), PostagemResponse.class);
	}

}
