package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class TransferService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL;
    private String authToken = null;

    public TransferService(String apiUrl, RestTemplate restTemplate) {
        this.API_BASE_URL = apiUrl;
        this.restTemplate = restTemplate;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }



    public Transfer[] getAllPastTransfers(){
        Transfer[] transfers = null;
        try {
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<Transfer[]> response = restTemplate.exchange(
                    API_BASE_URL + "transfers",
                    HttpMethod.GET,
                    entity,
                    Transfer[].class
            );
            transfers = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }


public Transfer[] getAllPendingRequests() {
    Transfer[] transfers = null;
    try {
        HttpEntity<Void> entity = createAuthEntity();
        ResponseEntity<Transfer[]> response = restTemplate.exchange(
                API_BASE_URL + "transfer/pending",
                HttpMethod.GET,
                entity,
                Transfer[].class
        );
        transfers = response.getBody();
    } catch (RestClientResponseException e) {
        BasicLogger.log(e.getMessage());
    }
    return transfers;
}


    public Transfer getTransferByTransferId(){
        Transfer transferid = null;
        try {

            ResponseEntity<Transfer> response = restTemplate.exchange(
                    API_BASE_URL + "transfers",
//                            + transferId ,
                    HttpMethod.GET,
                    createAuthEntity(),
                    Transfer.class
            );
            transferid = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return transferid;

    }


    public Transfer[] getPendingTransfersByUserId(int userId) {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfer/pending" + userId, HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }


    public Transfer[] getTransfersByUserId(int userId) {
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfer/user/" + userId, HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            return response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            if (e instanceof RestClientResponseException && ((RestClientResponseException) e).getRawStatusCode() == 404) {
                BasicLogger.log("User ID " + userId + " not found.");
            } else {
                BasicLogger.log(e.getMessage());
            }
            return new Transfer[0];
        }
    }



    public Transfer sendBucks(TransferDto transferDto) {
        Transfer responseTransfer = null;
        try {
            HttpEntity<TransferDto> entity = new HttpEntity<>(transferDto, createAuthHeaders());
            ResponseEntity<Transfer> response = restTemplate.exchange(
                    API_BASE_URL + "transfer/send",
                    HttpMethod.POST,
                    entity,
                    Transfer.class
            );
            responseTransfer = response.getBody();
        } catch (RestClientResponseException e) {

        }
        return responseTransfer;
    }


    public Transfer createTransfer(Transfer transfer) {
        Transfer createdTransfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.postForEntity(API_BASE_URL + "transfer/create", makeTransferEntity(transfer), Transfer.class);
            createdTransfer = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return createdTransfer;
    }

    public Transfer sendBucks(Transfer transfer) {
        Transfer returnedTransfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.postForEntity(API_BASE_URL + "transfer/send", makeTransferEntity(transfer), Transfer.class);
            returnedTransfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return returnedTransfer;
    }


    public Transfer requestBucks(Transfer transfer) {
        HttpEntity<Transfer> entity = makeTransferEntity(transfer);
        Transfer returnedTransfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "transfer/request", HttpMethod.POST, entity, Transfer.class);
            returnedTransfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return returnedTransfer;
    }


    public String approveTransfer(int transferId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "transfer/approve", HttpMethod.POST, makeAuthEntityWithBody("transferId", transferId), String.class);
            return response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
            return "Error: Unable to approve transfer";
        }
    }

    public String rejectTransfer(int transferId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "transfer/reject", HttpMethod.POST, makeAuthEntityWithBody("transferId", transferId), String.class);
            return response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
            return "Error: Unable to reject transfer";
        }
    }


    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Map<String, Integer>> makeAuthEntityWithBody(String key, int value) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        Map<String, Integer> body = Map.of(key, value);
        return new HttpEntity<>(body, headers);
    }
}
