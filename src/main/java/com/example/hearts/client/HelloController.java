package com.example.hearts.client;

import com.example.hearts.Player;
import com.example.hearts.Room;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HelloController {

    private ServerCommunicationHandler serverCommunication;

    private Player player;

    @FXML
    private Button joinButton;

    @FXML
    private Pane pane;

    private Pane root;

    @FXML
    private TextField nameField;

    private ListView<Room> roomsList;

    private Button newRoomButton;


    public void init() {
        this.serverCommunication = new ServerCommunicationHandler();
        serverCommunication.connectToServer("localhost", 9997);

    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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


    public void createNewRoom(ActionEvent event) {
        serverCommunication.sendToServer("CREATE_ROOM", 0);
//        displayGameView();
    }

    private void displayGameView(Room room) {
        Platform.runLater(() -> {
            Pane innerPane = (Pane) root.lookup("#pane1");
            int counter=1;
            for (Player player : room.getPlayers()){
                Label pointsLabel = (Label) innerPane.lookup("#points" + counter);
                pointsLabel.setText(String.valueOf(player.getPoints()));
                Label nameLabel = (Label) innerPane.lookup("#name" + counter);
                nameLabel.setText(player.getName());
                counter++;
            }
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

            for (Room room : rooms) {
                if (room.getPlayers().contains(player)) {
                    displayGameView(room);
                    break;
                }
            }

            roomsList.setOnMouseClicked(event -> {
                Room selectedRoom = roomsList.getSelectionModel().getSelectedItem();
                if (selectedRoom != null && (selectedRoom.getPlayers() == null || selectedRoom.getPlayers().size() < 4)) {
                    serverCommunication.sendToServer("JOIN_ROOM", selectedRoom.getRoomId());
                    loadGameView();
                }
            });
        });
    }

    private void loadGameView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
        Stage stage = (Stage) newRoomButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}