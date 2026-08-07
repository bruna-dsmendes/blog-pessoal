package com.generation.blogpessoal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
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
import com.generation.blogpessoal.dto.usuario.DadosDoUsuarioResponse;
import com.generation.blogpessoal.dto.usuario.ExclusaoDeContaRequest;
import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.LoginResponse;
import com.generation.blogpessoal.dto.usuario.PerfilPublicoResponse;
import com.generation.blogpessoal.dto.usuario.UsuarioAtualizarRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioResponse;
import com.generation.blogpessoal.security.AuthCookieService;
import com.generation.blogpessoal.service.ContaService;
import com.generation.blogpessoal.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

	private final UsuarioService usuarioService;
	private final ContaService contaService;
	private final AuthCookieService authCookieService;

	public UsuarioController(UsuarioService usuarioService, ContaService contaService,
			AuthCookieService authCookieService) {
		this.usuarioService = usuarioService;
		this.contaService = contaService;
		this.authCookieService = authCookieService;
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

	@GetMapping("/perfil/{username}")
	@Operation(summary = "Perfil público de um autor, com estatísticas e tags mais usadas")
	public ResponseEntity<PerfilPublicoResponse> perfilPublico(@PathVariable String username) {
		return ResponseEntity.ok(usuarioService.perfilPublico(username));
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

	@GetMapping("/me/dados")
	@Operation(summary = "Exporta todos os seus dados, incluindo rascunhos (LGPD, art. 18, V)")
	public ResponseEntity<DadosDoUsuarioResponse> exportarDados(
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		return ResponseEntity.ok(contaService.exportar(usuarioLogado.getUsername()));
	}

	@PostMapping("/excluir-conta")
	@Operation(summary = "Exclui a conta e encerra a sessão (LGPD, art. 18, VI)")
	public ResponseEntity<Void> excluirConta(
			@Valid @RequestBody ExclusaoDeContaRequest request,
			@AuthenticationPrincipal UserDetails usuarioLogado) {

		contaService.excluir(usuarioLogado.getUsername(), request);

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, authCookieService.limpar().toString())
				.build();
	}

	@PostMapping("/logar")
	@Operation(summary = "Autentica e devolve a sessão em um cookie httpOnly")
	public ResponseEntity<LoginResponse> autenticar(@Valid @RequestBody LoginRequest request) {

		LoginResponse resposta = usuarioService.autenticar(request);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookieService.criar(resposta.token()).toString())
				.body(resposta);
	}

	/*
	 * Precisa existir no servidor. Como o front não enxerga o cookie httpOnly,
	 * ele não consegue apagar a sessão sozinho: só quem escreveu o cookie pode
	 * sobrescrevê-lo com validade zero.
	 */
	@PostMapping("/deslogar")
	@Operation(summary = "Encerra a sessão apagando o cookie")
	public ResponseEntity<Void> deslogar() {

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, authCookieService.limpar().toString())
				.build();
	}

}
