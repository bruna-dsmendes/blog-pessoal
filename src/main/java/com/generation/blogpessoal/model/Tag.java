package com.generation.blogpessoal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_tags")
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
	private String nome;

	@Column(length = 60, nullable = false, unique = true)
	private String slug;

	public Tag() {
	}

	public Tag(String nome, String slug) {
		this.nome = nome;
		this.slug = slug;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	/*
	 * equals e hashCode pelo slug, que é a chave natural e imutável da tag.
	 * Usar o id quebraria o Set quando a tag ainda não foi persistida.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Tag outra)) {
			return false;
		}
		return slug != null && slug.equals(outra.slug);
	}

	@Override
	public int hashCode() {
		return slug == null ? 0 : slug.hashCode();
	}

}
