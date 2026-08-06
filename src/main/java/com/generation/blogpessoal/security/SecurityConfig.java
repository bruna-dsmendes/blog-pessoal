package com.generation.blogpessoal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] ENDPOINTS_PUBLICOS = {
			"/usuarios/logar",
			"/usuarios/cadastrar",
			"/error/**",
			"/", "/docs", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**"
	};

	private final JwtAuthFilter jwtAuthFilter;
	private final CorsConfigurationSource corsConfigurationSource;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers(ENDPOINTS_PUBLICOS).permitAll()
						.requestMatchers(HttpMethod.OPTIONS).permitAll()
						.requestMatchers(HttpMethod.GET, "/postagens/**", "/temas/**").permitAll()

						.anyRequest().authenticated())

				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> response.sendError(
								HttpServletResponse.SC_UNAUTHORIZED,
								"Não autorizado - Token JWT ausente ou inválido"))
						.accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(
								HttpServletResponse.SC_FORBIDDEN,
								"Acesso negado")))

				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

}
