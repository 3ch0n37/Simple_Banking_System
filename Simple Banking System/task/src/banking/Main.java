package banking;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    static Bank bank;
    static State state = State.START;
    static Action action = Action.START;
    static Scanner scanner;
    static Account currentAccount;

    public static boolean menu() {
        if (state == State.START) {
            System.out.print("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit\n");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    action = Action.CREATE_ACCOUNT;
                    break;
                case 2:
                    action = Action.LOG_IN;
                    break;
                case 0:
                    action = Action.STOP;
                    break;
                default:
                    return false;
            }
        } else if (state == State.LOGGED_IN) {
            System.out.print("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit\n");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    action = Action.CHECK_BALANCE;
                    break;
                case 2:
                    action = Action.ADD_INCOME;
                    break;
                case 3:
                    action = Action.DO_TRANSFER;
                    break;
                case 4:
                    action = Action.CLOSE_ACCOUNT;
                    break;
                case 5:
                    action = Action.LOG_OUT;
                    break;
                case 0:
                    action = Action.STOP;
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static void createAccount() {
        String number = Account.generateNumber();
        String pin = Account.generatePin();
        Account n = new Account(number, pin, 0);
        bank.addAccount(n);
        System.out.printf("Your card has been created%n" +
                "Your card number:%n" +
                "%s%nYour card PIN:%n%s%n%n", number, pin);
        action = Action.START;
    }

    public static void login() {
        System.out.println("\nEnter your card number:");
        String number = scanner.next();
        System.out.println("Enter your pin:");
        String pin = scanner.next();
        Account a = bank.findAccount(number);
        if(a != null && a.checkPin(pin)) {
            currentAccount = a;
            state = State.LOGGED_IN;
            action = Action.MENU;
            System.out.println("\nYou have successfully logged in!\n");
        } else {
            System.out.println("\nWrong card number or PIN!\n");
            action = Action.MENU;
        }
    }

    public static void transfer() {
        System.out.println("\nTransfer:\nEnter card number:");
        String sendTo = scanner.next();
        if(sendTo.length() < 16 || sendTo.charAt(15) != Account.luhns(sendTo) + '0') {
            System.out.println("Probably you made a mistake in card number. Please try again!\n");
            return;
        }
        Account reciever = bank.findAccount(sendTo);
        if(reciever == null) {
            System.out.println("Such a card does not exist.\n");
            return;
        }
        System.out.println("Enter how much money you want to transfer:");
        double ammount = scanner.nextDouble();
        Error_codes err = bank.doTransfer(currentAccount, reciever, ammount);
        switch (err) {
            case SAME_ACCOUNT:
                System.out.println("You can't transfer money to the same account!\n");
                break;
            case NOT_ENOUGH:
                System.out.println("Not enough money!\n");
                break;
            case SUCCESS:
                System.out.println("Success!\n");
                break;
        }
    }

    public static void income() {
        System.out.println("Enter income:");
        double in = scanner.nextDouble();
        if(bank.addIncome(currentAccount, in)) {
            System.out.println("Income was added!\n");
            currentAccount = bank.findAccount(currentAccount.getNumber());
        } else {
            System.out.println("There was an error while adding income...\n");
        }
    }

    public static void main(String[] args) {
        if(args.length != 2 || !args[0].equals("-fileName")) {
            System.out.println("-fileName is a required argument");
        }
        String dbFile = args[1];
        SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl("jdbc:sqlite:" + dbFile);
        try (Connection con = source.getConnection()){
            if(!con.isValid(5)) {
                System.out.println("Connection not valid!");
                return;
            }
            bank = new Bank(con);
            scanner = new Scanner(System.in);
            boolean m;

            while (action != Action.STOP) {
                switch (action) {
                    case START:
                    case MENU:
                        do {
                            m = menu();
                        } while (!m);
                        break;
                    case CREATE_ACCOUNT:
                        createAccount();
                        break;
                    case LOG_IN:
                        login();
                        break;
                    case CHECK_BALANCE:
                        System.out.printf("%nBalance %.2f%n%n", currentAccount.getBalance());
                        action = Action.MENU;
                        break;
                    case DO_TRANSFER:
                        transfer();
                        action = Action.MENU;
                        break;
                    case ADD_INCOME:
                        income();
                        action = Action.MENU;
                        break;
                    case CLOSE_ACCOUNT:
                        if(bank.closeAccount(currentAccount)) {
                            currentAccount = null;
                            state = State.START;
                            System.out.println("The account has been closed!");
                        } else {
                            System.out.println("Couldn't close your account");
                        }
                        action = Action.MENU;
                        break;
                    case NOT_IMPLEMENTED:
                        System.out.println("Selection not implemented!");
                        action = Action.MENU;
                        break;
                    case LOG_OUT:
                        currentAccount = null;
                        state = State.START;
                        action = Action.MENU;
                        System.out.println("\nYou have successfully logged out!\n");
                        break;
                }
            }
            System.out.println("\nBye!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}