package edu.citadel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import edu.citadel.services.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).oauth2Login(custom -> {
    }).oauth2Login(custom -> {
      // TODO: Add login logic here
    })
        .csrf(csrf -> csrf.disable());
    return http.build();

  }

  // Define bean or method for custom user service (see class below)
  @Bean
  public CustomOAuth2UserService customOAuth2UserService() {
    return new CustomOAuth2UserService();
  }
}
