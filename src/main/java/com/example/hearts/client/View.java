package com.example.hearts.client;

import com.example.hearts.*;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class View {

    private Pane root;

    @FXML
    private TextField nameField;

    private ListView<Room> roomsList;
    private Pane roomsPane;
    private Scene roomScene;
    private Stage stage;


    private Button newRoomButton;

    private Controller controller;

    public View(Controller controller) {
        this.controller = controller;
    }

    public void setRoomView(ActionEvent event){
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("rooms-view.fxml"));
            try {
                roomsPane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (roomScene == null)
                roomScene = new Scene(roomsPane, 815, 475);
            if(this.stage == null)
                this.stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            this.stage.setScene(roomScene);

            newRoomButton = new Button("Stwórz nowy pokój");
            newRoomButton.setLayoutX(350.0);
            newRoomButton.setLayoutY(123.0);
            newRoomButton.setMnemonicParsing(false);
            newRoomButton.setOnAction(event1 -> controller.createNewRoom());
            roomsPane.getChildren().add(newRoomButton);
        });
    }

    public void setRoomScene(){
        this.stage.setScene(roomScene);
    }

    public void loadGameView() {
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(root);
            Stage stage = (Stage) newRoomButton.getScene().getWindow();
            stage.setScene(scene);
            initChat();

            stage.show();
        });
    }

    private void initChat() {
        Platform.runLater(() -> {
            Button sendButton = (Button) root.lookup("#chatButton");
            TextField messageTextField = (TextField) root.lookup("#chatText");
            sendButton.setOnAction(event -> {
                controller.sendChatMessage(messageTextField.getText());
                messageTextField.setText("");
            });
        });
    }


    public void displayGameView(Room room) {
        Platform.runLater(() -> {

            for (int i=1;i<=3;i++){
                ImageView imageView = (ImageView) root.lookup("#p" + i);
                imageView.setImage(new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\reverse_card.jpg"));
            }

            if(room.getPlayers().size() < 4) {
                for (int cardCounter = 1; cardCounter <= 13; cardCounter++) {
                    ImageView card = (ImageView) root.lookup("#card" + cardCounter);
                    Image cardImage = new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\reverse_card.jpg");
                    card.setImage(cardImage);
                }
            }

            Pane innerPane = (Pane) root.lookup("#pane1");
            int counter=1;
            for (Player player : room.getPlayers()){
                Label nameLabel = (Label) innerPane.lookup("#name" + counter);
                nameLabel.setText(player.getName());
                counter++;
            }
        });
    }

    public void updateRoomListView(List<Room> rooms){
        Platform.runLater(() -> {
            roomsList = (ListView<Room>) roomsPane.lookup("#roomsList");
            roomsList.getItems().clear();
            roomsList.getItems().addAll(rooms);
            roomsList.setOnMouseClicked(event -> {
                Room selectedRoom = roomsList.getSelectionModel().getSelectedItem();
                if (selectedRoom != null && (selectedRoom.getPlayers() == null || selectedRoom.getPlayers().size() < 4)) {
                    controller.joinRoom(selectedRoom);
                    loadGameView();
                }
            });
        });
    }

    public void displayCardOnTable(Card card, String elementId) {
        Platform.runLater(() -> {
            if(card != null) {
                ImageView cardImg = (ImageView) root.lookup(elementId);
                Image cardImage = new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\" + card.getImagePath());
                cardImg.setImage(cardImage);
            }
            else {
                ImageView cardImg = (ImageView) root.lookup(elementId);
                cardImg.setImage(null);
            }
        });
    }

    public void setCardInDeck(int cardCounter, Card card, Player player, boolean setOnClick){
        Platform.runLater(() -> {
            ImageView cardImg = (ImageView) root.lookup("#card" + cardCounter);
            int finalCardCounter1 = cardCounter;
            Image cardImage = new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\" + card.getImagePath());
            cardImg.setImage(cardImage);

            int finalCardCounter = cardCounter;
            if(setOnClick)
                cardImg.setOnMouseClicked(event -> {
                    controller.makeMove(card, player);
                });
            else{
                cardImg.setOnMouseClicked(null);
            }
        });
    }

    public void removeCardFromDeck(int number){
        Platform.runLater(() -> {
            ImageView card = (ImageView) root.lookup("#card" + number);
            card.setImage(null);
            card.setOnMouseClicked(null);
        });
    }

    public void displayNameOnTable(PlayerInfo playerInfo, int number){
        Platform.runLater(() -> {
            Label nameLabel = (Label) root.lookup("#l" + number);
            nameLabel.setText(playerInfo.getName());
        });
    }

    public void displayPoints(List<Integer> pointsList){
        Platform.runLater(() -> {
            Pane innerPane = (Pane) root.lookup("#pane1");
            int counter=1;
            for (Integer points : pointsList){
                Label pointsLabel = (Label) innerPane.lookup("#points" + counter);
                pointsLabel.setText(String.valueOf(points));
                counter++;
            }
        });
    }

    public void addMessageToListView(List<String> chatHistory){
        Platform.runLater(() -> {
            ListView<String> chatListView = (ListView<String>) root.lookup("#chatList");
            chatListView.getItems().clear();
            chatListView.getItems().addAll(chatHistory);

        });
    }

    public void displayEndGamePanel() {
        Platform.runLater(() -> {
            Pane endPane = (Pane) root.lookup("#endPane");
            endPane.setVisible(true);
            Button endButton = (Button) endPane.lookup("#endButton");
            endButton.setOnAction(actionEvent -> {
                setRoomScene();
            });

        });
    }
}
