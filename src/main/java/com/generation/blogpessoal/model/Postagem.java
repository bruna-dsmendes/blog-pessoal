package com.generation.blogpessoal.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_postagens")
public class Postagem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String titulo;

	@Column(length = 200)
	private String subtitulo;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String conteudo;

	@Column(length = 160, nullable = false, unique = true)
	private String slug;

	@Column(name = "capa_url", length = 1000)
	private String capaUrl;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private StatusPostagem status = StatusPostagem.RASCUNHO;

	@Column(name = "tempo_leitura", nullable = false)
	private Integer tempoLeitura = 1;

	@CreationTimestamp
	@Column(name = "data", updatable = false)
	private LocalDateTime criadoEm;

	@UpdateTimestamp
	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	/** Congela na primeira publicação. Editar depois não altera esse valor. */
	@Column(name = "publicado_em")
	private LocalDateTime publicadoEm;

	/*
	 * Escrita dupla durante a janela de expand and contract.
	 * A aplicação nova não lê esse campo, mas continua preenchendo para que a
	 * versão anterior siga funcionando em caso de rollback. Sai na V7.
	 */
	@Deprecated
	@Column(columnDefinition = "TEXT")
	private String texto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	/*
	 * BatchSize em vez de JOIN FETCH: buscar coleção com paginação faz o
	 * Hibernate trazer tudo para a memória e paginar em Java. Com BatchSize,
	 * uma página de 10 postagens resolve as tags em uma consulta extra.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_postagem_tags",
			joinColumns = @JoinColumn(name = "postagem_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@BatchSize(size = 25)
	private Set<Tag> tags = new LinkedHashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getSubtitulo() {
		return subtitulo;
	}

	public void setSubtitulo(String subtitulo) {
		this.subtitulo = subtitulo;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
		this.texto = conteudo;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getCapaUrl() {
		return capaUrl;
	}

	public void setCapaUrl(String capaUrl) {
		this.capaUrl = capaUrl;
	}

	public StatusPostagem getStatus() {
		return status;
	}

	public void setStatus(StatusPostagem status) {
		this.status = status;
	}

	public Integer getTempoLeitura() {
		return tempoLeitura;
	}

	public void setTempoLeitura(Integer tempoLeitura) {
		this.tempoLeitura = tempoLeitura;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public LocalDateTime getPublicadoEm() {
		return publicadoEm;
	}

	public void setPublicadoEm(LocalDateTime publicadoEm) {
		this.publicadoEm = publicadoEm;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags == null ? new LinkedHashSet<>() : tags;
	}

}
