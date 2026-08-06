package com.generation.blogpessoal.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Centraliza a tradução de exceção para resposta HTTP.
 *
 * Sem isso, cada controller precisa lembrar de montar o próprio erro, e o que
 * escapa vira stack trace de 500 na cara de quem consome a API.
 */
@RestControllerAdvice
public class RestExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResposta> tratarValidacao(
			MethodArgumentNotValidException ex, HttpServletRequest request) {

		Map<String, String> campos = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

		return ResponseEntity.badRequest().body(ErroResposta.comCampos(
				HttpStatus.BAD_REQUEST.value(),
				"Validação falhou",
				"Um ou mais campos estão inválidos",
				request.getRequestURI(),
				campos));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResposta> tratarJsonInvalido(
			HttpMessageNotReadableException ex, HttpServletRequest request) {

		return ResponseEntity.badRequest().body(ErroResposta.de(
				HttpStatus.BAD_REQUEST.value(),
				"Requisição malformada",
				"O corpo da requisição não pôde ser lido. Verifique o JSON enviado.",
				request.getRequestURI()));
	}

	@ExceptionHandler({ RecursoNaoEncontradoException.class, NoHandlerFoundException.class })
	public ResponseEntity<ErroResposta> tratarNaoEncontrado(
			Exception ex, HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResposta.de(
				HttpStatus.NOT_FOUND.value(),
				"Não encontrado",
				ex.getMessage(),
				request.getRequestURI()));
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErroResposta> tratarRegraDeNegocio(
			RegraDeNegocioException ex, HttpServletRequest request) {

		return ResponseEntity.badRequest().body(ErroResposta.de(
				HttpStatus.BAD_REQUEST.value(),
				"Requisição inválida",
				ex.getMessage(),
				request.getRequestURI()));
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResposta> tratarConflito(
			ConflitoException ex, HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResposta.de(
				HttpStatus.CONFLICT.value(),
				"Conflito",
				ex.getMessage(),
				request.getRequestURI()));
	}

	@ExceptionHandler({ OperacaoNaoPermitidaException.class, AccessDeniedException.class })
	public ResponseEntity<ErroResposta> tratarAcessoNegado(
			Exception ex, HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErroResposta.de(
				HttpStatus.FORBIDDEN.value(),
				"Acesso negado",
				ex.getMessage(),
				request.getRequestURI()));
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	public ResponseEntity<ErroResposta> tratarCredenciais(
			CredenciaisInvalidasException ex, HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResposta.de(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				ex.getMessage(),
				request.getRequestURI()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResposta> tratarIntegridade(
			DataIntegrityViolationException ex, HttpServletRequest request) {

		log.warn("Violação de integridade em {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResposta.de(
				HttpStatus.CONFLICT.value(),
				"Conflito de dados",
				"A operação viola uma restrição do banco de dados",
				request.getRequestURI()));
	}

	/*
	 * Rede de segurança. A mensagem real vai para o log, não para o cliente:
	 * detalhe interno de exceção é informação útil para quem quer atacar a API.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResposta> tratarErroInesperado(
			Exception ex, HttpServletRequest request) {

		log.error("Erro inesperado em {}", request.getRequestURI(), ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErroResposta.de(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Erro interno",
				"Ocorreu um erro inesperado. Tente novamente mais tarde.",
				request.getRequestURI()));
	}

}
