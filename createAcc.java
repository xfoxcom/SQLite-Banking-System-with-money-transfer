package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class createAcc {
    public String cardNumber;
    private String PIN;
    private long balance;
    public static final String URL = "jdbc:sqlite:";
    public static boolean isExit = false;


    public createAcc(String cardNumber, String PIN, long balance) {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.balance = balance;
    }

    public static void addToSQl(createAcc account, String filename) {
        int i = 1;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(URL + filename);

        try (Connection connection = dataSource.getConnection()) {

            try (Statement statement = connection.createStatement()) {

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (id integer, number text, pin text, balance integer)");

                ResultSet resultSet = statement.executeQuery("SELECT * from card");
                while (resultSet.next()) {
                    i++;
                }

                statement.executeUpdate("INSERT INTO card VALUES " +

                        "(" + i + ", " + account.cardNumber + "," + account.PIN + "," + account.balance + ")");

                statement.close();
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int lastInt(StringBuilder s) {
        int[] array = new int[15];
        int sum = 0;
        String query = s.reverse().toString();
        String[] toInt = query.split("");
        for (int i = 0; i < toInt.length; i++) {
            if ((i + 1) % 2 != 0) {
                array[i] = Integer.parseInt(toInt[i]) * 2;
                if (array[i] > 9) {
                    array[i] = array[i] - 9;
                }
            } else array[i] = Integer.parseInt(toInt[i]);
            sum += array[i];
        }
        s.reverse();
        if (sum % 10 == 0) return 0;
        else
            return 10 - sum % 10;
    }

    public static void create(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(400000);
        for (int i = 0; i < 9; i++) {
            stringBuilder.append((int) (Math.random() * 9));
        }
        stringBuilder.append(lastInt(stringBuilder));

        StringBuilder stringBuilder1 = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            stringBuilder1.append((int) (Math.random() * 9));
        }
        createAcc account = new createAcc(stringBuilder.toString(), stringBuilder1.toString(), 0);
        addToSQl(account, filename);

        System.out.println("Your card has been created\nYour card number:\n" + stringBuilder + "\nYour card PIN:\n" + stringBuilder1 + "\n");
    }

    public static void logIn() {
        Scanner scr = new Scanner(System.in);
        System.out.println("Enter your card number: ");
        String number = scr.nextLine();
        System.out.println("Enter your PIN: ");
        String pin = scr.nextLine();
        System.out.println();
        String query = "SELECT * FROM card WHERE number = " + number + " AND " + "pin = " + pin;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(URL + "card.s3db");
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {

                int i = 0;
                ResultSet rs = statement.executeQuery("SELECT number, pin FROM card");
                while (rs.next()) {
                   if (number.equals(rs.getString("number")) & pin.equals(rs.getString("pin"))) {
                       i++;
                   }
                }
                if (i == 0) {
                    System.out.println("Wrong card number or PIN!\n");
                    throw new Exception();
                }

                ResultSet resultSet = statement.executeQuery(query);

                    createAcc acc = new createAcc(resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4));
                    System.out.println("You have successfully logged in!\n");
                    showInfo(acc, statement);

                    resultSet.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void showInfo(createAcc account, Statement statement) {
        Scanner scr = new Scanner(System.in);
        int num = 6;
        while (num != 0) {
            System.out.println("1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Log out\n0. Exit");
            num = scr.nextInt();
            switch (num) {
                case 1:
                    try {

                        ResultSet resultSet = statement.executeQuery("SELECT balance FROM card WHERE number = " + account.cardNumber);

                        System.out.println("\nBalance: " + resultSet.getInt("balance"));


                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    System.out.println();
                    break;
                case 2:
                    addIncome(account, statement);
                    break;
                case 3:
                    doTransfer(account, statement);
                    break;
                case 4:
                    closeAccount(account, statement);
                    num = 0;
                    break;
                case 5:
                    System.out.println("\nYou have successfully logged out!\n");
                    num = 0;
                    break;
                case 0:
                    isExit = true;
                    break;
            }
        }
    }

    public static void addIncome(createAcc account, Statement statement) {
        Scanner scr = new Scanner(System.in);
        System.out.println("\nEnter income: ");
        int income = scr.nextInt();
        account.balance = account.balance + income;
        System.out.println("Income was added!\n");

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(URL + "card.s3db");

        try {
            statement.executeUpdate("UPDATE card SET balance = " + account.balance + " WHERE number = " + account.cardNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeAccount(createAcc account, Statement statement) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(URL + "card.s3db");

        try {

            statement.executeUpdate("DELETE FROM card WHERE number = " + account.cardNumber);
            System.out.println("The account has been closed!\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void doTransfer(createAcc account, Statement statement) {
        Scanner scr = new Scanner(System.in);
        System.out.println("\nTransfer\nEnter card number: ");
        String number = scr.nextLine();
        int u = 0;

        try {

            if (number.equals(account.cardNumber)) {
                System.out.println("You can't transfer money to the same account!\n");
                throw new Exception();
            }

            if (!isLuhn(number)) {
                System.out.println("Probably you made a mistake in the card number. Please try again!\n");
                throw new Exception();
            }

            ResultSet resultSet = statement.executeQuery("SELECT number, balance FROM card WHERE number = " + number);

            while (resultSet.next()) {
                if (number.equals(resultSet.getString(1))) {
                    u++;
                }
            }

            if (u == 0) {
                System.out.println("Such a card does not exist.\n");
                throw new Exception();
            }

            System.out.println("Enter how much money you want to transfer:");
            int amount = scr.nextInt();

            if (amount > account.balance) {
                    System.out.println("Not enough money!");
                    throw new Exception();
                }

                statement.executeUpdate("UPDATE card SET balance = " + (account.balance - amount) + " WHERE number = " + account.cardNumber);

                statement.executeUpdate("UPDATE card SET balance = balance +" + amount + " WHERE number = " + number);

                System.out.println("Success!\n");

                resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public static boolean isLuhn (String value) {
        int sum = Character.getNumericValue(value.charAt(value.length() - 1));
        int parity = value.length() % 2;
        for (int i = value.length() - 2; i >= 0; i--) {
            int summand = Character.getNumericValue(value.charAt(i));
            if (i % 2 == parity) {
                int product = summand * 2;
                summand = (product > 9) ? (product - 9) : product;
            }
            sum += summand;
        }
        return (sum % 10) == 0;
    }
}
