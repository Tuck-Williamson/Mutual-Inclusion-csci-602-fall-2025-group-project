package edu.citadel.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import edu.citadel.dal.UserRepository;
import edu.citadel.dal.model.User;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // GitHub typical attributes include: id, login, name, email, avatar_url
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "github"
        String providerId = String.valueOf(attrs.get("id"));
        String username = (String) attrs.getOrDefault("login", "unknown");
        String displayName = (String) attrs.getOrDefault("name", username);
        String email = (String) attrs.get("email"); // may be null; could call user/emails endpoint if needed
        String avatar = (String) attrs.get("avatar_url");

        // Upsert local user
        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            user.setUsername(username);
            user.setDisplayName(displayName);
            if (email != null) user.setEmail(email);
            user.setAvatarUrl(avatar);
        } else {
            user = new User();
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setAvatarUrl(avatar);
            // set defaults if you need roles etc
        }
        user = userRepository.save(user);

        // Create authorities
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

        Map<String, Object> userAttributes = Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "displayName", user.getDisplayName(),
//            "email", user.getEmail(), // TODO: find a way to handle the null email case
            "avatarUrl", user.getAvatarUrl()
        );

        return new DefaultOAuth2User(Collections.singleton(authority), userAttributes, "username");
    }
}

