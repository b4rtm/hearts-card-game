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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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


    public void initialize() {
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
                Label nameLabel = (Label) innerPane.lookup("#name" + counter);
                nameLabel.setText(player.getName());
                counter++;
            }
        });
    }

    public void updateGameView(GameState gameState){

        Platform.runLater(() -> {
            Pane innerPane = (Pane) root.lookup("#pane1");
//            int counter=1;
//            for (Integer points : gameState.getPoints()){
//                Label pointsLabel = (Label) innerPane.lookup("#points" + counter);
//                pointsLabel.setText(String.valueOf(points));
//                counter++;
//            }


            for (int cardCounter=1; cardCounter<=gameState.getPlayer().getCards().size(); cardCounter++){
                ImageView card = (ImageView) root.lookup("#card" + cardCounter);
                int finalCardCounter1 = cardCounter;
                Image cardImage = new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\" + gameState.getPlayer().getCards().get(finalCardCounter1 -1).getImagePath());
                card.setImage(cardImage);

                int finalCardCounter = cardCounter;
                if(gameState.getTurn() == gameState.getPlayer().getId())
                    card.setOnMouseClicked(event -> {
                        serverCommunication.sendToServer("MOVE", new Move(gameState.getPlayer().getCards().get(finalCardCounter -1), gameState.getPlayer()));
                    });
                else{
                    card.setOnMouseClicked(null);
                }

            }

            for (int blankCardCounter = gameState.getPlayer().getCards().size(); blankCardCounter<=13;blankCardCounter++){
                ImageView card = (ImageView) root.lookup("#card" + blankCardCounter);
                card.setImage(null);
            }

            for (Map.Entry<PlayerInfo, Card> entry : gameState.getCardsOnTable().entrySet()) {
                PlayerInfo playerInfo = entry.getKey();
                Card card = entry.getValue();

                System.out.println("PlayerInfo ID: " + playerInfo.getId() + ", Card: " + (card == null ? "null" : card.getImagePath()));
            }

            PlayerInfo foundPlayer = getPlayerInfo(gameState);
            displayCardOnTable(gameState, foundPlayer,"#yourCard");

            PlayerInfo playerInfoLowest = findPlayerWithLowestId(player.getId(), gameState.getCardsOnTable());
            displayCardOnTable(gameState, playerInfoLowest,"#leftCard");

            PlayerInfo playerInfoMiddle = findPlayerWithMiddleId(player.getId(), gameState.getCardsOnTable());
            displayCardOnTable(gameState, playerInfoMiddle,"#topCard");

            PlayerInfo playerInfoHighest = findPlayerWithHighestId(player.getId(), gameState.getCardsOnTable());
            displayCardOnTable(gameState, playerInfoHighest,"#rightCard");

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                if(!gameState.getCardsOnTable().containsValue(null)){
                    serverCommunication.sendToServer("CLEAR_TABLE", gameState.getRoomId());
                }
            }, 2, TimeUnit.SECONDS);

        });


    }

    private void displayCardOnTable(GameState gameState, PlayerInfo foundPlayer, String elementId) {
        Card yourCardOnTable = gameState.getCardsOnTable().get(foundPlayer);

        if(yourCardOnTable != null) {
            ImageView card = (ImageView) root.lookup(elementId);
            Image cardImage = new Image("file:" + "C:\\Users\\barte\\IdeaProjects\\Hearts\\src\\main\\resources\\com\\example\\hearts\\client\\cards\\" + yourCardOnTable.getImagePath());
            card.setImage(cardImage);
        }
        else {
            ImageView card = (ImageView) root.lookup(elementId);
            card.setImage(null);
        }
    }

    public PlayerInfo findPlayerWithLowestId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .min(Comparator.comparingInt(PlayerInfo::getId))
                .orElse(null);
    }

    public PlayerInfo findPlayerWithMiddleId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .sorted(Comparator.comparingInt(PlayerInfo::getId))
                .skip(1) // Zawsze pomijamy drugi element, bo zawsze są cztery elementy
                .findFirst()
                .orElse(null);
    }

    public PlayerInfo findPlayerWithHighestId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .max(Comparator.comparingInt(PlayerInfo::getId))
                .orElse(null);
    }

    private static PlayerInfo getPlayerInfo(GameState gameState) {
        PlayerInfo foundPlayer = null;
        Set<PlayerInfo> playerInfos = gameState.getCardsOnTable().keySet();
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo.getId() == gameState.getPlayer().getId()) {
                foundPlayer = playerInfo;
                break;
            }
        }
        return foundPlayer;
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