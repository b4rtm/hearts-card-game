package com.example.hearts.client;

import com.example.hearts.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    @FXML
    private ListView<Room> roomsList;

    @FXML
    private Button newRoom;



    public void init() {
        this.serverCommunication = new ServerCommunicationHandler();
        // Inicjalizacja kontrolera, np. po wczytaniu widoku FXML
        System.out.println("XDDDDD");
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

        List<Room> rooms =  serverCommunication.receiveRooms();
        ObservableList<Room> observableRooms = FXCollections.observableArrayList(rooms);
        roomsList = new ListView<>();
        roomsList.setLayoutX(308.0);
        roomsList.setLayoutY(227.0);
        roomsList.setPrefHeight(200.0);
        roomsList.setPrefWidth(200.0);
        roomsList.getItems().addAll(rooms);
        nowyWidok.getChildren().add(roomsList);
    }

    @FXML
    void createNewRoom(ActionEvent event) {

    }
    public void onClose() {
        // Wywołane przy zamknięciu aplikacji, może zawierać zamykanie połączenia
        serverCommunication.closeConnection();
    }

}