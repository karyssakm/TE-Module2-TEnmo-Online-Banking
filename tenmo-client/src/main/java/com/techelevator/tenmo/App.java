package com.techelevator.tenmo;


import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        transferService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());

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
            System.out.println("Your current balance is: $ " + balance);
        } else {
            System.out.println("User not authenticated.");
        }
    }


	private void viewTransferHistory() {

        if (currentUser != null) {
            //need to only call current user
            Transfer[] transfers = transferService.getAllPastTransfers();

            if (transfers != null && transfers.length > 0) {
                System.out.println("-----------------------------------------------");
                System.out.println("*  *  *   *  Past Transfer History  *   *  *  *");
                System.out.println("-----------------------------------------------");
                System.out.println("ID            From/To             Amount");
                System.out.println("-----------------------------------------------");


                for (Transfer transfer: transfers) {

                    String transferUserId;
                    try {
                        if (transfer.getAccountFrom() == (accountService.getAccountByUserId(currentUser.getUser().getId()).getAccountId()))  {
                            User receiverUser = userService.getUserById(accountService.getAccountById(transfer.getAccountTo()).getUserId());
                            transferUserId = receiverUser != null ? "To: " + receiverUser.getUsername() : "To: Unknown";
                        } else {
                            User senderUser = userService.getUserById(accountService.getAccountById(transfer.getAccountFrom()).getUserId());
                            transferUserId = senderUser != null ? "From: " + senderUser.getUsername() : "From: Unknown";
                        }

                        System.out.printf("%-12d %-20s $%.2f\n",
                                transfer.getTransferId(),
                                transferUserId,
                                transfer.getAmount()
                        );

                    } catch (Exception e) {
                        transferUserId = "Unknown";
                    }

                }
                System.out.println("-----------------------------------------------");
                int transferId = consoleService.promptForInt("\nPlease enter transfer ID to view details: ");

                if (transferId != 0) {
                    Transfer transfer = transferService.getTransferByTransferId(transferId);
                    viewTransferDetails(transfer);
                }
            } else {
                System.out.println("No transfers found.");
            }
        } else {
            System.out.println("User not authenticated.");
        }
            }



    private void viewTransferDetails(Transfer transfer) {
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("ID: " + transfer.getTransferId());
        System.out.println("From: " + userService.getUserById(accountService.getAccountById(transfer.getAccountFrom()).getUserId()).getUsername());
        System.out.println("To: " + userService.getUserById(accountService.getAccountById(transfer.getAccountTo()).getUserId()).getUsername());
        System.out.println("Type: " + (transfer.getTransferTypeId() == 1 ? "Request" : "Send"));
        System.out.println("Status: " + (transfer.getTransferStatusId() == 1 ? "Pending" : transfer.getTransferStatusId() == 2 ? "Approved" : "Rejected"));
        System.out.println("Amount: $" + transfer.getAmount());
        System.out.println("-----------------------------------------------------------------\n");
    }




	private void viewPendingRequests() {
        if (currentUser != null) {
            Transfer[] pendingRequests = transferService.getAllPendingRequests();

            if (pendingRequests != null && pendingRequests.length > 0) {
                System.out.println("-----------------------------------------------------------------");
                System.out.println("*     *   *  *   *   *   Pending Requests   *   *  *   *   *    *");
                System.out.println("-----------------------------------------------------------------");
                System.out.println("ID            From/To                 Amount");
                System.out.println("-----------------------------------------------------------------");

                for (Transfer transfer : pendingRequests) {

                    String transferUserId;
                    try {
                        if (transfer.getAccountFrom() == (accountService.getAccountByUserId(currentUser.getUser().getId()).getAccountId())) {
                            User receiverUser = userService.getUserById(accountService.getAccountById(transfer.getAccountTo()).getUserId());
                            transferUserId = receiverUser != null ? "To: " + receiverUser.getUsername() : "To: Unknown";


                            System.out.printf("%-12d %-20s $%.2f\n",
                                    transfer.getTransferId(),
                                    transferUserId,
                                    transfer.getAmount()
                            );
                        }

                    } catch (Exception e) {
                        transferUserId = "Unknown";
                    }
                }

//                int transferId = consoleService.promptForInt("\nPlease enter transfer ID to approve/reject (0 to cancel): ");
//                if (transferId != 0) {
//                    Transfer transfer = transferService.getTransferByTransferId();
//
//                }



                }
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
                    System.out.println(account.getAccountId() + "               " + (user != null ? user.getUsername() : "Unknown User"));
                }


                //user input (required)
                int accountTo = consoleService.promptForInt("\nEnter the account ID to send TE bucks to: ");
                BigDecimal amount = consoleService.promptForBigDecimal("Enter the amount to send: ");


                //current senders info

                Account senderAccount = accountService.getAccountByUserId(currentUser.getUser().getId());
                BigDecimal currentSenderBalance = senderAccount.getBalance();


                //receivers info
                Account receiverAccount = accountService.getAccountById(accountTo);
                BigDecimal currentReceiverBalance = receiverAccount.getBalance();


                //validate user inputs
                if (accountTo == senderAccount.getAccountId()) {
                    System.out.println("\nYou cannot send money to yourself.");
                    return;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("\nYou cannot send a zero or negative amount.");
                    return;
                }

                if (currentSenderBalance.compareTo(amount) < 0) {
                    System.out.println("\nInsufficient funds.");
                    return;
                }
//
//                System.out.println("Sender Account: " + senderAccount);
//                System.out.println("Receiver Account: " + receiverAccount);
                System.out.println("\nSender Balance: " + currentSenderBalance);
                System.out.println("Receiver Balance: " + currentReceiverBalance);


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
                        BigDecimal newSenderBalance = currentSenderBalance.subtract(amount);
                        senderAccount.setBalance(newSenderBalance);
                        accountService.updateAccount(senderAccount);


                        // Get and update the receiver's account balance locally and on the server
//
                        BigDecimal newReceiverBalance = currentReceiverBalance.add(amount);
                        receiverAccount.setBalance(newReceiverBalance);
//                        accountService.addBalance(receiverAccount.getAccountId(), amount);
                        accountService.updateAccount(receiverAccount);

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
