package com.generation.blogpessoal.dto.usuario;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Senha opcional: re-encodar o hash recebido de volta trancaria a conta. */
public record UsuarioAtualizarRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
		String nome,

		@NotBlank(message = "O nome de usuário é obrigatório")
		@Size(min = 3, max = 30, message = "O nome de usuário deve ter entre 3 e 30 caracteres")
		@Pattern(regexp = "^[a-z0-9-]+$",
				message = "Use apenas letras minúsculas, números e hífen")
		@Schema(description = "Aparece na URL do perfil público", example = "bruna-mendes")
		String username,

		@Schema(example = "email@email.com.br")
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "Informe um e-mail válido")
		String usuario,

		@Size(min = 8, max = 100, message = "A senha deve ter no mínimo 8 caracteres")
		@Schema(description = "Envie apenas se quiser trocar a senha. Omita para manter a atual.")
		String senha,

		@Size(max = 5000, message = "O link da foto não pode passar de 5000 caracteres")
		String foto,

		@Size(max = 280, message = "A bio não pode passar de 280 caracteres")
		String bio,

		@Size(max = 10, message = "Use no máximo 10 links")
		List<@Valid LinkRequest> links) {
}
