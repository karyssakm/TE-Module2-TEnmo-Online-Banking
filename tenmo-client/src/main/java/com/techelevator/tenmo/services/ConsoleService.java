package com.techelevator.tenmo.services;



import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private Scanner scanner = new Scanner(System.in);
    public ConsoleService() {
        this.scanner = new Scanner(System.in);
    }


    public int promptForRecipientAccount() {
        System.out.print("Enter recipient's account number: ");
        return scanner.nextInt();
    }
    public BigDecimal promptForAmount() {
        System.out.print("Enter amount to transfer: $");
        return scanner.nextBigDecimal();
    }
    public int promptForTransferId() {
        System.out.print("Enter transfer ID: ");
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a valid transfer ID: ");
            }
        }
    }

    public void printTransferHistory(Transfer[] transfers) {
        if (transfers != null && transfers.length > 0) {
            System.out.println("------- Transfer History -------");
            for (Transfer transfer : transfers) {
                System.out.println("Amount: $" + transfer.getAmount());
                System.out.println("From: " + transfer.getAccountFrom());
                System.out.println("To: " + transfer.getAccountTo());
                System.out.println("--------------------------------");
            }
        } else {
            System.out.println("No transfers found.");
        }
    }


    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");

        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

}
