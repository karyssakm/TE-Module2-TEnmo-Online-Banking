package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
//@PreAuthorize("isAuthorized()")
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
    public ResponseEntity<List<Transfer>> getAllPastTransfers() {
        List<Transfer> pastTransfers = transferDao.getAllPastTransfers();
        return ResponseEntity.ok(pastTransfers);
    }
    @RequestMapping(path = "/transfer/pending", method = RequestMethod.GET)
    public ResponseEntity<List<Transfer>> getAllPendingRequests() {
        List<Transfer> pendingRequests = transferDao.getAllPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @RequestMapping(path = "/transfer/send", method = RequestMethod.POST)
    public ResponseEntity<String> sendBucks(@RequestBody TransferDto transferDto) {
        try {
            Transfer newTransfer = transferDao.sendBucks(transferDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("TE Bucks sent successfully");
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    //Add method to getAllTransfers (list)




}
