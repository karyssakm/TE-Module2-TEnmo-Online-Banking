package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;

import java.util.List;

public interface TransferDao {

    List<Transfer> getAllPastTransfers();

    List<Transfer> getAllPendingRequests(int userId);

    Transfer sendBucks(TransferDto transferDto);

    Transfer requestBucks(int accountFrom);

    Transfer createTransfer(Transfer transferId);

    void save(Transfer transfer);
    Transfer getTransferByTransferId(int transferId);


    //should we put updateAccountAferTransfer into TransferDao or into AccountDao?
   // Transfer updateTransfer();


}
















