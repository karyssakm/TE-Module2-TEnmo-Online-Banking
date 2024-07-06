package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;

import java.util.List;

public interface TransferDao {

    List<Transfer> getAllPastTransfers();

    List<Transfer> getAllPendingRequests();

    Transfer sendBucks(TransferDto transferDto);

    Transfer requestBucks(int accountFrom);

    Transfer createTransfer(Transfer transferId);

    void save(Transfer transfer);



    //should we put updateAccountAferTransfer into TransferDao or into AccountDao?
   // Transfer updateTransfer();


    Transfer getTransferByTransferId(int transferId);
}














//Transfer getTransferById(int transferId);

///List<Transfer> getTransfersByAccountId(int accountId);

