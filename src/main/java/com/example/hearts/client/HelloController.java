package com.example.hearts.client;

import com.example.hearts.Room;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HelloController {

    private ServerCommunicationHandler serverCommunication;

    @FXML
    private Button joinButton;

    @FXML
    private Pane pane;

    @FXML
    private TextField nameField;

    private ListView<Room> roomsList;

    @FXML
    private Button newRoom;


    public void init() {
        this.serverCommunication = new ServerCommunicationHandler();
        serverCommunication.connectToServer("localhost", 9997);
    }

    @FXML
    void addPlayer(ActionEvent event) {
        serverCommunication.sendToServer(nameField.getText());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("rooms-view.fxml"));
        Pane nowyWidok;
        try {
            nowyWidok = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scene nowaScena = new Scene(nowyWidok, 815, 475);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(nowaScena);

        roomsList = new ListView<>();
        roomsList.setLayoutX(308.0);
        roomsList.setLayoutY(227.0);
        roomsList.setPrefHeight(200.0);
        roomsList.setPrefWidth(200.0);
        nowyWidok.getChildren().add(roomsList);

        Thread readMessagesThread = new Thread(() -> serverCommunication.readMessagesFromServer(this));
        readMessagesThread.start();
    }

    @FXML
    void createNewRoom(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
        Pane root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        Scene scene = new Scene(root);
        Stage stage = (Stage) newRoom.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    public void onClose() {
        // Wywołane przy zamknięciu aplikacji, może zawierać zamykanie połączenia
        serverCommunication.closeConnection();
    }

    public void updateRoomsList(List<Room> rooms) {
        Platform.runLater(() -> {
            roomsList.getItems().clear();
            roomsList.getItems().addAll(rooms);
        });
    }
}