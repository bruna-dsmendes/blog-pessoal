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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.PageResponse;
import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.postagem.PostagemResponse;
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

	@GetMapping
	@Operation(summary = "Lista postagens paginadas, da mais recente para a mais antiga")
	public ResponseEntity<PageResponse<PostagemResponse>> listar(
			@PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.listar(pageable)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca uma postagem pelo id")
	public ResponseEntity<PostagemResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(postagemService.buscarPorId(id));
	}

	@GetMapping("/titulo/{titulo}")
	@Operation(summary = "Busca postagens pelo título")
	public ResponseEntity<PageResponse<PostagemResponse>> buscarPorTitulo(
			@PathVariable String titulo,
			@PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(postagemService.buscarPorTitulo(titulo, pageable)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cria uma postagem. O autor vem do token, não do corpo da requisição")
	public PostagemResponse criar(
			@Valid @RequestBody PostagemRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return postagemService.criar(request, usuarioLogado.getUsername());
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza uma postagem. Só o autor consegue")
	public ResponseEntity<PostagemResponse> atualizar(
			@PathVariable Long id,
			@Valid @RequestBody PostagemRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(postagemService.atualizar(id, request, usuarioLogado.getUsername()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui uma postagem. Só o autor consegue")
	public void excluir(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		postagemService.excluir(id, usuarioLogado.getUsername());
	}

}
