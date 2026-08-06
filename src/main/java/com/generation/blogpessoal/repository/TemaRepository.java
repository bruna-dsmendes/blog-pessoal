package com.generation.blogpessoal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.blogpessoal.model.Tema;

public interface TemaRepository extends JpaRepository<Tema, Long> {

	Page<Tema> findAllByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

}
