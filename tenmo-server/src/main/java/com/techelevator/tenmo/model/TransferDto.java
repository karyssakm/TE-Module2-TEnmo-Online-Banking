package com.techelevator.tenmo.model;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

public class TransferDto {
    private int userFrom;
    private int userTo;
    private int userId;

    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;

//    @NotEmpty
//    private String transferFrom;

    public int getUserFrom(){
        return userFrom;
    }
    public void setUserFrom(int userFrom){
        this.userFrom= userFrom;
    }

    public int getUserTo() {
        return userTo;
    }

    public void setUserTo(int userTo) {
        this.userTo = userTo;
    }
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public int getUserId(){
        return userId;
    }
    public void setUserId (int userId){
        this.userId = userId;
    }
}
