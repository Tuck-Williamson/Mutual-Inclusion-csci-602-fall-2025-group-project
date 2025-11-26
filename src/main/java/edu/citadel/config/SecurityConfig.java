package edu.citadel.config;

import edu.citadel.api.service.MutInkOAuth2AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    MutInkOAuth2AccountService accountService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Only ignore truly static assets / docs / dev consoles.
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/h2-console/**",
                        "/webjars/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/favicon.ico"
                );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Ensure Spring Security uses the CorsConfigurationSource bean
                .cors(Customizer.withDefaults())

                // Keep CSRF disabled for now (you said you'd enable/handle CSRF later).
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(authorize ->
                        authorize
                                // MUST permit OPTIONS preflight first
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/login/**", "/logout/**").permitAll()

                                // health / server endpoints can be open if desired
                                .requestMatchers("/server/status", "/server/**").permitAll()

                                // static / SPA entrypoints
                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/js/**",
                                        "/css/**",
                                        "/img/**",
                                        "/error",
                                        "/accounts/**",
                                        "/favicon.ico"
                                ).permitAll()

                                // endpoints you previously wanted accessible (adjust as needed)
                                .requestMatchers("/api/user", "/ws/**", "/list/**").permitAll()

                                // everything else requires authentication
                                .anyRequest().authenticated()
                )

                // OAuth2 login (keeps original behaviour; change defaultSuccessUrl if you want)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(accountService))
                        .defaultSuccessUrl("/", false) // don't force; let savedRequest work
                )

                // Return 401 for unauthenticated API calls
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Logout
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}
