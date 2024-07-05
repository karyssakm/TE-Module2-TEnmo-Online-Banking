package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.apache.coyote.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
@RestController

public class AccountController {
    private final AccountDao accountDao;

    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }


    @RequestMapping(path = "/accounts", method = RequestMethod.GET)
    public List<Account> list() {
        return accountDao.getAllAccounts();
    }

    @RequestMapping(path = "/account/{userId}", method = RequestMethod.GET)
    public Account get(@PathVariable int userId) {
        Account account = accountDao.getAccountByUserId(userId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        } else {
            return account;
        }
    }

    @RequestMapping(path = "/account/{userId}/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(@PathVariable int userId) {
        return accountDao.getBalanceByUserId(userId);
    }
}


