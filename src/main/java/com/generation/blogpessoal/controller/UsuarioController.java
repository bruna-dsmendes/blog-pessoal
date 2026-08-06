package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.generation.blogpessoal.dto.PageResponse;
import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioAtualizarRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/all")
	@Operation(summary = "Lista usuários paginados, sem expor senha")
	public ResponseEntity<PageResponse<UsuarioResponse>> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {

		return ResponseEntity.ok(PageResponse.de(usuarioService.listar(pageable)));
	}

	@GetMapping("/me")
	@Operation(summary = "Retorna o perfil do usuário autenticado")
	public ResponseEntity<UsuarioResponse> perfil(@AuthenticationPrincipal UserDetails usuarioLogado) {
		return ResponseEntity.ok(usuarioService.buscarPorEmail(usuarioLogado.getUsername()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(usuarioService.buscarPorId(id));
	}

	@PostMapping("/cadastrar")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse cadastrar(@Valid @RequestBody UsuarioRequest request) {
		return usuarioService.cadastrar(request);
	}

	@PutMapping("/atualizar")
	@Operation(summary = "Atualiza o perfil do usuário autenticado. O id do corpo é ignorado de propósito")
	public ResponseEntity<UsuarioResponse> atualizar(
			@Valid @RequestBody UsuarioAtualizarRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(usuarioService.atualizar(usuarioLogado.getUsername(), request));
	}

	@PostMapping("/logar")
	public ResponseEntity<LoginResponse> autenticar(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(usuarioService.autenticar(request));
	}

}
