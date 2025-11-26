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

    public Account setNewCurrentAccount(Long id, String username, Long login_id) {
        this.currentAccount = new Account();
        this.currentAccount.setUser_id(id);
        this.currentAccount.setUsername(username);
        Login login = new Login();
        login.setLoginId(login_id);
        login.setLoginProvider(LoginProvider.ROOT);
        this.currentAccount.setLogin(login);
        this.currentAccount.setCreated_on(Timestamp.valueOf(LocalDateTime.now()));
        this.currentAccount.setLast_login(Timestamp.valueOf(LocalDateTime.now()));
        accountRepository.save(this.currentAccount);
        return this.currentAccount;
    }

    @Override
    public Account getCurrentAccount() {
        return currentAccount;
    }
}
