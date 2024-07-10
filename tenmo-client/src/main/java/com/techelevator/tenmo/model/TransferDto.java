package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferDto {
    private int accountFrom;
    private int accountTo;
    private String type;


//    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;

//    @NotEmpty
//    private String transferFrom;


    public int getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
