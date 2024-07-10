package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.dao.DataAccessException;
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


    private JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Transfer> getAllPastTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                transfers.add(transfer);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Error accessing data from database", e);
        }
        return transfers;
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
    public Transfer sendBucks(TransferDto transferDto) {
        // this is so we make sure the we will not send money to our selfs
        if (transferDto.getAccountFrom() == transferDto.getAccountTo()) {
            throw new IllegalArgumentException("Cannot send money to yourself");
        }
       //  we have to make sure that we have enough money
//        BigDecimal senderBalance = getBalanceByUserId(transferDto.getUserFrom());
//        if (senderBalance.compareTo(transferDto.getAmount()) < 0) {
//            throw new IllegalArgumentException("Insufficient balance");
//        }
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, transferDto.getAccountFrom(), transferDto.getAccountTo(), transferDto.getAmount());

//            //  this is for when we make sure to subtract from amount
//            updateBalance(transferDto.getUserFrom(), senderBalance.subtract(transferDto.getAmount()));
//
//            //  this is to add to the balance
//            BigDecimal receiverBalance = getBalanceByUserId(transferDto.getUserTo());
//            updateBalance(transferDto.getUserTo(), receiverBalance.add(transferDto.getAmount()));

            // this is what gets and returns the newly created transfer
            int transferId = jdbcTemplate.queryForObject(sql,Integer.class);
            return getTransferByTransferId(transferId);
        } catch (DataAccessException e) {
            throw new DaoException("Error sending TE Bucks", e);
        }
    }

//    private void updateBalance(int userId, BigDecimal newBalance) {
//        String sql = "UPDATE account SET balance = ? WHERE user_id = ?";
//        jdbcTemplate.update(sql, newBalance, userId);
//    }








    @Override
    public Transfer requestBucks(int accountFrom) {
        return null;
    }

    @Override
    public Transfer createTransfer(Transfer transferId) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, " +
                "account_from, account_to, amount) VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        try {
            int newTransferId = jdbcTemplate.queryForObject(sql, new Object[] {
                    transferId.getTransferTypeId(),
                    transferId.getTransferStatusId(),
                    transferId.getAccountFrom(),
                    transferId.getAccountTo(),
                    transferId.getAmount()
            }, Integer.class);
            transferId.setTransferId(newTransferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferId;
    }

//    @Override
//    public Transfer updateTransfer() {
//        return null;
//    }


    @Override
    public Transfer getTransferByTransferId(int transferId) {
        Transfer transfer = null;
        String sql = " SELECT * FROM transfer WHERE transfer_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);


        }
        return transfer;
    }

    @Override
    public void save(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                transfer.getTransferTypeId(),
                transfer.getTransferStatusId(),
                transfer.getAccountFrom(),
                transfer.getAccountTo(),
                transfer.getAmount());
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