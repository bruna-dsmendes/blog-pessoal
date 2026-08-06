package com.generation.blogpessoal.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Envelope de paginacao proprio da API.
 *
 * Serializar o Page do Spring diretamente funciona, mas expoe a estrutura
 * interna do framework no contrato e gera warning a partir do Spring Boot 3.3.
 */
public record PageResponse<T>(
		List<T> conteudo,
		int pagina,
		int tamanho,
		long totalElementos,
		int totalPaginas,
		boolean primeira,
		boolean ultima) {

	public static <T> PageResponse<T> de(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

}
