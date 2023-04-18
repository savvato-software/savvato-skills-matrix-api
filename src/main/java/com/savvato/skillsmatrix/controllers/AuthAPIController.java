package com.savvato.skillsmatrix.controllers;

import javax.validation.Valid;

import com.savvato.skillsmatrix.config.principal.UserPrincipal;
import com.savvato.skillsmatrix.controllers.dto.AuthRequest;
import com.savvato.skillsmatrix.entities.User;
import com.savvato.skillsmatrix.services.AuthServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class AuthAPIController {

    private final AuthenticationManager authenticationManager;

    public AuthAPIController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping(value = { "/api/public/login" }, method = RequestMethod.POST)
    public ResponseEntity<User> login(@RequestBody @Valid AuthRequest request) {
        try {
            Authentication authenticate = authenticationManager
                .authenticate(
                    new UsernamePasswordAuthenticationToken(
                        request.email, request.password
                    )
                );

            User user = ((UserPrincipal) authenticate.getPrincipal()).getUser();

            return ResponseEntity.ok()
                .header(
                    HttpHeaders.AUTHORIZATION,
                    AuthServiceImpl.generateAccessToken(user)
                )
                .body(user);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
}
