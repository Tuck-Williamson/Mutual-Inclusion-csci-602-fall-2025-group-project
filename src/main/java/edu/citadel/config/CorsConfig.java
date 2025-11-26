package edu.citadel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    // Override via profile (application-do-app.properties) for production
    @Value("${app.domain.origin:http://localhost:5001}")
    private String allowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // allow credentials so cookies (session) are sent
        config.setAllowCredentials(true);

        // allowed origins - using exact origin (not "*") because allowCredentials=true
        config.setAllowedOrigins(List.of(allowedOrigin));

        // allow any header and method; tighten later if you want
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedMethod(CorsConfiguration.ALL);

        // optionally expose headers (e.g., for XSRF tokens or custom headers)
        config.addExposedHeader("XSRF-TOKEN");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // apply to all endpoints
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
