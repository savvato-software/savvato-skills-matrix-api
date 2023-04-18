package com.savvato.skillsmatrix.services;

import java.util.Optional;

import com.savvato.skillsmatrix.config.principal.UserPrincipal;
import com.savvato.skillsmatrix.entities.User;
import com.savvato.skillsmatrix.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceSKILLSMATRIX implements UserDetailsService {

	@Autowired
	UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> opt = userRepo.findByEmail(email);
		User rtn = null;

		if (opt.isPresent())
			rtn = opt.get();
		else
			throw new UsernameNotFoundException(email);

		return new UserPrincipal(rtn);
	}
}
