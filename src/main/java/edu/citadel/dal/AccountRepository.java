package edu.citadel.dal;

import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.LoginProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByLoginLoginIdAndLoginLoginProvider(Long loginId, LoginProvider loginProvider);
}
