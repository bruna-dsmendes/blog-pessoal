package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.generation.blogpessoal.dto.tema.TemaRequest;
import com.generation.blogpessoal.dto.tema.TemaResponse;
import com.generation.blogpessoal.service.TemaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/temas")
@Tag(name = "Temas")
public class TemaController {

	private final TemaService temaService;

	public TemaController(TemaService temaService) {
		this.temaService = temaService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<TemaResponse>> listar(
			@PageableDefault(size = 20, sort = "descricao") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(temaService.listar(pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TemaResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(temaService.buscarPorId(id));
	}

	@GetMapping("/descricao/{descricao}")
	public ResponseEntity<PageResponse<TemaResponse>> buscarPorDescricao(
			@PathVariable String descricao,
			@PageableDefault(size = 20, sort = "descricao") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(temaService.buscarPorDescricao(descricao, pageable)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TemaResponse criar(@Valid @RequestBody TemaRequest request) {
		return temaService.criar(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza um tema. Retorna 200, e não 201: nada novo foi criado")
	public ResponseEntity<TemaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TemaRequest request) {
		return ResponseEntity.ok(temaService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui um tema, desde que nenhuma postagem esteja usando ele")
	public void excluir(@PathVariable Long id) {
		temaService.excluir(id);
	}

}
