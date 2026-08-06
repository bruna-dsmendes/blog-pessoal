package com.generation.blogpessoal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.blogpessoal.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findBySlug(String slug);

	List<Tag> findAllBySlugIn(Collection<String> slugs);

	Page<Tag> findAllByNomeContainingIgnoreCase(String nome, Pageable pageable);

}
