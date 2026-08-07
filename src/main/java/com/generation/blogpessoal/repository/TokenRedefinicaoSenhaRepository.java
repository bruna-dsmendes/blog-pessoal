package com.generation.blogpessoal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generation.blogpessoal.model.TokenRedefinicaoSenha;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {

	@Query("""
			SELECT t FROM TokenRedefinicaoSenha t
			JOIN FETCH t.usuario
			WHERE t.tokenHash = :hash
			""")
	Optional<TokenRedefinicaoSenha> buscarPorHash(@Param("hash") String hash);

	/** Um pedido novo invalida os anteriores da mesma pessoa. */
	@Modifying
	@Query("DELETE FROM TokenRedefinicaoSenha t WHERE t.usuario.id = :usuarioId")
	void excluirDoUsuario(@Param("usuarioId") Long usuarioId);

}
