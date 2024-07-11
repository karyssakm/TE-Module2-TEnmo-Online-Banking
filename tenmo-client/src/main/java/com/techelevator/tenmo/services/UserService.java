package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UserService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL;


    @Autowired
    public UserService(String apiUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.API_BASE_URL = apiUrl;
    }


    public User getUserById(int userId) {
        User user = null;
        try {
            ResponseEntity<User> response = restTemplate.exchange(API_BASE_URL + "/user/" + userId, HttpMethod.GET, null, User.class);
            user = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }


    public List<User> getAllUsers() {
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "/users", HttpMethod.GET, null, User[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : new ArrayList<>();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
            return new ArrayList<>();
        }
    }

}

