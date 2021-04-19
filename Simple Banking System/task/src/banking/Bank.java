package banking;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class Bank {
    private Connection db;

    Bank(Connection db) {
        this.db = db;
        try(Statement statement = db.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "number TEXT," +
                    "pin TEXT," +
                    "balance INTEGER DEFAULT 0" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addAccount(Account account) {
        String sql = "INSERT INTO card (number, pin) VALUES (?,?)";
        try(PreparedStatement statement = this.db.prepareStatement(sql)) {
            statement.setString(1, account.getNumber());
            statement.setString(2, account.getPin());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Account findAccount(String number) {
        String sql = "SELECT number, pin, balance FROM card WHERE number = ?";
        Set<Account> accounts = new HashSet<>();
        try(PreparedStatement prstm = this.db.prepareStatement(sql)) {
            prstm.setString(1, number);
            ResultSet rs = prstm.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(accounts.size() == 1) {
            return (Account) accounts.toArray()[0];
        }
        return null;
    }

    public Error_codes doTransfer(Account from, Account to, double ammount) {
        String updateSql = "UPDATE card set balance = ? where number = ?";
        try (PreparedStatement prstm = this.db.prepareStatement(updateSql)) {
            if(from.getBalance() < ammount) {
                return Error_codes.NOT_ENOUGH;
            } else if (from.getNumber().equals(to.getNumber())) {
                return Error_codes.SAME_ACCOUNT;
            }
            prstm.setDouble(1, from.getBalance() - ammount);
            prstm.setString(2, from.getNumber());
            prstm.executeUpdate();
            prstm.setDouble(1, to.getBalance() + ammount);
            prstm.setString(2, to.getNumber());
            prstm.executeUpdate();
            return Error_codes.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return Error_codes.FAILURE;
        }
    }

    public boolean addIncome(Account to, double ammount) {
        String updateSql = "UPDATE card SET balance = ? WHERE number = ?";
        try(PreparedStatement prstm = this.db.prepareStatement(updateSql)) {
            prstm.setDouble(1, to.getBalance() + ammount);
            prstm.setString(2, to.getNumber());
            prstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeAccount(Account toClose) {
        String deleteSql = "DELETE FROM card WHERE number = ?";
        try(PreparedStatement prstm = this.db.prepareStatement(deleteSql)) {
            prstm.setString(1, toClose.getNumber());
            prstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
