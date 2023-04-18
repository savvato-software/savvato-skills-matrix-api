package com.savvato.skillsmatrix.config;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import com.savvato.skillsmatrix.config.filters.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    
    private final JwtTokenFilter jwtTokenFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtTokenFilter jwtTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Enable CORS and disable CSRF
    	http = http.cors().and().csrf().disable();
    	
    	// Set session management to stateless
    	http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
    	
    	// Set unauthorized requests exception handler
    	http = http.exceptionHandling()
    			.authenticationEntryPoint(
    				(request, response, ex) -> {
    					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
    				}
    			).and();
    	
    	// Set permissions on endpoints
    	http.authorizeRequests()
    	.antMatchers(HttpMethod.POST, "/api/public/login").permitAll()
    	.antMatchers(HttpMethod.POST, "/api/public/user/new").permitAll()
        .antMatchers(HttpMethod.GET, "/api/public/user/isEmailAddressAvailable").permitAll()
        .antMatchers(HttpMethod.GET, "/api/public/user/isPhoneNumberAvailable").permitAll()
        .antMatchers(HttpMethod.GET, "/api/public/user/isUsernameAvailable").permitAll()
        .antMatchers(HttpMethod.POST, "/api/public/user/changeLostPassword").permitAll()
        .antMatchers(HttpMethod.GET, "/api/public/user/isUserInformationUnique").permitAll()
        .antMatchers(HttpMethod.POST, "/api/public/sendSMSChallengeCodeToPhoneNumber").permitAll()
        .antMatchers(HttpMethod.POST, "/api/public/isAValidSMSChallengeCode").permitAll()

    	.anyRequest().hasAnyRole("admin,accountholder");
    		
    	
    	// Add JWT token filter
    	http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
    
    //====================encoders
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //===================CORS
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD","GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
