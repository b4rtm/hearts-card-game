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

    /**
     *  Initializes the controller by establishing a connection to the server and initializing the view.
     */
    public void initialize() {
        this.serverCommunication = new ServerCommunicationHandler();
        serverCommunication.connectToServer("localhost", 9997);
        view = new View(this);
    }

    /**
     * Gets the player associated with this controller.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the player associated with this controller.
     *
     * @param player The player to set.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Handles the client initialization, sending the player's name to the server, loading the room view, and starting the listening thread.
     *
     * @param event The ActionEvent triggered by the user.
     */
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

    /**
     * Creates a new room by sending message to the server.
     */
    public void createNewRoom() {
        serverCommunication.sendToServer("CREATE_ROOM", 0);
    }

    /**
     * Sends a chat message to the server.
     *
     * @param message The message to send.
     */
    public void sendChatMessage(String message) {
        serverCommunication.sendToServer("CHAT_MESSAGE", new ChatMessage(player.getId(), player.getName(), message));
    }

    /**
     * Updates the game view based on the provided game state, including handling end game conditions, points, cards in the deck, and cards on the table.
     *
     * @param gameState The current game state.
     */
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

    /**
     * Clears the table from cards if needed after a delay.
     *
     * @param gameState The current game state.
     */
    private void clearTableFromCardsIfNeeded(GameState gameState) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            if (!gameState.getCardsOnTable().containsValue(null) && (gameState.getPlayer().getId() == gameState.getTurn())) {
                serverCommunication.sendToServer("CLEAR_TABLE", gameState.getRoomId());
            }
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Sets the cards in the player's deck.
     *
     * @param gameState The current game state.
     */
    private void setCardsInDeck(GameState gameState) {
        for (int cardCounter = 1; cardCounter <= gameState.getPlayer().getCards().size(); cardCounter++) {
            view.setCardInDeck(cardCounter, gameState.getPlayer().getCards().get(cardCounter - 1), gameState.getPlayer(), gameState.getTurn() == gameState.getPlayer().getId());
        }

        for (int blankCardCounter = gameState.getPlayer().getCards().size() + 1; blankCardCounter <= 13; blankCardCounter++) {
            view.removeCardFromDeck(blankCardCounter);
        }
    }

    /**
     * Updates the display of cards on the table based on the provided game state.
     *
     * @param gameState The current game state.
     */
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

    /**
     * Displays a player on the table with their card and name.
     *
     * @param gameState    The current game state.
     * @param playerInfo   Information about the player to display.
     * @param cardElementId The ID of the UI element representing the card.
     * @param position      The position on the table.
     */
    private void displayPlayerOnTable(GameState gameState, PlayerInfo playerInfo, String cardElementId, int position) {
        view.displayCardOnTable(gameState.getCardsOnTable().get(playerInfo), cardElementId);
        view.displayNameOnTable(playerInfo, position);
    }

    /**
     * Sends a move message to the server based on the selected card and current player.
     *
     * @param card   The card selected by the player.
     * @param player The current player.
     */
    public void makeMove(Card card, Player player) {
        serverCommunication.sendToServer("MOVE", new Move(card, player));
    }

    /**
     * Closes the connection to the server.
     */
    public void onClose() {
        try {
            serverCommunication.closeConnection(player.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the list of available rooms and displays the game view if the current player is part of any room.
     *
     * @param rooms The list of available rooms.
     */
    public void updateRoomsList(List<Room> rooms) {
        view.updateRoomListView(rooms);

        for (Room room : rooms) {
            if (room.getPlayers().contains(player)) {
                view.displayGameView(room);
                break;
            }
        }
    }

    /**
     * Updates the chat history and formats messages to be displayed in the chat ListView.
     *
     * @param message The chat message to be added to the history.
     */
     public void updateChat(ChatMessage message) {
        chatHistory.add(message);
        List<String> formattedMessages = chatHistory.stream()
                .map(chatMessage -> chatMessage.getPlayerName() + ": " + chatMessage.getMessage())
                .collect(Collectors.toList());
        view.addMessageToListView(formattedMessages);
    }

    /**
     * Joins the selected room by sending a  message to the server.
     *
     * @param selectedRoom The room to join.
     */
    public void joinRoom(Room selectedRoom) {
        serverCommunication.sendToServer("JOIN_ROOM", selectedRoom.getRoomId());
    }

}