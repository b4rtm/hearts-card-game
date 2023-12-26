package com.example.hearts.client;

import com.example.hearts.JoinRoomRequest;
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

    private Button newRoomButton;


    public void init() {
        this.serverCommunication = new ServerCommunicationHandler();
        serverCommunication.connectToServer("localhost", 9997);



    }

    @FXML
    synchronized void addPlayer(ActionEvent event) {
        serverCommunication.sendToServer("NAME", nameField.getText());

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

        newRoomButton = new Button("Stwórz nowy pokój");
        newRoomButton.setLayoutX(350.0);
        newRoomButton.setLayoutY(123.0);
        newRoomButton.setMnemonicParsing(false);
        newRoomButton.setOnAction(this::createNewRoom);
        nowyWidok.getChildren().add(newRoomButton);

        Thread readMessagesThread = new Thread(() -> serverCommunication.readMessagesFromServer(this));
        readMessagesThread.start();
    }


    synchronized void createNewRoom(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
        Pane root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        serverCommunication.sendToServer("CREATE_ROOM", new JoinRoomRequest(12,32));

        Platform.runLater(() -> {
            Scene scene = new Scene(root);
            Stage stage = (Stage) newRoomButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        });
    }
    public void onClose() {
        // Wywołane przy zamknięciu aplikacji, może zawierać zamykanie połączenia
        serverCommunication.closeConnection();
    }

    synchronized public void updateRoomsList(List<Room> rooms) {
        Platform.runLater(() -> {
            roomsList.getItems().clear();
            roomsList.getItems().addAll(rooms);
        });
    }
}