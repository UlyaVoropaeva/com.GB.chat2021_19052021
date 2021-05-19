package com.GB.chat2021.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ClientHandler {

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String username;


    public ClientHandler(Socket clientSocket, Server server) throws IOException, ClassNotFoundException, SQLException {
        this.clientSocket = clientSocket;
        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());
        this.server = server;


        new Thread(() -> {
            try {
                //цикл авторизации
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        break;
                    }
                }

                //цикл общения
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }
    private void executeCommand(String cmd) throws IOException, SQLException {
        if (cmd.startsWith("/exit")) {
            disconnect();
            return;
        }
        if (cmd.startsWith("/login ")) {
            String usernameFromLogin = cmd.split("\\s")[1];

            if (server.isUserOnline(usernameFromLogin)) {
                sendMessage("/login_failed Current nickname has already been occupied");
                return;
            }
            username = usernameFromLogin;
            sendMessage("/login_ok " + username);
            server.subscribe(this);
            return;
        }

        if (cmd.startsWith("/who_am_i")) {
            server.usernameMessage(username);
            System.out.println("Клиент:" + username);
            return;
        }

        if (cmd.startsWith("/w")) {
            String[] tokens = cmd.split("\\s", 3);
            server.privatUserMessage(this, tokens[1], tokens[2]);
            return;
        }

    }


    private void disconnect() {
        server.unsubscribe(this);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }
    }

    public String getUsername() {
        return username;
    }

}