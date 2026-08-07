package com.generation.blogpessoal.service;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final boolean habilitado;
	private final String remetente;
	private final String nomeRemetente;

	public EmailService(JavaMailSender mailSender,
			@Value("${app.mail.habilitado:false}") boolean habilitado,
			@Value("${app.mail.remetente}") String remetente,
			@Value("${app.mail.nome-remetente}") String nomeRemetente) {
		this.mailSender = mailSender;
		this.habilitado = habilitado;
		this.remetente = remetente;
		this.nomeRemetente = nomeRemetente;
	}

	/*
	 * Assíncrono e tolerante a falha de propósito. O usuário não deve esperar o
	 * SMTP responder, e uma indisponibilidade do provedor de e-mail não pode
	 * derrubar a requisição nem revelar se a conta existe.
	 */
	@Async
	public void enviar(String destinatario, String assunto, String corpoHtml) {

		if (!habilitado) {
			log.info("Envio de e-mail desabilitado. Assunto que seria enviado para {}: {}",
					destinatario, assunto);
			return;
		}

		try {
			MimeMessage mensagem = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");

			helper.setFrom(remetente, nomeRemetente);
			helper.setTo(destinatario);
			helper.setSubject(assunto);
			helper.setText(corpoHtml, true);

			mailSender.send(mensagem);

		} catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
			log.error("Falha ao enviar e-mail para {}", destinatario, e);
		}
	}

}
