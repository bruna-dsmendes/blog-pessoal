package com.generation.blogpessoal.security;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.generation.blogpessoal.model.Usuario;

public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final String username;
	private final String password;
	private final transient LocalDateTime senhaAlteradaEm;

	public UserDetailsImpl(Usuario user) {
		this.username = user.getUsuario();
		this.password = user.getSenha();
		this.senhaAlteradaEm = user.getSenhaAlteradaEm();
	}

	/** Usada pelo filtro para descartar token emitido antes da troca de senha. */
	public LocalDateTime getSenhaAlteradaEm() {
		return senhaAlteradaEm;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return Collections.emptyList();  
	}

	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public String getUsername() {

		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}