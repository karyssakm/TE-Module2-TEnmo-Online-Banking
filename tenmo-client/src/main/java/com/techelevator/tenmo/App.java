package com.techelevator.tenmo;


import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL, restTemplate);
    private final TransferService transferService = new TransferService(API_BASE_URL, restTemplate);
    private final UserService userService = new UserService(API_BASE_URL, restTemplate);
    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            }else if (menuSelection == 6) {
                transferByTransferId();
            }else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        if (currentUser != null) {
            BigDecimal balance = accountService.getBalanceByUserId(currentUser.getUser().getId());
            System.out.println("Your current balance is: $ " +balance );
        } else {
            System.out.println("User not authenticated.");
        }
    }

    private void transferByTransferId(){
        if (currentUser != null) {
            int transferId = consoleService.promptForTransferId(); // Implement this method in ConsoleService to prompt for transfer ID
            Transfer transfer = transferService.getTransferByTransferId();
            if (transfer != null) {
                System.out.println("Transfer Details:");
                System.out.println("Transfer ID: " + transfer.getTransferId());
                System.out.println("Amount: $" + transfer.getAmount());
                System.out.println("From: " + transfer.getAccountFrom());
                System.out.println("To: " + transfer.getAccountTo());

            } else {
                System.out.println("Transfer not found or you do not have access to this transfer.");
            }
        } else {
            System.out.println("User not authenticated.");
        }
    }

	private void viewTransferHistory() {

        if (currentUser != null) {
            Transfer[] transfers = transferService.getAllPastTransfers();
            if (transfers != null && transfers.length > 0) {
                System.out.println("------- Transfer History -------");
                System.out.println("ID :        From/To           Amount   " );
                System.out.println("--------------------------------");

                for(Transfer transfer: transfers) {
                    //Transfer transferred = transferService.getTransferByTransferId();
//                    System.out.println(transfer.getTransferId(),transfer.getAccountFrom(),transfer.getAccountTo(), transfer.getAmount());
//                    System.out.println("Transfers");
//                    System.out.println("Amount: $" + transfer.getAmount());
//                    System.out.println("From: " + transfer.getAccountFrom());
//                    System.out.println("To: " + transfer.getAccountTo());
//                    System.out.println("ID : " + transfer.getTransferId());
//                    System.out.println("--------------------------------");
                }
            } else {
                System.out.println("No transfers found.");
            }
        } else {
            System.out.println("User not authenticated.");
        }
	}

	private void viewPendingRequests() {
        if (currentUser != null) {
            Transfer[] pendingRequests = transferService.getAllPendingRequests();
            if (pendingRequests != null && pendingRequests.length > 0) {
                System.out.println("Pending Transfer Requests:");
                for (Transfer transfer : pendingRequests) {
                    System.out.println(transfer);
                }
            } else {
                System.out.println("No pending transfer requests found.");
            }
        } else {
            System.out.println("User not authenticated.");
        }
	}

    private void sendBucks() {
        try {
            Account[] accountsList = accountService.getAllAccounts();

            if (accountsList != null) {
                System.out.println("-------------------------------------------");
                System.out.println("Accounts");
                System.out.println("Account ID             Name");
                System.out.println("-------------------------------------------");
                for (Account account : accountsList) {
                    User user = userService.getUserById(account.getUserId());
                    System.out.println(account.getAccountId() + "                      " + (user != null ? user.getUsername() : "Unknown User"));
                }

                //user input (required)
                int accountTo = consoleService.promptForInt("\nEnter the account ID to send TE bucks to: ");
                BigDecimal amount = consoleService.promptForBigDecimal("Enter the amount to send: ");


//            validating user inputs
                int accountFromId = accountService.getAccountById(currentUser.getUser().getId()).getAccountId();
                if (accountTo == accountFromId) {
                    System.out.println("\nYou cannot send money to yourself.");
                    return;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("\nYou cannot send a zero or negative amount.");
                    return;
                }


                Account senderAccount = accountService.getAccountById(currentUser.getUser().getId());
                BigDecimal currentBalance = senderAccount.getBalance();
                if (currentBalance.compareTo(amount) < 0) {
                    System.out.println("\nInsufficient funds.");
                    return;
                }

                // Transfer object
                Transfer transfer = new Transfer();
                transfer.setAccountFrom(senderAccount.getAccountId());
                transfer.setAccountTo(accountTo);
                transfer.setAmount(amount);
                transfer.setTransferTypeId(2); // send
                transfer.setTransferStatusId(2); // approved

                try {
                    Transfer sentTransfer = transferService.createTransfer(transfer); // Assuming createTransfer is used for sending as well
                    if (sentTransfer != null) {
                        System.out.println("\nTransfer successful. Transfer ID: " + sentTransfer.getTransferId());

                        // Update the sender's account balance locally and on the server
                        System.out.println("Sender's balance before update: " + senderAccount.getBalance());
                        senderAccount.setBalance(currentBalance.subtract(amount));
                        accountService.updateAccount(senderAccount);
                        System.out.println("Sender's balance after update: " + senderAccount.getBalance());
//                        accountService.updateBalance(senderAccount.getAccountId(), senderAccount.getBalance().subtract(amount));

                        // Get and update the receiver's account balance locally and on the server
                        Account receiverAccount = accountService.getAccountByUserId(accountTo);
                        System.out.println("Receiver's balance before update: " + receiverAccount.getBalance());

                        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
                        accountService.updateAccount(receiverAccount);
                        System.out.println("Receiver's balance after update: " + receiverAccount.getBalance());


                        System.out.println("\nTransfer successful. Your new balance is: " + senderAccount.getBalance());
                    } else {
                        System.out.println("\nTransfer failed.");
                    }
                } catch (Exception e) {
                    System.out.println("\nTransfer failed: " + e.getMessage());
                }
            }
        } catch (RestClientResponseException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (ResourceAccessException e) {
            System.out.println("Error: Unable to connect to the server.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }

    }


    private void requestBucks() {
        Account[] accounts = accountService.getAllAccounts();

        if (accounts != null) {
            System.out.println("-------------------------------------------");
            System.out.println("Accounts");
            System.out.println("ID                        Name");
            System.out.println("-------------------------------------------");
            for (Account account : accounts) {
                User user = userService.getUserById(account.getUserId());
                System.out.println(account.getAccountId() + "                      " + (user !=null ? user.getUsername() : "Unknown User"));
            }

            //required are user inputs
            int accountFrom = consoleService.promptForInt("\nEnter the account ID to request TE bucks from: ");
            BigDecimal amount = consoleService.promptForBigDecimal("Enter the amount to request: ");


            //validate user inputs

            if (accountFrom == currentUser.getUser().getId()) {
                System.out.println("\nYou cannot request money from yourself.");
                return;
            }


            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("\nYou cannot request a zero or negative amount.");
                return;
            }


            Transfer transfer = new Transfer();
            transfer.setAccountFrom(accountFrom);
            transfer.setAccountTo(accountService.getAccountByUserId(currentUser.getUser().getId()).getAccountId());
//            int userId = currentUser.getUser().getId();
//            Account toAccount = accountService.getAccountById(userId);
//            int accountId = toAccount.getAccountId();
            transfer.setAmount(amount);
            transfer.setTransferTypeId(1); // 1 is request
            transfer.setTransferStatusId(1); // 1 is pending

            try {
                Transfer requestedTransfer = transferService.createTransfer(transfer);
                if (requestedTransfer != null) {
                    System.out.println("\nRequest successful. Transfer ID: " + requestedTransfer.getTransferId());
                } else {
                    System.out.println("\nRequest failed.");
                }
            } catch (Exception e) {
                System.out.println("\nRequest failed: " + e.getMessage());
            }
        }
    }




}
