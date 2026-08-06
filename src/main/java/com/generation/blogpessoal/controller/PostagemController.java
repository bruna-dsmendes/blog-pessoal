package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.PageResponse;
import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.postagem.PostagemResponse;
import com.generation.blogpessoal.dto.postagem.PostagemResumoResponse;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.service.PostagemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/postagens")
@Tag(name = "Postagens")
public class PostagemController {

	private final PostagemService postagemService;

	public PostagemController(PostagemService postagemService) {
		this.postagemService = postagemService;
	}

	// ---------------------------------------------------------------- leitura

	@GetMapping
	@Operation(summary = "Feed público, só com postagens publicadas")
	public ResponseEntity<PageResponse<PostagemResumoResponse>> feed(
			@PageableDefault(size = 10, sort = "publicadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.feed(pageable)));
	}

	@GetMapping("/buscar")
	@Operation(summary = "Busca no título e no subtítulo das postagens publicadas")
	public ResponseEntity<PageResponse<PostagemResumoResponse>> buscar(
			@RequestParam String termo,
			@PageableDefault(size = 10, sort = "publicadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.buscar(termo, pageable)));
	}

	@GetMapping("/autor/{username}")
	@Operation(summary = "Artigos publicados de um autor")
	public ResponseEntity<PageResponse<PostagemResumoResponse>> porAutor(
			@PathVariable String username,
			@PageableDefault(size = 10, sort = "publicadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.porAutor(username, pageable)));
	}

	@GetMapping("/tag/{slugTag}")
	@Operation(summary = "Postagens publicadas com uma tag")
	public ResponseEntity<PageResponse<PostagemResumoResponse>> porTag(
			@PathVariable String slugTag,
			@PageableDefault(size = 10, sort = "publicadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.porTag(slugTag, pageable)));
	}

	@GetMapping("/minhas")
	@Operation(summary = "Postagens do usuário autenticado, incluindo rascunhos")
	public ResponseEntity<PageResponse<PostagemResumoResponse>> minhas(
			@RequestParam(required = false) StatusPostagem status,
			@PageableDefault(size = 10, sort = "atualizadoEm", direction = Sort.Direction.DESC) Pageable pageable,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(PageResponse.de(
				postagemService.minhas(usuarioLogado.getUsername(), status, pageable)));
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Busca uma postagem pelo slug. É por aqui que o front monta a URL do artigo")
	public ResponseEntity<PostagemResponse> porSlug(
			@PathVariable String slug,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.porSlug(slug, emailDe(usuarioLogado)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostagemResponse> porId(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.porId(id, emailDe(usuarioLogado)));
	}

	// ---------------------------------------------------------------- escrita

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cria uma postagem como rascunho")
	public PostagemResponse criar(
			@Valid @RequestBody PostagemRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return postagemService.criar(request, usuarioLogado.getUsername());
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza o conteúdo. Não altera o status")
	public ResponseEntity<PostagemResponse> atualizar(
			@PathVariable Long id,
			@Valid @RequestBody PostagemRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.atualizar(id, request, usuarioLogado.getUsername()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id, @AuthenticationPrincipal UserDetails usuarioLogado) {
		postagemService.excluir(id, usuarioLogado.getUsername());
	}

	// ----------------------------------------------------------- ciclo de vida

	@PatchMapping("/{id}/publicar")
	@Operation(summary = "Publica a postagem. A data de publicação é gravada só na primeira vez")
	public ResponseEntity<PostagemResponse> publicar(
			@PathVariable Long id, @AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.publicar(id, usuarioLogado.getUsername()));
	}

	@PatchMapping("/{id}/arquivar")
	@Operation(summary = "Tira a postagem do feed, mantendo o link acessível")
	public ResponseEntity<PostagemResponse> arquivar(
			@PathVariable Long id, @AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.arquivar(id, usuarioLogado.getUsername()));
	}

	@PatchMapping("/{id}/rascunho")
	@Operation(summary = "Volta a postagem para rascunho")
	public ResponseEntity<PostagemResponse> voltarParaRascunho(
			@PathVariable Long id, @AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.voltarParaRascunho(id, usuarioLogado.getUsername()));
	}

	/** Endpoints públicos recebem null quando ninguém está autenticado. */
	private String emailDe(UserDetails usuarioLogado) {
		return usuarioLogado == null ? null : usuarioLogado.getUsername();
	}

}
