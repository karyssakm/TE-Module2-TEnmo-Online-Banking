package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    List<Account> getAllAccounts();

    Account getAccountById(int accountId);

    Account getAccountByUserId(int userId);
    BigDecimal getBalanceByUserId(int userId);

    boolean existsById(int accountId);

//    Account update(Account account);

    Account updateBalance(Account account);

    Account findByAccountId (int accountId);

}










