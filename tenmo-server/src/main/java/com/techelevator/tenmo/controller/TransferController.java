package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
public class TransferController {

    private final TransferDao transferDao;
    private final AccountDao accountDao;
    private final UserDao userDao;

    public TransferController(TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
    }


    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> getAllPastTransfers() {
        List<Transfer> pastTransfers = null;
        try {
            pastTransfers = transferDao.getAllPastTransfers();
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No past transfers found.");
        }
        return pastTransfers;
    }


//    //check to what this method is doing
//    @PreAuthorize("permitAll()")
//    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
//    public List<Transfer> list() {
//        return transferDao.getAllPastTransfers();
//    }



    @RequestMapping(path = "/transfer/pending/{userId}", method = RequestMethod.GET)
    public List<Transfer> getPendingRequest(@PathVariable int userId) {

        List<Transfer> pendingTransfer = null;
        try {
            pendingTransfer = transferDao.getAllPendingRequests(userId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return pendingTransfer;
    }


    @RequestMapping(path = "/transfer/send", method = RequestMethod.POST)
    public Transfer sendBucks(@Valid @RequestBody Transfer transfer) {
        transfer.setTransferTypeId(2); // 2 for send
        transfer.setTransferStatusId(2); // 2 for approved
        return createTransfer(transfer);
    }



    @RequestMapping(path = "/transfer/{transferId}", method = RequestMethod.GET)
    public ResponseEntity<Transfer> getTransferByTransferId(@PathVariable int transferId) {
        try {
            Transfer transfer = transferDao.getTransferByTransferId(transferId);
            if (transfer != null) {
                return new ResponseEntity<>(HttpStatus.OK);
        } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve transfer.");
        }
    }



    @RequestMapping(path = "/transfer/request", method = RequestMethod.POST)
    public ResponseEntity<Transfer> requestBucks(@Valid @RequestBody Transfer transfer) {
        Transfer createdTransfer = null;
        try {
            validateTransfer(transfer);
            transfer.setTransferStatusId(1);  //pending
            createdTransfer = transferDao.createTransfer(transfer);
            return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create transfer request", e);
        }
    }

//    @RequestMapping(path = "transfer/{accountId}/", method = RequestMethod.GET)
//    public Transfer getTransferIdByAccountId(@PathVariable int accountId) {
//        Transfer transfer = transferDao.getTransferByTransferId();
//    }




    @RequestMapping(path = "/transfer/create", method = RequestMethod.POST)
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer) {
        Transfer createTransfer = null;
        try {
            createTransfer = transferDao.createTransfer(transfer);
            if (transfer == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer is null.");
            }
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transfer fail.");
        }
        return createTransfer;
    }


    @RequestMapping(path = "/transfer/approve", method = RequestMethod.POST)
    public ResponseEntity<Transfer> approveTransfer(@Valid @RequestBody Transfer transfer) {
        try {
            Transfer existingTransfer = transferDao.getTransferByTransferId(transfer.getTransferId());
            if (existingTransfer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
            }
            transfer.setTransferStatusId(2); // Approved
            transferDao.updateTransferStatus(existingTransfer); // Updating transferId
            return new ResponseEntity<>(existingTransfer, HttpStatus.OK);  //200 status is okay
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to approve transfer", e);
        }
    }



    @RequestMapping(path = "/transfer/reject", method = RequestMethod.POST)
    public ResponseEntity<Transfer> rejectTransfer(@Valid @RequestBody Transfer transfer) {
        try {
            Transfer existingTransfer = transferDao.getTransferByTransferId(transfer.getTransferId());
            if (existingTransfer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
            }
            existingTransfer.setTransferStatusId(3); // rejected
            transferDao.updateTransferStatus(existingTransfer); // Update transfer status in the database
            return new ResponseEntity<>(existingTransfer, HttpStatus.OK); // 200 status is okay
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to reject transfer", e);
        }
    }


    private void validateTransfer (Transfer transfer) {
        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be positive.");
        }
        if (transfer.getAccountFrom() == transfer.getAccountTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account.");
        }
        if (!accountDao.existsById(transfer.getAccountFrom()) || !accountDao.existsById(transfer.getAccountTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not exist.");
        }
    }



//private void transferBucksBetweenAccounts(Transfer transfer) {
//        Account fromAccount = accountDao.getAccountById(transfer.getAccountFrom());
//        Account toAccount = accountDao.getAccountById(transfer.getAccountTo());
//
//        if (fromAccount == null || toAccount == null) {
//            throw new RuntimeException("One or both accounts do not exist");
//        }
//
//        if (fromAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
//            throw new RuntimeException("Insufficient balance");
//        }
//
//        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
//        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));
//
//        accountDao.update(fromAccount);
//        accountDao.update(toAccount);
//    }

}
