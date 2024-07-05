package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface TransferDao {

    List<Transfer> getAllPastTransfers();

    List<> getAllPendingRequests();


    Transfer sendBucks(int accountTo);

    

    Transfer requestBucks(int accountFrom);

    Transfer createTransfer(int transferId);




    //should we put updateAccountAferTransfer into TransferDao or into AccountDao?
    Transfer updateTransfer();





}
