package com.techelevator.tenmo.dao;

public class TransferStatus {
    //public static final String TRANSFER_STATUS_APPROVED = "Approved" ;
    private int statusId;
    private String statusDesc;

    public TransferStatus(int statusId, String statusDesc) {
        this.statusId = statusId;
        this.statusDesc = statusDesc;
    }

    public static void setTransferStatus(TransferStatus transferStatus) {
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusDescription() {
        return statusDesc;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDesc= statusDescription;
    }
}
