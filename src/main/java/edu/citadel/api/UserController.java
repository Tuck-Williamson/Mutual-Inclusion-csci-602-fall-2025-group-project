package edu.citadel.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Controller to give the front-end information as to whether the user
 * is logged in.
 * // TODO: Implement back-end logic for user authentication and session management.
 * // TODO: Use user information to relate to lists in the database (ACL's, etc).
 */
@RestController
public class UserController {


    // No ResponseEntity here, just a mapping to /user to check if logged in.
    @GetMapping("/api/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String username = principal.getAttribute("login");
            return Collections.singletonMap("name", username != null ? username : "User");
        }

        return Collections.singletonMap("name", "Guest");
    }
}
