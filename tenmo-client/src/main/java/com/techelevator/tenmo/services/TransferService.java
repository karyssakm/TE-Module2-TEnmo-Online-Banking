package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class TransferService {
    private static final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL;
    private static String authToken = null;

    public TransferService(String apiUrl) {
        this.API_BASE_URL = apiUrl;
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
                    API_BASE_URL + "transfers" transferId ,
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
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }
    private static HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }
}
