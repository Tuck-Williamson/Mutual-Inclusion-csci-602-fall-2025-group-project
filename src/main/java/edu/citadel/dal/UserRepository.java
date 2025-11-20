package edu.citadel.dal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.citadel.dal.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
