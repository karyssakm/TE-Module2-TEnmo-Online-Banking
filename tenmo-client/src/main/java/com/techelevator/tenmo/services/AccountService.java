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

    private final RestTemplate restTemplate;
    private final String API_BASE_URL;
    private String authToken = null;



    public AccountService(String apiUrl, RestTemplate restTemplate) {
        this.API_BASE_URL = apiUrl;
        this.restTemplate = restTemplate;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
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


    public Account[] getAllAccounts() {
        Account[] accounts = null;
        try {
            ResponseEntity<Account[]> response = restTemplate.exchange(
                    API_BASE_URL + "/accounts", HttpMethod.GET, makeAuthEntity(), Account[].class);
            accounts = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return accounts;
    }



    public Account getAccountByUserId(int userId) {
        Account account = null;
        try {
            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "account/" + userId, HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

//    public Account getAccountByUserId(int userId) {
//        Account account = BigDecimal.ZERO;
//        try {
//            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "/account/" + userId, HttpMethod.GET, makeAuthEntity(), Account.class);
//            account = response.getBody();
//        } catch (RestClientResponseException e) {
//            BasicLogger.log(e.getMessage());
//        }
//        return account;
//    }


    public Account getAccountById(int accountId) {
        Account account = null;
        try {
            String url = API_BASE_URL + accountId;
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<Account> response = restTemplate.exchange(
                    url, HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }



    public  BigDecimal getBalanceByUserId(int userId) {
        BigDecimal balance = null;
        try {
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "account/" + userId + "/balance", HttpMethod.GET, entity, BigDecimal.class, userId);
            if (response.getStatusCode() == HttpStatus.OK) {
                balance = response.getBody();
            }
        } catch (Exception e) {
            BasicLogger.log("Error fetching balance: " + e.getMessage());
        }
        return balance;
    }


    public BigDecimal getBalanceByAccountId(int accountId) {
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                    API_BASE_URL + "/account/" + accountId + "/balance", HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }


    public void updateAccount(Account account) {
        try {
            String url = API_BASE_URL + "/account/" + account.getAccountId() + "/balance";
            HttpEntity<BigDecimal> entity = makeAuthEntityWithAmount(account.getBalance());
            restTemplate.exchange(url, HttpMethod.PUT, entity, Account.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log("Update account request failed: " + e.getMessage());
        }
    }


    public void addBalance(int accountId, BigDecimal amount) {
        try {
            String url = API_BASE_URL + "/account/" + accountId + "/balance/add";
            HttpEntity<BigDecimal> entity = makeAuthEntityWithAmount(amount);
            restTemplate.exchange(url, HttpMethod.PUT, entity, Account.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log("Add balance request failed: " + e.getMessage());
        }
    }
//
    public void subtractBalance(int accountId, BigDecimal amount) {
        try {
            String url = API_BASE_URL + "/account/" + accountId + "/balance/subtract";
            HttpEntity<BigDecimal> entity = makeAuthEntityWithAmount(amount);
            restTemplate.exchange(url, HttpMethod.PUT, entity, Account.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log("Subtract balance request failed: " + e.getMessage());
        }
    }







    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<BigDecimal> makeAuthEntityWithAmount(BigDecimal amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(amount, headers);
    }



}


