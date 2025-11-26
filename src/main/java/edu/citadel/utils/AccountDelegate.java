package edu.citadel.utils;

import edu.citadel.dal.model.Account;

@FunctionalInterface
public interface AccountDelegate {
    Account getCurrentAccount();
}
