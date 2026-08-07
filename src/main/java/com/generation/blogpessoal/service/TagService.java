package com.generation.blogpessoal.service;

import com.generation.blogpessoal.dto.tag.TagResponse;
import com.generation.blogpessoal.exception.RecursoNaoEncontradoException;
import com.generation.blogpessoal.model.Tag;
import com.generation.blogpessoal.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagService {

	private final TagRepository tagRepository;
	private final SlugService slugService;

	public TagService(TagRepository tagRepository, SlugService slugService) {
		this.tagRepository = tagRepository;
		this.slugService = slugService;
	}

	@Transactional(readOnly = true)
	public Page<TagResponse> listar(Pageable pageable) {
		return tagRepository.findAll(pageable).map(TagResponse::de);
	}

	@Transactional(readOnly = true)
	public Page<TagResponse> buscarPorNome(String nome, Pageable pageable) {
		return tagRepository.findAllByNomeContainingIgnoreCase(nome, pageable).map(TagResponse::de);
	}

	@Transactional(readOnly = true)
	public TagResponse buscarPorSlug(String slug) {
		return tagRepository.findBySlug(slug)
				.map(TagResponse::de)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Tag não encontrada: " + slug));
	}

	@Transactional
	public Set<Tag> resolver(List<String> nomes) {

		if (nomes == null || nomes.isEmpty()) {
			return new LinkedHashSet<>();
		}

		Map<String, String> nomePorSlug = new LinkedHashMap<>();

		for (String nome : nomes) {
			String slug = slugService.normalizar(nome);
			if (!slug.isEmpty()) {
				nomePorSlug.putIfAbsent(slug, nome.trim());
			}
		}

		if (nomePorSlug.isEmpty()) {
			return new LinkedHashSet<>();
		}

		Set<Tag> resultado = new LinkedHashSet<>(tagRepository.findAllBySlugIn(nomePorSlug.keySet()));

		resultado.forEach(tag -> nomePorSlug.remove(tag.getSlug()));

		nomePorSlug.forEach((slug, nome) -> resultado.add(tagRepository.save(new Tag(nome, slug))));

		return resultado;
	}

}
