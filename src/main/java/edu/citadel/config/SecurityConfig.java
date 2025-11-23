package edu.citadel.config;

import edu.citadel.security.CustomCsrfTokenRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                      authorize
                              .requestMatchers("/oauth2/**").permitAll()
                              .requestMatchers("/login/**", "/logout/**").permitAll()
                              .requestMatchers("/",
                                      "/index.html",
                                      "/js/**",
                                      "/webjars/**",
                                      "/error",
                                      "/server/**",
                                      "/accounts/**",
                                      "/favicon/ico" )
                              .permitAll()
                              .requestMatchers(
                                      // TODO: Add way for front-end to access these endpoints without authentication
                                      "/api/user",
                                      "/ws/**",
                                      "/list/**"
                              ).permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/?error=auth_failed")
                )
                .exceptionHandling(e ->
                       e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(csrf -> csrf
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .csrfTokenRequestHandler(new CustomCsrfTokenRequestHandler())
                ).logout(
                        logout -> logout.logoutSuccessUrl("/").permitAll());// TODO: Adjust logout URL as needed
        return http.build();
    }
}
