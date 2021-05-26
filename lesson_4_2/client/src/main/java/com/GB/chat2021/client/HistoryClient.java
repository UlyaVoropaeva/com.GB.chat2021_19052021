package com.GB.chat2021.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HistoryClient {

    private static PrintWriter out;

    private static String fileName(String username) {
        return "history/local_" + username + ".txt";
    }

    public static void listMessage(String message) {
        out.println(message);
    }

    public static void close() {

        if (out != null) {
            out.close();
        }

    }

    public static String fileInputStreamHistory(String username) {

        if (!Files.exists(Paths.get(fileName(username)))) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try {
            List<String> list = Files.readAllLines(Paths.get(fileName(username)));
            int finishPosition = list.size();
            int startPosition = 0;

            if (finishPosition > 100) {
                startPosition = finishPosition - 100;
            }
            for (int i = startPosition; i < finishPosition; i++) {
                result.append(list.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static void fileOutputStreamHistory(String username) {

        try {
            out = new PrintWriter(new FileOutputStream(fileName(username), true), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
