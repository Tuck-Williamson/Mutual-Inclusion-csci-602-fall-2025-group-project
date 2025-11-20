package edu.citadel.api;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class DebugController {

  private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

  @Value("${spring.security.oauth2.client.registration.github.client-id:client-id-not-set}")
  private String clientId;

  @GetMapping("/debug/client-id")
  public Map<String, String> getClientId() {
    logger.info("Getting clientId");
    return Map.of("clientId", clientId);
  }

  @GetMapping("/login")
  public Map<String, String> login(HttpServletRequest request, @AuthenticationPrincipal OAuth2User principal) {
    logger.info("Login endpoint accessed");
    return Map.of("message", "Login successful");
  }
  @GetMapping("/debug/me")
  public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
    return user == null
        ? Map.of("user", "null")
        : Map.of("attributes", user.getAttributes());
  }
}
