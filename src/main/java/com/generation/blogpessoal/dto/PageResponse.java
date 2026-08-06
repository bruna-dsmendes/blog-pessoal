package com.generation.blogpessoal.dto;

import java.util.List;

import org.springframework.data.domain.Page;

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
