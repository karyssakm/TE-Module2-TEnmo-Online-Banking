package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;
    private final AccountDao accountDao;



    public JdbcTransferDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }


    @Override    //getAllTransfers
    public List<Transfer> getAllPastTransfers() {
        List<Transfer> pastTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                pastTransfers.add(mapRowToTransfer(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return pastTransfers;
    }


    @Override
    public List<Transfer> getTransfersByAccountId(int accountId) {
        List<Transfer> transferByAccountId = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE account_from = ? OR account_to = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                transferByAccountId.add(transfer);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error accessing data from database", e);

        }
        return transferByAccountId;
    }


    @Override
    public List<Transfer> getPendingTransfersByUserId(int userId) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE (account_from IN (SELECT account_id FROM account WHERE user_id = ?) " +
                "OR account_to IN (SELECT account_id FROM account WHERE user_id = ?)) " +
                "AND transfer_status_id = 1";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                pendingTransfers.add(mapRowToTransfer(results));
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error accessing data from database", e);
        }
        return pendingTransfers;
    }


    @Override
    public List<Transfer> getCurrentPendingTransfersByUserId(int userId) {
        List<Transfer> currentPendingTransfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id " +
                "WHERE a.user_id = ? AND t.transfer_status_id = 1";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                currentPendingTransfers.add(transfer);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error accessing data from database", e);

        }
        return currentPendingTransfers;
    }







    @Override
    public List<Transfer> getAllPendingRequests(int userId) {
        List<Transfer> pendingRequests = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.account_from, t.account_to, t.transfer_status_id, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id OR t.account_to = a.account_id " +
                "WHERE a.user_id = ? AND t.transfer_status_id = 1";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                pendingRequests.add(transfer);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error retrieving pending requests", e);
        }

        return pendingRequests;

    }


    @Override
    public Transfer sendBucks(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    transfer.getTransferTypeId(),
                    transfer.getTransferStatusId(),
                    transfer.getAccountFrom(),
                    transfer.getAccountTo(),
                    transfer.getAmount());

            updateAccountBalances(transfer);
        } catch (DataAccessException e) {
            throw new DaoException("Error sending TE Bucks", e);
        }
        return transfer;
    }


    @Override
    public Transfer findByAccountId(int accountId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE account_from = ? OR account_to = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
            if (results.next()) {
                return mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataAccessException e) {
            throw new DaoException("Error retrieving transfer by account ID", e);
        }
        return null;
    }





    @Override
    public Transfer createTransfer(Transfer transfer) {
        Transfer newTransfer = null;

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try {
            int newTransferId = jdbcTemplate.queryForObject(sql, int.class, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
            newTransfer = getTransferByTransferId(newTransferId);
        } catch (DataAccessException e) {
            throw new DaoException("Error creating transfer", e);
        }
        return newTransfer;
    }


    @Override
    public Transfer getTransferByTransferId(int transferId) {
        Transfer transferByTransferId = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, " +
                "account_from, account_to, amount FROM transfer WHERE " +
                "transfer_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()) {
                transferByTransferId = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);


        }
        return transferByTransferId;
    }


    @Override
    public Transfer requestBucks(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try {
            Integer newTransferId = jdbcTemplate.queryForObject(sql, Integer.class,
                    transfer.getTransferTypeId(),
                    transfer.getTransferStatusId(),
                    transfer.getAccountFrom(),
                    transfer.getAccountTo(),
                    transfer.getAmount());
            if (newTransferId != null) {
                transfer.setTransferId(newTransferId);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error requesting TE Bucks", e);
        }
        return transfer;
    }

    private void updateAccountBalances(Transfer transfer) {
        String updateFromAccountSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
        String updateToAccountSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";

        try {
            jdbcTemplate.update(updateFromAccountSql, transfer.getAmount(), transfer.getAccountFrom());
            jdbcTemplate.update(updateToAccountSql, transfer.getAmount(), transfer.getAccountTo());
        } catch (DataAccessException e) {
            throw new DaoException("Error updating account balances", e);
        }
    }



    @Override
    public void updateTransferStatus(Transfer transfer) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        try {
            jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
        } catch (Exception e) {
            throw new DaoException("Error updating transfer status", e);
        }
    }



    @Override
    public Transfer save(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        Integer newTransferId = jdbcTemplate.queryForObject(sql, Integer.class,
                transfer.getTransferTypeId(),
                transfer.getTransferStatusId(),
                transfer.getAccountFrom(),
                transfer.getAccountTo(),
                transfer.getAmount());
        if (newTransferId != null) {
            transfer.setTransferId(newTransferId);
        }
        return transfer;
    }



    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
        transfer.setAccountFrom(rs.getInt("account_from"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        return transfer;
    }
}