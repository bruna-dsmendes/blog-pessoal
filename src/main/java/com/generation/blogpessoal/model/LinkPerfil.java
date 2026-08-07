package com.generation.blogpessoal.model;

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

/**
 * Tabela em vez de uma coluna por rede: acrescentar uma nova passa a ser uma
 * linha no enum, sem migration nem campo novo em quatro arquivos.
 */
@Entity
@Table(name = "tb_links",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_links_usuario_tipo",
				columnNames = { "usuario_id", "tipo" }))
public class LinkPerfil {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private TipoLink tipo;

	@Column(length = 300, nullable = false)
	private String url;

	@Column(nullable = false)
	private Integer ordem = 0;

	public LinkPerfil() {
	}

	public LinkPerfil(Usuario usuario, TipoLink tipo, String url, Integer ordem) {
		this.usuario = usuario;
		this.tipo = tipo;
		this.url = url;
		this.ordem = ordem;
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public TipoLink getTipo() {
		return tipo;
	}

	public void setTipo(TipoLink tipo) {
		this.tipo = tipo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

}
