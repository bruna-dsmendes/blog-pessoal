package com.generation.blogpessoal.util;

import java.util.List;

import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.usuario.LoginRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioAtualizarRequest;
import com.generation.blogpessoal.dto.usuario.UsuarioRequest;

public class TestBuilder {

	private TestBuilder() {
	}

	public static UsuarioRequest criarUsuario(String nome, String email, String senha) {
		return new UsuarioRequest(nome, email, senha, "-");
	}

	public static UsuarioAtualizarRequest atualizarUsuario(String nome, String email, String senha) {
		return atualizarUsuario(nome, email, senha, gerarUsername(nome));
	}

	public static UsuarioAtualizarRequest atualizarUsuario(String nome, String email, String senha,
			String username) {
		return new UsuarioAtualizarRequest(nome, username, email, senha, "-", null, null, null);
	}

	private static String gerarUsername(String nome) {
		return nome.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
	}

	public static LoginRequest criarLogin(String email, String senha) {
		return new LoginRequest(email, senha);
	}

	public static PostagemRequest criarPostagem(String titulo, String conteudo, List<String> tags) {
		return new PostagemRequest(titulo, "Um subtitulo qualquer", conteudo, null, tags);
	}

	public static PostagemRequest criarPostagem(String titulo) {
		return criarPostagem(titulo, "Conteudo de teste com algumas palavras para o corpo do artigo.",
				List.of("Java"));
	}

}
