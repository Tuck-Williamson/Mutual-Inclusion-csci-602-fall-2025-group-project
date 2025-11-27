package edu.citadel.utils;

import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.model.Account;
import edu.citadel.dal.model.Login;
import edu.citadel.dal.model.LoginProvider;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Setter
@Component
@Primary
public class TestAccountDelegate implements AccountDelegate {

    private Account currentAccount;

    @Autowired
    private AccountRepository accountRepository;

    public TestAccountDelegate() {
        this.currentAccount = new Account();
        this.currentAccount.setUser_id(0L);
        this.currentAccount.setUsername("Guest");
        Login login = new Login();
        login.setLoginId(0L);
        login.setLoginProvider(LoginProvider.ROOT);
        this.currentAccount.setLogin(login);
        this.currentAccount.setCreated_on(Timestamp.valueOf(LocalDateTime.now()));
        this.currentAccount.setLast_login(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public Account getCurrentAccount() {
        return currentAccount;
    }
}
