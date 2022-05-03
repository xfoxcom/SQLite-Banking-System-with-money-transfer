package banking;


import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + args[1]);

        try (Connection connection = dataSource.getConnection()) {

            try  (Statement statement = connection.createStatement()) {

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (id integer, number text, pin text, balance integer)");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Scanner scr = new Scanner(System.in);
        int num=4;

        while (num!=0) {
            System.out.println("1. Create an account\n2. Log into account\n0. Exit");
            num = scr.nextInt();
            System.out.println();
            switch (num) {
                case 1: createAcc.create(args[1]); break;
                case 2: createAcc.logIn();
               if (createAcc.isExit) { num = 0;
                   System.out.println("\nBye!"); }
                break;
                case 0:
                    System.out.println("Bye!"); break;
            }
        }
    }
}