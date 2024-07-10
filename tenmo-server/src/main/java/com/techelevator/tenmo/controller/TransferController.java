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
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private final TransferDao transferDao;
    private final AccountDao accountDao;
    private final UserDao userDao;

    public TransferController(TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
    }
    @PreAuthorize("permitAll()")
    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> list() {
        return transferDao.getAllPastTransfers();
    }

    @PreAuthorize("permitAll()")
    @RequestMapping(path = "/transfer/pending/{userId}", method = RequestMethod.GET)
    public List<Transfer> getPendingRequest(@PathVariable int userId) {

        List<Transfer> pendingTransfer = null;
        try {
            pendingTransfer = transferDao.getAllPendingRequests( userId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return pendingTransfer;
    }
   // @ResponseStatus(HttpStatus.CREATED)
   @PreAuthorize("permitAll()")
    @PostMapping(path = "/transfer/send")
    public ResponseEntity<String> sendBucks(@RequestBody TransferDto transferDto) {
        try {
            Transfer newTransfer = transferDao.sendBucks(transferDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("TE Bucks sent successfully");
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }
    @PreAuthorize("permitAll()")
    @RequestMapping(path = "/transfer/{transferId}", method = RequestMethod.GET)
    public ResponseEntity<Transfer> getTransferById(@PathVariable int transferId) {
        Transfer transfer = transferDao.getTransferByTransferId(transferId);
        if (transfer != null) {
            return ResponseEntity.ok(transfer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    //Add method to getAllTransfers (list)


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfer/create", method = RequestMethod.POST)
    public Transfer createTransfer(@Valid @RequestBody TransferDto transferDto, Principal principal) {
        Transfer transfer = buildTransferFromTransferDTO(transferDto);
//        validateAuthorizationToCreate(principal, transfer);
        if (transfer.getTransferStatusId() == 2) {          //2 is approved
            transferBucksBetweenAccounts(transfer);     //add in method here to transfer between accounts
        }
        try {
            return transferDao.createTransfer(transfer);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void transferBucksBetweenAccounts(Transfer transfer) {
        Account fromAccount = accountDao.getAccountById(transfer.getAccountFrom());
        Account toAccount = accountDao.getAccountById(transfer.getAccountTo());

        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("One or both accounts do not exist");
        }

        if (fromAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));

        accountDao.update(fromAccount);
        accountDao.update(toAccount);
    }

    private Transfer buildTransferFromTransferDTO(TransferDto transferDto) {
        Transfer transfer = new Transfer();
        transfer.setAccountFrom(transferDto.getAccountFrom());
        transfer.setAccountTo(transferDto.getAccountTo());
        transfer.setAmount(transferDto.getAmount());

        if ("Request".equalsIgnoreCase(transferDto.getType())) {
            transfer.setTransferTypeId(1); // Request
        } else if ("Send".equalsIgnoreCase(transferDto.getType())) {
            transfer.setTransferTypeId(2); // Send
        } else {
            throw new IllegalArgumentException("Invalid transfer type");
        }

        transfer.setTransferStatusId(1); // Pending
        return transfer;
    }

//    private void validateAuthorizationToCreate(Principal principal, Transfer transfer) {
//        String username = principal.getName();
//        Account fromAccount = accountDao.existsById(transfer.getAccountFrom());
//        if (fromAccount == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From account does not exist.");
//        }
//
//        int ownerId = fromAccount.getUserId();
//        User user = userDao.getUserByUsername(username);
//
//        if (user == null || user.getId() != ownerId) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot create this transfer.");
//        }
//    }






}
