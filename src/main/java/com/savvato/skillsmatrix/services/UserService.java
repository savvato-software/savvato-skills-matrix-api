package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.controllers.dto.UserRequest;
import com.savvato.skillsmatrix.entities.User;

import java.util.Optional;

public interface UserService {
	public Optional<User> createNewUser(UserRequest req, String preferredContactMethod);

	public Optional<User> find(String query);
	public Optional<User> findById(Long id);
	Optional<User> update(UserRequest request);

	User changeLostPassword(String pw, String phoneNumber, String challengeCode);
}
