package com.GB.chat2021.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private int port;
    private List<ClientHandler> clients;
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;


    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту" + port + "...");

            connect();

            while (true) {
                System.out.println("Ждем нового клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private static void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chat2021.db");
        stmt = connection.createStatement();
        System.out.println("Связь с базой данных установлена");

    }

    private static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void fillTable(String username) throws SQLException {

        connection.setAutoCommit(false);
        psInsert.setString(1, "" + username);

        psInsert.executeUpdate();
        connection.commit();
    }

    private static void prepareInsertStatement() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO students (name, time) VALUES ( ? , 0 );");
    }

    private static void prepareDeleteStatement() throws SQLException {
        psInsert = connection.prepareStatement("DELETE FROM students WHERE (name) = ( ? );");
    }

    private static void prepareUpdateStatement() throws SQLException {
        psInsert = connection.prepareStatement("UPDATE students SET time = ( time + 1 ) WHERE name = ( ? );");
    }


    private static boolean selectForm(ClientHandler clientHandler) throws SQLException {

        PreparedStatement ps = connection.prepareStatement("SELECT name FROM students WHERE name = ( ? );");
        ps.setString(1, clientHandler.getUsername());
        ResultSet rs = ps.executeQuery();
        System.out.println("rs.getString(\"name\") = " + rs.getString("name"));
        if (rs.getString("name").equals(clientHandler.getUsername())) {
            rs.close();
            return true;
        } else {
            rs.close();
            return false;
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws SQLException {


        clients.add(clientHandler);


        if (selectForm(clientHandler)) {
            prepareUpdateStatement();
            fillTable(clientHandler.getUsername());
            broadcastMessage("Клиент " + clientHandler.getUsername() + " подлючился повторно. \n");
        } else {
            prepareInsertStatement();
            fillTable(clientHandler.getUsername());
            broadcastMessage("Клиент " + clientHandler.getUsername() + " подлючился \n");
        }

        broadcastClientList();

    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {


        clients.remove(clientHandler);

        broadcastMessage("Клиент " + clientHandler.getUsername() + " отключился \n");
        broadcastClientList();

    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public boolean isUserOnline(String nickname) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void usernameMessage(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(nickname)) {
                client.sendMessage("Ваш никнейм: " + client.getUsername() + "\n");
                return;
            }
        }

    }

    public void privatUserMessage(ClientHandler clientHandler, String username, String massage) {

        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                client.sendMessage("От " + clientHandler.getUsername() + ":  " + massage + " \n");
                clientHandler.sendMessage("Пользователю " + username + ":  " + massage + " \n");
                return;
            }

        }
        clientHandler.sendMessage("Невозможно отправить сообщение. В списке контактов отсуствует : " + username + "\n");
        return;
    }

    private void broadcastClientList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler client : clients) {
            stringBuilder.append(client.getUsername()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(clientsList);
        }
    }
}
