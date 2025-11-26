package edu.citadel.utils;

import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.LoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountDelegateService implements AccountDelegate {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            String loginProviderString = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            String loginId = ((OAuth2User) authentication.getPrincipal()).getAttribute("id").toString();
            LoginProvider provider = LoginProvider.valueOf(loginProviderString.toUpperCase());
            Long providerId = Long.valueOf(loginId);
            Optional<Account> foundAccount =
                    accountRepository.findByLoginLoginIdAndLoginLoginProvider(providerId, provider);
            return foundAccount.orElse(
                    accountRepository.findByLoginLoginIdAndLoginLoginProvider(0L, LoginProvider.ROOT).get()
            );
        }
        return accountRepository.findByLoginLoginIdAndLoginLoginProvider(0L, LoginProvider.ROOT).get();
    }
}
