package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Account;
import com.techelevator.util.BasicLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {
  //  private static final String API_BASE_URL=  "http://localhost:8080/";
    private static final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL;
    private static String authToken = null;
    // private AccountDao accountDao;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }
    public AccountService(String apiUrl){
        this.API_BASE_URL = apiUrl;
    }
    public Account[] getAllAccount(int userid){
        Account[] accounts = new Account[]{new Account()};
        try{
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL+ "accounts/" +userid,HttpMethod.GET, entity, Account.class);
            response.getBody();
        }catch (RestClientResponseException  e){
            BasicLogger.log(e.getMessage());
        }
        return accounts;
    }
    public Account getAccountById(int accountId){
        Account account= new Account();
        try{
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL+ "accountId/" +accountId,HttpMethod.GET, entity, Account.class);
            account = response.getBody();
        }catch (RestClientResponseException  e){
            BasicLogger.log(e.getMessage());
        }
        return account;

    }

    public  BigDecimal getBalanceByUserId(int userId) {
        BigDecimal balance = null;
        try {
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "/account/{userId}/balance", HttpMethod.GET, entity, BigDecimal.class, userId);
            balance = response.getBody();
//            BasicLogger.log("Your current balance is: " + balance);
        } catch (Exception e) {
            BasicLogger.log("Error fetching balance: " + e.getMessage());
        }
        return balance;
    }




    private static HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


}


