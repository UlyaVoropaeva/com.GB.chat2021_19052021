package com.GB.chat2021.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Controller {
    @FXML
    TextField msgField, usernameField;
    @FXML
    TextArea textArea;
    @FXML
    HBox loginPanel, msgPanel;
    @FXML
    ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private List<String> list ;

    public void clickSendBtn(ActionEvent actionEvent) {
        String msg = msgField.getText()+"\n";
        try {
            out.writeUTF(msg);
            msgField.clear();
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }
    public void clickExit(){
        textArea.clear();
        disconnect();
        return;
    }
    public void login(ActionEvent actionEvent) throws Exception {
        if(socket == null || socket.isClosed()) {
            connect();
        }

        if(usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setUsername(String username) {
        this.username = username;
        if(username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);

        }
    }
    public void connect() throws Exception {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread dataThread = new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {

                        String msg = in.readUTF();
                        if(msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s")[1]);
                            textArea.appendText(HistoryClient.fileInputStreamHistory(msg.split("\\s")[1]));
                            HistoryClient.fileOutputStreamHistory (msg.split("\\s")[1]);

                            break;
                        }
                        if(msg.startsWith("/login_failed ")) {
                            String cause = msg.split("\\s", 2)[1];
                            textArea.appendText(cause + '\n');
                        }

                    }

                    //цикл общения
                    while (true) {
                        String msg = in.readUTF();

                        if(msg.startsWith("/")){
                            executeCommander(msg);
                            continue;
                        }
                        textArea.appendText(msg);
                        HistoryClient.listMessage(msg);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            dataThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server [localhost: 8189]");
        }
    }



    private void executeCommander(String cmd){
        if("/exit".equalsIgnoreCase(cmd)) {
            HistoryClient.close();
            clickExit();
        }

        if(cmd.startsWith("/clients_list ")) {
            String[] tokens = cmd.split("\\s");
            Platform.runLater(() -> {
                clientList.getItems().clear();
                for (int i = 1; i < tokens.length; i++) {
                    clientList.getItems().add(tokens[i]);
                }
            });
        }
    }
    public void disconnect() {
        setUsername(null);

        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}