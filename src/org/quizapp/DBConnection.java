package org.quizapp;

import java.sql.*;

public class DBConnection {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/quizdb", // your DB name
                "root",                               // your MySQL username
                "13579"                               // your MySQL password
            );
            System.out.println("Database Connected!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
