package com.generation.blogpessoal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.estatisticas.EstatisticasResponse;
import com.generation.blogpessoal.service.EstatisticaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/estatisticas")
@Tag(name = "Estatísticas")
public class EstatisticaController {

	private final EstatisticaService estatisticaService;

	public EstatisticaController(EstatisticaService estatisticaService) {
		this.estatisticaService = estatisticaService;
	}

	@GetMapping
	@Operation(summary = "Números da plataforma, contando apenas conteúdo publicado")
	public ResponseEntity<EstatisticasResponse> daPlataforma() {
		return ResponseEntity.ok(estatisticaService.daPlataforma());
	}

}
