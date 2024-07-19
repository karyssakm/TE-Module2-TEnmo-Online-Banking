package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.TransferDao;
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
    private final TransferDao transferDao;


    public AccountController(AccountDao accountDao, TransferDao transferDao) {
        this.accountDao = accountDao;
        this.transferDao = transferDao;
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


    @RequestMapping(path = "/account/{accountId}/balance", method = RequestMethod.PUT)
    public ResponseEntity<Account> updateBalance(@PathVariable int accountId, @RequestBody BigDecimal amount) {
        try {
            Account account = accountDao.getAccountById(accountId);
//            account.setBalance(account.getBalance().add(amount));
            account.setBalance(amount);
            Account updatedAccount = accountDao.updateBalance(account);
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update balance", e);
        }
    }


    @RequestMapping(path = "/account/{accountId}/balance/add", method = RequestMethod.PUT)
    public ResponseEntity<Account> addBalance(@PathVariable int accountId, @RequestBody BigDecimal amount) {
        try {
            Account account = accountDao.getAccountById(accountId);
            account.setBalance(account.getBalance().add(amount));
//            account.setBalance(amount);
            accountDao.updateBalance(account);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update balance", e);
        }
    }

    @RequestMapping(path = "/account/{accountId}/balance/subtract", method = RequestMethod.PUT)
    public ResponseEntity<Account> subtractBalance(@PathVariable int accountId, @RequestBody BigDecimal amount) {
        try {
            Account account = accountDao.getAccountById(accountId);
            account.setBalance(account.getBalance().subtract(amount));
//            account.setBalance(amount);
            accountDao.updateBalance(account);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update balance", e);
        }
    }



    @RequestMapping(path = "/{accountId}", method = RequestMethod.GET)
    public ResponseEntity<Account> getAccountById(@PathVariable int accountId) {
        try {
            Account account = accountDao.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving account", e);
        }
    }





}


