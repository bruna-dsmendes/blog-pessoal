package com.generation.blogpessoal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generation.blogpessoal.model.Postagem;

public interface PostagemRepository extends JpaRepository<Postagem, Long> {
	
	@Query(value = """
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.tema
			LEFT JOIN FETCH p.usuario
			""",
			countQuery = "SELECT COUNT(p) FROM Postagem p")
	Page<Postagem> buscarTodasComRelacionamentos(Pageable pageable);

	@Query(value = """
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.tema
			LEFT JOIN FETCH p.usuario
			WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
			""",
			countQuery = """
			SELECT COUNT(p) FROM Postagem p
			WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
			""")
	Page<Postagem> buscarPorTitulo(@Param("titulo") String titulo, Pageable pageable);

	@Query("""
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.tema
			LEFT JOIN FETCH p.usuario
			WHERE p.id = :id
			""")
	Optional<Postagem> buscarPorIdComRelacionamentos(@Param("id") Long id);

	long countByTemaId(Long temaId);

}
