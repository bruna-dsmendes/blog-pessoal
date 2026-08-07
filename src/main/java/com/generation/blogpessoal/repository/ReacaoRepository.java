package com.generation.blogpessoal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.blogpessoal.model.Reacao;

public interface ReacaoRepository extends JpaRepository<Reacao, Long> {

	long countByPostagemId(Long postagemId);

	boolean existsByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

	Optional<Reacao> findByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

}
