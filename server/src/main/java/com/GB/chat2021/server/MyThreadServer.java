package com.GB.chat2021.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class MyThreadServer extends Thread{
    private Socket clientSocket;

    public MyThreadServer(Socket clientSocket){
        this.clientSocket = clientSocket;

    }
    public void run(){
        Scanner sc = new Scanner(System.in);

        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream( clientSocket.getOutputStream());

            String msg;

            while (true){
                msg = sc.nextLine();
               out.writeUTF(msg+ " ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}