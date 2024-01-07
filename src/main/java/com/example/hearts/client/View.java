package com.example.hearts.client;

import com.example.hearts.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

/**
 * The View class handles the graphical user interface, updating and displaying  UI elements.
 */
public class View {

    public static final String ABSOLUTE_CARD_IMAGE_PATH = "file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\";
    private Pane root;
    private ListView<Room> roomsList;
    private Pane roomsPane;
    private Scene roomScene;
    private Stage stage;
    private Button newRoomButton;
    private Controller controller;

    /**
     * Constructs a new View with the specified controller.
     *
     * @param controller The controller associated with this view.
     */
    public View(Controller controller) {
        this.controller = controller;
    }

    /**
     * Loads the room view when called in response to a button click event.
     *
     * @param event The action event triggering the method call.
     */
    public void loadRoomView(ActionEvent event) {
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
            if (this.stage == null)
                this.stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            this.stage.setScene(roomScene);

            this.stage.setOnCloseRequest(event1 -> {
                controller.onClose();
                Platform.exit();
            });

            newRoomButton = new Button("Stwórz nowy pokój");
            newRoomButton.setLayoutX(350.0);
            newRoomButton.setLayoutY(123.0);
            newRoomButton.setMnemonicParsing(false);
            newRoomButton.setOnAction(event1 -> controller.createNewRoom());
            roomsPane.getChildren().add(newRoomButton);
        });
    }

    /**
     * Sets the current scene to the room scene.
     */
    public void setRoomScene() {
        this.stage.setScene(roomScene);
    }

    /**
     * Loads the game view, initializing the UI elements.
     */
    public void loadGameView() {
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(root);
            this.stage = (Stage) newRoomButton.getScene().getWindow();
            stage.setScene(scene);
            initChat();

            for (int i = 1; i <= 3; i++) {
                ImageView imageView = (ImageView) root.lookup("#p" + i);
                imageView.setImage(new Image(ABSOLUTE_CARD_IMAGE_PATH + "reverse_card.jpg"));
            }

            stage.show();
        });
    }

    /**
     * Initializes the chat UI elements.
     */
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

    /**
     * Displays the cards, and names.
     *
     * @param room The room for which to display the game view.
     */
    public void displayGameView(Room room) {
        Platform.runLater(() -> {
            if (room.getPlayers().size() < 4) {
                for (int cardCounter = 1; cardCounter <= 13; cardCounter++) {
                    ImageView card = (ImageView) root.lookup("#card" + cardCounter);
                    Image cardImage = new Image(ABSOLUTE_CARD_IMAGE_PATH + "reverse_card.jpg");
                    card.setImage(cardImage);
                }
            }

            Pane innerPane = (Pane) root.lookup("#pane1");
            int counter = 1;
            for (Player player : room.getPlayers()) {
                Label nameLabel = (Label) innerPane.lookup("#name" + counter);
                nameLabel.setText(player.getName());
                counter++;
            }
        });
    }

    /**
     * Updates the room list view with the provided list of rooms.
     *
     * @param rooms The list of rooms to be displayed.
     */
    public void updateRoomListView(List<Room> rooms) {
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

    /**
     * Displays a card on the table.
     *
     * @param card      The card to be displayed.
     * @param elementId The ID of the UI element representing the card.
     */
    public void displayCardOnTable(Card card, String elementId) {
        Platform.runLater(() -> {
            if (card != null) {
                ImageView cardImg = (ImageView) root.lookup(elementId);
                Image cardImage = new Image(ABSOLUTE_CARD_IMAGE_PATH + card.getImagePath());
                cardImg.setImage(cardImage);
            } else {
                ImageView cardImg = (ImageView) root.lookup(elementId);
                cardImg.setImage(null);
            }
        });
    }

    /**
     * Sets a card in the player's deck and handles onclick.
     *
     * @param cardCounter The position of the card in the deck.
     * @param card        The card to be displayed.
     * @param player      The player to whom the card belongs.
     * @param setOnClick  Whether to set a click event for the card.
     */
    public void setCardInDeck(int cardCounter, Card card, Player player, boolean setOnClick) {
        Platform.runLater(() -> {
            ImageView cardImg = (ImageView) root.lookup("#card" + cardCounter);
            Image cardImage = new Image(ABSOLUTE_CARD_IMAGE_PATH + card.getImagePath());
            cardImg.setImage(cardImage);

            if (setOnClick)
                cardImg.setOnMouseClicked(event -> controller.makeMove(card, player));
            else {
                cardImg.setOnMouseClicked(null);
            }
        });
    }

    /**
     * Removes a card from the player's deck.
     *
     * @param number The position of the card to be removed.
     */
    public void removeCardFromDeck(int number) {
        Platform.runLater(() -> {
            ImageView card = (ImageView) root.lookup("#card" + number);
            card.setImage(null);
            card.setOnMouseClicked(null);
        });
    }

    /**
     * Displays the player's name on the table.
     *
     * @param playerInfo The information about the player.
     * @param number     The position number on the table.
     */
    public void displayNameOnTable(PlayerInfo playerInfo, int number) {
        Platform.runLater(() -> {
            Label nameLabel = (Label) root.lookup("#l" + number);
            nameLabel.setText(playerInfo.getName());
        });
    }

    /**
     * Displays the points.
     *
     * @param pointsList The list of points to be displayed.
     */
    public void displayPoints(List<Integer> pointsList) {
        Platform.runLater(() -> {
            Pane innerPane = (Pane) root.lookup("#pane1");
            int counter = 1;
            for (Integer points : pointsList) {
                Label pointsLabel = (Label) innerPane.lookup("#points" + counter);
                pointsLabel.setText(String.valueOf(points));
                counter++;
            }
        });
    }

    /**
     * Adds a message to the chat list.
     *
     * @param chatHistory The chat history to be displayed.
     */
    public void addMessageToListView(List<String> chatHistory) {
        Platform.runLater(() -> {
            ListView<String> chatListView = (ListView<String>) root.lookup("#chatList");
            chatListView.getItems().clear();
            chatListView.getItems().addAll(chatHistory);

        });
    }

    /**
     * Displays the end game panel and sets up the corresponding button action.
     */
    public void displayEndGamePanel() {
        Platform.runLater(() -> {
            Pane endPane = (Pane) root.lookup("#endPane");
            endPane.setVisible(true);
            Button endButton = (Button) endPane.lookup("#endButton");
            endButton.setOnAction(actionEvent -> setRoomScene());

        });
    }

    /**
     * Displays the deal number.
     *
     * @param dealNumber The deal number to be displayed.
     */
    public void displayDealNumber(int dealNumber) {
        Platform.runLater(() -> {
            Label dealLabel = (Label) root.lookup("#dealLabel");
            dealLabel.setText("Rozdanie #" + dealNumber);
        });
    }
}
