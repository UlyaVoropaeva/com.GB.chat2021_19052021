package com.GB.chat2021.server;

import java.sql.*;

public class SQLServer {

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    private static Server server;

   public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:msqlite:main.db");
            stmt = connection.createStatement();
            System.out.println("Связь с базой данных установлена");
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void registration (ClientHandler clientHandler){
        try {
            if (selectNameClientHandler(clientHandler)) {
                prepareUpdateStatement();
                fillTable(clientHandler.getUsername());
                server.broadcastMessage("Клиент " + clientHandler.getUsername() + " подлючился повторно. \n");
            } else {   prepareInsertStatement();
                fillTable(clientHandler.getUsername());
                server.broadcastMessage("Клиент " + clientHandler.getUsername() + " подлючился \n");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static boolean selectNameClientHandler (ClientHandler clientHandler) throws SQLException {


        PreparedStatement ps = connection.prepareStatement("SELECT username FROM client WHERE username = ( ? );");
        ps.setString(1, clientHandler.getUsername());
        ResultSet rs = ps.executeQuery();
        System.out.println("rs.getString(\"username\") = " + rs.getString("username"));
        if (rs.getString("username").equals(clientHandler.getUsername())) {
            rs.close();
            return true;
        } else {
            rs.close();
            return false;
        }
    }
    private static void fillTable(String username) throws SQLException {

        connection.setAutoCommit(false);
        psInsert.setString(1, "" + username);

        psInsert.executeUpdate();
        connection.commit();
    }
    private static void prepareInsertStatement() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO client (username, number) VALUES ( ? , 0 );");
    }

    private static void prepareDeleteStatement() throws SQLException {
        psInsert = connection.prepareStatement("DELETE FROM client WHERE (username) = ( ? );");
    }

    private static void prepareUpdateStatement() throws SQLException {
        psInsert = connection.prepareStatement("UPDATE client SET number = ( number + 1 ) WHERE username = ( ? );");
    }

    public static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.commit();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
