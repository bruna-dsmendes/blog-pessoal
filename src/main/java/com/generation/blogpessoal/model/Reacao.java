package com.generation.blogpessoal.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tb_reacoes",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_reacoes_postagem_usuario",
				columnNames = { "postagem_id", "usuario_id" }))
public class Reacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "postagem_id")
	private Postagem postagem;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private TipoReacao tipo = TipoReacao.CURTIR;

	@CreationTimestamp
	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	public Reacao() {
	}

	public Reacao(Postagem postagem, Usuario usuario, TipoReacao tipo) {
		this.postagem = postagem;
		this.usuario = usuario;
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public Postagem getPostagem() {
		return postagem;
	}

	public void setPostagem(Postagem postagem) {
		this.postagem = postagem;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public TipoReacao getTipo() {
		return tipo;
	}

	public void setTipo(TipoReacao tipo) {
		this.tipo = tipo;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

}
