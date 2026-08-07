package com.generation.blogpessoal.repository;

import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.StatusPostagem;
import com.generation.blogpessoal.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostagemRepository extends JpaRepository<Postagem, Long> {

	/*
	 * O autor vem por JOIN FETCH porque é ManyToOne. As tags não: coleção com
	 * paginação faria o Hibernate paginar em memória. Elas são resolvidas pelo
	 * BatchSize declarado na entidade.
	 */
	@Query(value = """
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.usuario
			WHERE p.status = :status
			""",
			countQuery = "SELECT COUNT(p) FROM Postagem p WHERE p.status = :status")
	Page<Postagem> buscarPorStatus(@Param("status") StatusPostagem status, Pageable pageable);

	@Query(value = """
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.usuario
			JOIN p.tags t
			WHERE t.slug = :slugTag AND p.status = :status
			""",
			countQuery = """
			SELECT COUNT(p) FROM Postagem p JOIN p.tags t
			WHERE t.slug = :slugTag AND p.status = :status
			""")
	Page<Postagem> buscarPorTag(@Param("slugTag") String slugTag,
			@Param("status") StatusPostagem status, Pageable pageable);

	@Query(value = """
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.usuario
			WHERE p.status = :status
			  AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
			   OR LOWER(COALESCE(p.subtitulo, '')) LIKE LOWER(CONCAT('%', :termo, '%')))
			""",
			countQuery = """
			SELECT COUNT(p) FROM Postagem p
			WHERE p.status = :status
			  AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
			   OR LOWER(COALESCE(p.subtitulo, '')) LIKE LOWER(CONCAT('%', :termo, '%')))
			""")
	Page<Postagem> buscarPorTermo(@Param("termo") String termo,
			@Param("status") StatusPostagem status, Pageable pageable);

	@Query(value = """
			SELECT p FROM Postagem p
			JOIN FETCH p.usuario u
			WHERE u.usuario = :email
			""",
			countQuery = "SELECT COUNT(p) FROM Postagem p WHERE p.usuario.usuario = :email")
	Page<Postagem> buscarDoAutor(@Param("email") String email, Pageable pageable);

	@Query(value = """
			SELECT p FROM Postagem p
			JOIN FETCH p.usuario u
			WHERE u.usuario = :email AND p.status = :status
			""",
			countQuery = """
			SELECT COUNT(p) FROM Postagem p
			WHERE p.usuario.usuario = :email AND p.status = :status
			""")
	Page<Postagem> buscarDoAutorPorStatus(@Param("email") String email,
			@Param("status") StatusPostagem status, Pageable pageable);

	@Query("""
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.usuario
			LEFT JOIN FETCH p.tags
			WHERE p.id = :id
			""")
	Optional<Postagem> buscarPorIdCompleta(@Param("id") Long id);

	@Query("""
			SELECT p FROM Postagem p
			LEFT JOIN FETCH p.usuario
			LEFT JOIN FETCH p.tags
			WHERE p.slug = :slug
			""")
	Optional<Postagem> buscarPorSlugCompleta(@Param("slug") String slug);

	@Query(value = """
			SELECT p FROM Postagem p
			JOIN FETCH p.usuario u
			WHERE u.username = :username AND p.status = :status
			""",
			countQuery = """
			SELECT COUNT(p) FROM Postagem p
			WHERE p.usuario.username = :username AND p.status = :status
			""")
	Page<Postagem> buscarDoAutorPorUsername(@Param("username") String username,
			@Param("status") StatusPostagem status, Pageable pageable);

	long countByUsuarioIdAndStatus(Long usuarioId, StatusPostagem status);

	@Query("""
			SELECT COALESCE(SUM(p.tempoLeitura), 0) FROM Postagem p
			WHERE p.usuario.id = :usuarioId AND p.status = :status
			""")
	long somarTempoLeitura(@Param("usuarioId") Long usuarioId,
			@Param("status") StatusPostagem status);

	/** Tags do autor, da mais usada para a menos usada. */
	@Query("""
			SELECT t FROM Postagem p JOIN p.tags t
			WHERE p.usuario.id = :usuarioId AND p.status = :status
			GROUP BY t
			ORDER BY COUNT(t) DESC
			""")
	List<Tag> buscarTagsMaisUsadas(@Param("usuarioId") Long usuarioId,
			@Param("status") StatusPostagem status, Pageable pageable);

	boolean existsBySlug(String slug);

}
