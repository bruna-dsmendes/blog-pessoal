package com.generation.blogpessoal.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê o token e popula o SecurityContext.
 *
 * O cookie tem prioridade: é por onde o navegador autentica. O header
 * Authorization continua aceito para o Swagger, testes e clientes de API,
 * que não são navegadores e não têm por que carregar cookie.
 *
 * Se o token for inválido, o filtro apenas não autentica e deixa a requisição
 * seguir. Quem decide o que fazer é o SecurityFilterChain, que responde 401 nos
 * endpoints protegidos e libera os públicos.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;
	private final AuthCookieService authCookieService;

	public JwtAuthFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService,
			AuthCookieService authCookieService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.authCookieService = authCookieService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		String token = extrairToken(request);

		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			autenticar(request, token);
		}

		filterChain.doFilter(request, response);
	}

	private String extrairToken(HttpServletRequest request) {

		return authCookieService.extrair(request).orElseGet(() -> {

			String authHeader = request.getHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
				return authHeader.substring(7);
			}

			return null;
		});
	}

	private void autenticar(HttpServletRequest request, String token) {

		try {
			String username = jwtService.extractUsername(token);

			if (username == null || username.isBlank()) {
				return;
			}

			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (!jwtService.validateToken(token, userDetails)) {
				return;
			}

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());

			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (JwtException | IllegalArgumentException e) {
			log.debug("Token JWT rejeitado: {}", e.getMessage());
		} catch (UsernameNotFoundException e) {
			log.debug("Token válido, mas o usuário não existe mais: {}", e.getMessage());
		}
	}

}
