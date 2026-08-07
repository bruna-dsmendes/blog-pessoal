package com.generation.blogpessoal.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envio pela API HTTP do provedor, e não por SMTP.
 *
 * A hospedagem bloqueia conexões de saída na porta 587 como medida antispam,
 * então o SMTP simplesmente estoura timeout em produção. A API usa 443, que é
 * HTTPS comum e passa.
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final RestClient restClient;
	private final boolean habilitado;
	private final String chaveDaApi;
	private final String remetente;
	private final String nomeRemetente;

	public EmailService(RestClient.Builder builder,
			@Value("${app.mail.habilitado:false}") boolean habilitado,
			@Value("${app.mail.api-url:https://api.brevo.com/v3/smtp/email}") String urlDaApi,
			@Value("${app.mail.api-key:}") String chaveDaApi,
			@Value("${app.mail.remetente}") String remetente,
			@Value("${app.mail.nome-remetente}") String nomeRemetente) {

		this.restClient = builder.baseUrl(urlDaApi).build();
		this.habilitado = habilitado;
		this.chaveDaApi = chaveDaApi;
		this.remetente = remetente;
		this.nomeRemetente = nomeRemetente;
	}

	/*
	 * Assíncrono e tolerante a falha de propósito. Quem pede não espera o
	 * provedor responder, e uma indisponibilidade dele não derruba a requisição
	 * nem revela se a conta existe.
	 */
	@Async
	public void enviar(String destinatario, String assunto, String corpoHtml) {

		if (!habilitado || chaveDaApi.isBlank()) {
			log.info("Envio de e-mail desabilitado. Assunto que seria enviado para {}: {}",
					destinatario, assunto);
			return;
		}

		Map<String, Object> corpo = Map.of(
				"sender", Map.of("name", nomeRemetente, "email", remetente),
				"to", List.of(Map.of("email", destinatario)),
				"subject", assunto,
				"htmlContent", corpoHtml);

		try {
			restClient.post()
					.header("api-key", chaveDaApi)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(corpo)
					.retrieve()
					.toBodilessEntity();

		} catch (RuntimeException e) {
			log.error("Falha ao enviar e-mail para {}", destinatario, e);
		}
	}

}
