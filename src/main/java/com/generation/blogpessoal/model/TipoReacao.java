package com.generation.blogpessoal.model;

/**
 * Hoje só existe uma reação, mas o campo já nasce como enum.
 *
 * Acrescentar SALVAR ou COMEMORAR depois vira uma linha aqui, sem migration
 * de estrutura, porque a coluna já guarda o nome do tipo.
 */
public enum TipoReacao {

	CURTIR

}
