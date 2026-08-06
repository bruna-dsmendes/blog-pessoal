package com.generation.blogpessoal.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey signingKey;
	private final Duration expiration;

	/*
	 * A chave vem de variável de ambiente e é validada no boot.
	 * Antes ela estava escrita no código e versionada no GitHub, o que na
	 * prática significa que qualquer pessoa com acesso ao repositório
	 * conseguia forjar um token válido para qualquer usuário.
	 */
	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {

		byte[] keyBytes = Decoders.BASE64.decode(secret);

		if (keyBytes.length < 32) {
			throw new IllegalStateException(
					"A variável JWT_SECRET precisa ser uma string Base64 de no mínimo 32 bytes (256 bits)");
		}

		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
		this.expiration = Duration.ofMinutes(expirationMinutes);
	}

	public String generateToken(String username) {
		Instant agora = Instant.now();
		return Jwts.builder()
				.subject(username)
				.issuedAt(Date.from(agora))
				.expiration(Date.from(agora.plus(expiration)))
				.signWith(signingKey)
				.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		Claims claims = extractAllClaims(token);
		return claims.getSubject().equals(userDetails.getUsername())
				&& claims.getExpiration().after(new Date());
	}

	public Instant calcularExpiracao() {
		return Instant.now().plus(expiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

}
