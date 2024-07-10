package com.techelevator.tenmo;


import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

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
                    System.out.println(transfer.getTransferId(),transfer.getAccountFrom(),transfer.getAccountTo(), transfer.getAmount());
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
        if (currentUser != null) {

            TransferDto transferDto = new TransferDto();
            transferDto.setAccountFrom(currentUser.getUser().getId());
            transferDto.setAccountTo(consoleService.promptForRecipientAccount());
            transferDto.setAmount(consoleService.promptForAmount());

            Transfer responseTransfer = transferService.sendBucks(transferDto);
            if (responseTransfer != null) {
                System.out.println("Transfer successful:");
                System.out.println(responseTransfer);
            } else {
                System.out.println("Failed to send bucks.");
            }
        } else {
            System.out.println("User not authenticated.");
        }
	}


	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}

}
