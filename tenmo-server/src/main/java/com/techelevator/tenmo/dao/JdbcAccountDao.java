package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {



    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Account account = mapRowToAccount(results);
                accounts.add(account);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error accessing data from database", e);
        }
        return accounts;
    }

    @Override
    public Account getAccountById(int accountId) {
        Account accounts = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                return mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return accounts;
    }

    @Override
    public Account getAccountByUserId(int userId){
        Account accounts = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            if (results.next()) {
                return mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return  accounts;
    }


    @Override
    public BigDecimal getBalanceByUserId(int userId) {
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        try {
            BigDecimal balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
            if (balance == null) {
                throw new DaoException("Balance not found for user ID: " + userId);
            }
            return balance;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("No balance found for user ID: " + userId, e);
        } catch (DataAccessException e) {
            throw new DaoException("Error retrieving balance for user ID: " + userId, e);
        }
    }

    @Override
    public boolean existsById(int accountId) {
        String sql = "SELECT COUNT(*) FROM account WHERE account_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
        return count != null && count > 0;
    }


    @Override
    public Account updateBalance(Account account) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        try {
            int numberOfRows = jdbcTemplate.update(sql, account.getBalance(), account.getAccountId());
            if (numberOfRows == 0) {
                throw new DaoException("Zero rows affected, expected at least one.");
            }
            return getAccountById(account.getAccountId());
        } catch (DataAccessException e) {
            throw new DaoException("Error updating balance for account ID: " + account.getAccountId(), e);
        }
    }


    @Override
    public Account findByAccountId(int accountId) {
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
        if (results.next()) {
            return mapRowToAccount(results);
        }
        return null;
    }

    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));

        return account;
    }
}













