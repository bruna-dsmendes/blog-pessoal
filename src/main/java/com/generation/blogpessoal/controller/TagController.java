package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.PageResponse;
import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Só leitura. Tags nascem junto com a postagem que as usa, como no dev.to,
 * então não existe endpoint de cadastro.
 */
@RestController
@RequestMapping("/tags")
@Tag(name = "Tags")
public class TagController {

	private final TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<TagResponse>> listar(
			@PageableDefault(size = 30, sort = "nome") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(tagService.listar(pageable)));
	}

	@GetMapping("/buscar")
	@Operation(summary = "Busca tags pelo nome, para autocomplete no editor")
	public ResponseEntity<PageResponse<TagResponse>> buscar(
			@RequestParam String nome,
			@PageableDefault(size = 10, sort = "nome") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(tagService.buscarPorNome(nome, pageable)));
	}

	@GetMapping("/{slug}")
	public ResponseEntity<TagResponse> porSlug(@PathVariable String slug) {
		return ResponseEntity.ok(tagService.buscarPorSlug(slug));
	}

}
