package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.PageResponse;
import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.service.TemaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Mantido apenas para leitura enquanto o front migra para /tags.
 * Removido junto com a migration V5.
 *
 * @deprecated use {@link TagController}.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/temas")
@Tag(name = "Temas (obsoleto)")
@SuppressWarnings("removal")
public class TemaController {

	private final TemaService temaService;

	public TemaController(TemaService temaService) {
		this.temaService = temaService;
	}

	@GetMapping
	@Operation(summary = "Obsoleto. Use GET /tags", deprecated = true)
	public ResponseEntity<PageResponse<TemaResponse>> listar(
			@PageableDefault(size = 20, sort = "descricao") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(temaService.listar(pageable)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obsoleto. Use GET /tags/{slug}", deprecated = true)
	public ResponseEntity<TemaResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(temaService.buscarPorId(id));
	}

	@GetMapping("/descricao/{descricao}")
	@Operation(summary = "Obsoleto. Use GET /tags/buscar", deprecated = true)
	public ResponseEntity<PageResponse<TemaResponse>> buscarPorDescricao(
			@PathVariable String descricao,
			@PageableDefault(size = 20, sort = "descricao") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(temaService.buscarPorDescricao(descricao, pageable)));
	}

}
