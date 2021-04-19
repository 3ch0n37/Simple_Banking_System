package banking;

import java.util.Random;

public class Account {
    private String number;
    private String pin;
    private double balance;

    Account(String number, String pin, Integer balance) {
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    public static int luhns(String number) {
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int num = (number.charAt(i) - '0');
            if (i % 2 == 0) {
                num *= 2;
                if(num > 9) {
                    num -= 9;
                }
            }
            sum += num;
        }
        if(sum % 10 == 0) {
            return 0;
        }
        return 10 - (sum % 10);
    }

    public static String generateNumber() {
        Random random = new Random();
        StringBuilder ret = new StringBuilder("400000");
        for (int i = 0; i < 9; i++) {
            ret.append(random.nextInt(10));
        }
        ret.append(luhns(ret.toString()));
        return ret.toString();
    }

    public static String generatePin() {
        Random random = new Random();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            ret.append(random.nextInt(10));
        }
        return ret.toString();
    }

    public String getNumber() {
        return this.number;
    }

    public String getPin() {
        return pin;
    }

    public boolean checkPin(String pin) {
        return this.pin.equals(pin);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
