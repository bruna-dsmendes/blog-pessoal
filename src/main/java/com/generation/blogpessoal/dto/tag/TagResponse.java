package com.generation.blogpessoal.dto.tag;

import com.generation.blogpessoal.model.Tag;

public record TagResponse(Long id, String nome, String slug) {

	public static TagResponse de(Tag tag) {
		return new TagResponse(tag.getId(), tag.getNome(), tag.getSlug());
	}

}
