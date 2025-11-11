package edu.citadel.api;

import edu.citadel.api.request.AccountRequestBody;
import edu.citadel.dal.AccountRepository;
import edu.citadel.dal.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody AccountRequestBody requestBody) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Account account = optionalAccount.get();
        if (requestBody.getUsername() != null) {
            account.setUsername(requestBody.getUsername());
        }
        if (requestBody.getEmail() != null) {
            account.setEmail(requestBody.getEmail());
        }

        Account updatedAccount = accountRepository.save(account);
        return ResponseEntity.ok(updatedAccount);
    }
}
