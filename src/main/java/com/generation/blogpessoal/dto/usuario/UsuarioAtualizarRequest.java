package com.generation.blogpessoal.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A senha é opcional aqui de propósito.
 *
 * No código antigo todo update re-encodava o campo senha. Se o front devolvesse
 * o hash que recebeu, o hash virava senha e era encodado de novo, deixando o
 * usuário sem conseguir logar.
 */
public record UsuarioAtualizarRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
		String nome,

		@Schema(example = "email@email.com.br")
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "Informe um e-mail válido")
		String usuario,

		@Size(min = 8, max = 100, message = "A senha deve ter no mínimo 8 caracteres")
		@Schema(description = "Envie apenas se quiser trocar a senha. Omita para manter a atual.")
		String senha,

		@Size(max = 5000, message = "O link da foto não pode passar de 5000 caracteres")
		String foto) {
}
