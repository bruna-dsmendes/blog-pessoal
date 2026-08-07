package com.generation.blogpessoal.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_tokens_senha")
public class TokenRedefinicaoSenha {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	/** Só o hash. O valor original existe apenas no link enviado por e-mail. */
	@Column(name = "token_hash", length = 64, nullable = false, unique = true)
	private String tokenHash;

	@CreationTimestamp
	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "expira_em", nullable = false)
	private LocalDateTime expiraEm;

	@Column(name = "usado_em")
	private LocalDateTime usadoEm;

	public TokenRedefinicaoSenha() {
	}

	public TokenRedefinicaoSenha(Usuario usuario, String tokenHash, LocalDateTime expiraEm) {
		this.usuario = usuario;
		this.tokenHash = tokenHash;
		this.expiraEm = expiraEm;
	}

	public boolean estaValido() {
		return usadoEm == null && expiraEm.isAfter(LocalDateTime.now());
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getExpiraEm() {
		return expiraEm;
	}

	public LocalDateTime getUsadoEm() {
		return usadoEm;
	}

	public void marcarComoUsado() {
		this.usadoEm = LocalDateTime.now();
	}

}
