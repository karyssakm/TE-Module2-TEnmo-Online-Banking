package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;

import java.util.List;

public interface TransferDao {

    List<Transfer> getAllPastTransfers();

    List<Transfer> getAllPendingRequests(int userId);

    List<Transfer> getPendingTransfersByUserId(int userId);

    List<Transfer> getCurrentPendingTransfersByUserId(int userId);
    List<Transfer> getTransfersByAccountId(int accountId);

    Transfer createTransfer(Transfer transfer);

    Transfer getTransferByTransferId(int transferId);


    Transfer sendBucks(Transfer transfer);

    Transfer requestBucks(Transfer transfer);



    void updateTransferStatus(Transfer transfer);

    Transfer save(Transfer transfer);

    Transfer findByAccountId(int accountId);


}
















