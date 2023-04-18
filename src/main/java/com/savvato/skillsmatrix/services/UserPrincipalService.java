package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.config.principal.UserPrincipal;

public interface UserPrincipalService {
	public UserPrincipal getUserPrincipalByName(String name);
	public UserPrincipal getUserPrincipalByEmail(String email);
}
