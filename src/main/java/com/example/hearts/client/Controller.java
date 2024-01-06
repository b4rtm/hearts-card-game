package com.example.hearts.client;

import com.example.hearts.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Controller {

    private ServerCommunicationHandler serverCommunication;
    private View view;
    private Player player;
    private List<ChatMessage> chatHistory = new ArrayList<>();

    @FXML
    private TextField nameField;

    public void initialize() {
        this.serverCommunication = new ServerCommunicationHandler();
        serverCommunication.connectToServer("localhost", 9997);
        view = new View(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @FXML
    void initClient(ActionEvent event) {
        serverCommunication.sendToServer("NAME", nameField.getText());
        view.loadRoomView(event);

        startListeningThread();
    }

    private void startListeningThread() {
        Thread readMessagesThread = new Thread(() -> serverCommunication.readMessagesFromServer(this));
        readMessagesThread.start();
    }

    public void createNewRoom() {
        serverCommunication.sendToServer("CREATE_ROOM", 0);
    }

    public void sendChatMessage(String message) {
        serverCommunication.sendToServer("CHAT_MESSAGE", new ChatMessage(player.getId(), player.getName(), message));
    }

    public void updateGameView(GameState gameState) {
        if (gameState.isEndGame()) {
            view.displayEndGamePanel();
        }

        view.displayPoints(gameState.getPointsList());
        view.displayDealNumber(gameState.getDealNumber());
        setCardsInDeck(gameState);
        putCardsOnTable(gameState);
        clearTableFromCardsIfNeeded(gameState);
    }

    private void clearTableFromCardsIfNeeded(GameState gameState) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            if (!gameState.getCardsOnTable().containsValue(null) && (gameState.getPlayer().getId() == gameState.getTurn())) {
                serverCommunication.sendToServer("CLEAR_TABLE", gameState.getRoomId());
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void setCardsInDeck(GameState gameState) {
        for (int cardCounter = 1; cardCounter <= gameState.getPlayer().getCards().size(); cardCounter++) {
            view.setCardInDeck(cardCounter, gameState.getPlayer().getCards().get(cardCounter - 1), gameState.getPlayer(), gameState.getTurn() == gameState.getPlayer().getId());
        }

        for (int blankCardCounter = gameState.getPlayer().getCards().size() + 1; blankCardCounter <= 13; blankCardCounter++) {
            view.removeCardFromDeck(blankCardCounter);
        }
    }

    private void putCardsOnTable(GameState gameState) {
        PlayerInfo foundPlayer = PlayerInfo.getPlayerInfo(gameState);
        view.displayCardOnTable(gameState.getCardsOnTable().get(foundPlayer), "#yourCard");

        PlayerInfo playerInfoLowest = PlayerInfo.findPlayerWithLowestId(player.getId(), gameState.getCardsOnTable());
        displayPlayerOnTable(gameState, playerInfoLowest, "#leftCard", 1);

        PlayerInfo playerInfoMiddle = PlayerInfo.findPlayerWithMiddleId(player.getId(), gameState.getCardsOnTable());
        displayPlayerOnTable(gameState, playerInfoMiddle, "#topCard", 2);

        PlayerInfo playerInfoHighest = PlayerInfo.findPlayerWithHighestId(player.getId(), gameState.getCardsOnTable());
        displayPlayerOnTable(gameState, playerInfoHighest, "#rightCard", 3);
    }

    private void displayPlayerOnTable(GameState gameState, PlayerInfo playerInfo, String cardElementId, int position) {
        view.displayCardOnTable(gameState.getCardsOnTable().get(playerInfo), cardElementId);
        view.displayNameOnTable(playerInfo, position);
    }

    public void makeMove(Card card, Player player) {
        serverCommunication.sendToServer("MOVE", new Move(card, player));
    }


    public void onClose() {
        try {
            serverCommunication.closeConnection(player.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateRoomsList(List<Room> rooms) {
        view.updateRoomListView(rooms);

        for (Room room : rooms) {
            if (room.getPlayers().contains(player)) {
                view.displayGameView(room);
                break;
            }
        }
    }

    synchronized public void updateChat(ChatMessage message) {
        chatHistory.add(message);
        List<String> formattedMessages = chatHistory.stream()
                .map(chatMessage -> chatMessage.getPlayerName() + ": " + chatMessage.getMessage())
                .collect(Collectors.toList());
        view.addMessageToListView(formattedMessages);
    }

    public void joinRoom(Room selectedRoom) {
        serverCommunication.sendToServer("JOIN_ROOM", selectedRoom.getRoomId());
    }

}