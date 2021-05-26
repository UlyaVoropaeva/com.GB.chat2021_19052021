package com.GB.chat2021.server;

import java.io.IOException;

public class ServerApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server(8189);
    }
}