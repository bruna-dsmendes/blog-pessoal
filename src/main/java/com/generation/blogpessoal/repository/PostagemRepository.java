package com.generation.blogpessoal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.model.StatusPostagem;

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

	/*
	 * Duas consultas em vez de uma com ":status IS NULL". Comparar parametro com
	 * NULL em JPQL deixa o Postgres sem conseguir inferir o tipo do parametro, e
	 * o erro que aparece nao tem nada a ver com a causa.
	 *
	 * Ambas incluem rascunhos: so sao chamadas com o e-mail do proprio autor.
	 */
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

	boolean existsBySlug(String slug);

}
