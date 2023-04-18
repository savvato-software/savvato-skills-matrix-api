package com.savvato.skillsmatrix.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.savvato.skillsmatrix.entities.UserRoleMap;
import com.savvato.skillsmatrix.repositories.UserRepository;
import com.savvato.skillsmatrix.repositories.UserRoleMapRepository;

@Service
public class UserRoleMapServiceImpl implements UserRoleMapService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	UserRoleMapRepository userRoleMapRepo;
	
	public void addRoleToUser(Long userId, ROLES role) {
		userRoleMapRepo.save(new UserRoleMap(userId, Long.valueOf(role.ordinal()+"") ));
	}
	
	public void removeRoleFromUser(Long userId, ROLES role) {
		userRoleMapRepo.delete(new UserRoleMap(userId, Long.valueOf(role.ordinal()+"") ));
	}
}
