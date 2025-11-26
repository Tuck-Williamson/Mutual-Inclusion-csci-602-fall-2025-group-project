package edu.citadel.config;

import edu.citadel.api.service.MutInkOAuth2AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    MutInkOAuth2AccountService accountService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/h2-console/**",
                        "webjars/**");
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                      authorize
                              .requestMatchers("/",
                                      "/index.html",
                                      "/js/**",
                                      "/css/**",
                                      "/img/**",
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
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(accountService))
                        .defaultSuccessUrl("/", true))
                .exceptionHandling(e ->
                       e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(AbstractHttpConfigurer::disable).logout(
                        logout -> logout.logoutSuccessUrl("/").permitAll());// TODO: Adjust logout URL as needed
        return http.build();
    }
}
