package edu.citadel.utils;

import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.Login;
import edu.citadel.dal.model.LoginProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Primary
public class TestAccountDelegate implements AccountDelegate {
    @Override
    public Account getCurrentAccount() {
        Account account = new Account();
        account.setUser_id(0L);
        account.setUsername("Guest");
        Login login = new Login();
        login.setLoginId(0L);
        login.setLoginProvider(LoginProvider.ROOT);
        account.setLogin(login);
        account.setCreated_on(Timestamp.valueOf(LocalDateTime.now()));
        account.setLast_login(Timestamp.valueOf(LocalDateTime.now()));
        return account;
    }
}
