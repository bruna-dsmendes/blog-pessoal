package com.generation.blogpessoal.util;

import com.generation.blogpessoal.dto.postagem.PostagemRequest;
import com.generation.blogpessoal.dto.tema.TemaRequest;
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
		return new UsuarioAtualizarRequest(nome, email, senha, "-");
	}

	public static LoginRequest criarLogin(String email, String senha) {
		return new LoginRequest(email, senha);
	}

	public static TemaRequest criarTema(String descricao) {
		return new TemaRequest(descricao);
	}

	public static PostagemRequest criarPostagem(String titulo, String texto, Long temaId) {
		return new PostagemRequest(titulo, texto, temaId);
	}

}
