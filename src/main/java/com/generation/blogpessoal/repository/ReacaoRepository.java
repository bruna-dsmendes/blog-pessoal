package com.generation.blogpessoal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generation.blogpessoal.model.Reacao;

public interface ReacaoRepository extends JpaRepository<Reacao, Long> {

	long countByPostagemId(Long postagemId);

	boolean existsByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

	Optional<Reacao> findByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

	List<Reacao> findAllByUsuarioId(Long usuarioId);

	@Modifying
	@Query("DELETE FROM Reacao r WHERE r.usuario.id = :usuarioId")
	void excluirDoUsuario(@Param("usuarioId") Long usuarioId);

	/*
	 * Subconsulta em vez de navegar por r.postagem.usuario: DELETE em massa no
	 * JPQL não aceita join implícito.
	 */
	@Modifying
	@Query("""
			DELETE FROM Reacao r
			WHERE r.postagem.id IN (SELECT p.id FROM Postagem p WHERE p.usuario.id = :usuarioId)
			""")
	void excluirDasPostagensDoUsuario(@Param("usuarioId") Long usuarioId);

}
