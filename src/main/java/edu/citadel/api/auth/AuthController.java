package edu.citadel.api.auth;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  @GetMapping("/api/me")
  public Map<String, Object> me(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return Map.of("authenticated", false);
    }
    // principal.getAttributes() contains whatever we returned from
    // CustomOAuth2UserService
    return Map.of(
        "authenticated", true,
        "user", principal.getAttributes());
  }

  // CSRF token endpoint (optional — CookieCsrfTokenRepository already puts token
  // in cookie,
  // but some SPAs prefer fetching it explicitly)
  @GetMapping("/csrf")
  public Map<String, String> csrf(org.springframework.security.web.csrf.CsrfToken token) {
    return Map.of("_csrf", token.getToken());
  }
}
