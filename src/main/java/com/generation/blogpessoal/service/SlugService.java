package com.generation.blogpessoal.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * Converte texto livre em slug de URL.
 *
 * "Como usar Java 21 na prática!" vira "como-usar-java-21-na-pratica".
 */
@Service
public class SlugService {

	private static final Pattern ACENTOS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");
	private static final Pattern HIFENS_NAS_PONTAS = Pattern.compile("^-+|-+$");

	private static final int TAMANHO_MAXIMO = 120;

	public String normalizar(String texto) {

		if (texto == null || texto.isBlank()) {
			return "";
		}

		String semAcento = ACENTOS.matcher(
				Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");

		String slug = NAO_ALFANUMERICO.matcher(semAcento.toLowerCase(Locale.ROOT)).replaceAll("-");
		slug = HIFENS_NAS_PONTAS.matcher(slug).replaceAll("");

		if (slug.length() > TAMANHO_MAXIMO) {
			slug = HIFENS_NAS_PONTAS.matcher(slug.substring(0, TAMANHO_MAXIMO)).replaceAll("");
		}

		return slug;
	}

	/**
	 * Acrescenta sufixo numérico até encontrar um slug livre.
	 *
	 * Ainda assim existe janela de corrida entre duas requisições simultâneas.
	 * A constraint UNIQUE no banco é quem garante a unicidade de fato: essa
	 * verificação serve para o caso comum, não como trava.
	 */
	public String gerarUnico(String texto, Predicate<String> jaExiste) {

		String base = normalizar(texto);

		if (base.isEmpty()) {
			base = "postagem";
		}

		String candidato = base;
		int sufixo = 2;

		while (jaExiste.test(candidato)) {
			candidato = base + "-" + sufixo;
			sufixo++;
		}

		return candidato;
	}

}
