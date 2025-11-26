package edu.citadel.api.service;
import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.Login;
import edu.citadel.dal.model.LoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MutInkOAuth2AccountService extends DefaultOAuth2UserService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);


        LoginProvider loginProvider = LoginProvider.valueOf(userRequest
                .getClientRegistration()
                .getRegistrationId()
                .toUpperCase());

        String providerIdStr = oAuth2User.getAttribute("id").toString();

        Long providerId = Long.parseLong(providerIdStr);

        Optional<Account> existingAccount
                = accountRepository.findByLoginLoginIdAndLoginLoginProvider(providerId, loginProvider);
        existingAccount.ifPresentOrElse(present -> {
                    present.setLast_login(Timestamp.valueOf(LocalDateTime.now()));
                    accountRepository.save(present);// Update
                }
                , () -> {
                    Account account = new Account();
                    account.setUsername(oAuth2User.getAttribute("login"));
                    account.setLast_login(Timestamp.valueOf(LocalDateTime.now()));
                    account.setCreated_on(Timestamp.valueOf(LocalDateTime.now()));
                    account.setEmail(oAuth2User.getAttribute("email"));
                    Login login = new Login();
                    login.setLoginId(providerId);
                    login.setLoginProvider(loginProvider);
                    account.setLogin(login);
                    accountRepository.save(account);
                });
        return oAuth2User;
        // TODO: exception handling when constraint errors arise

    }
}
