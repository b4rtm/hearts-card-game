package com.example.hearts.client;

import com.example.hearts.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

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
    synchronized void initClient(ActionEvent event) {
        serverCommunication.sendToServer("NAME", nameField.getText());
        view.setRoomView(event);

        Thread readMessagesThread = new Thread(() -> serverCommunication.readMessagesFromServer(this));
        readMessagesThread.start();
    }


    public void createNewRoom() {
        serverCommunication.sendToServer("CREATE_ROOM", 0);
    }

    public void sendChatMessage(String message){
        serverCommunication.sendToServer("CHAT_MESSAGE", new ChatMessage(player.getId(), player.getName(), message));
    }



    public void updateGameView(GameState gameState){

        view.displayPoints(gameState.getPointsList());

        for (int cardCounter=1; cardCounter<=gameState.getPlayer().getCards().size(); cardCounter++){
            view.setCardInDeck(cardCounter, gameState.getPlayer().getCards().get(cardCounter -1),gameState.getPlayer() , gameState.getTurn() == gameState.getPlayer().getId());
        }

        for (int blankCardCounter = gameState.getPlayer().getCards().size()+1; blankCardCounter<=13;blankCardCounter++){
            view.removeCardFromDeck(blankCardCounter);
        }

        putCardsOnTable(gameState);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            if(!gameState.getCardsOnTable().containsValue(null) && (gameState.getPlayer().getId() == gameState.getTurn())){
                serverCommunication.sendToServer("CLEAR_TABLE", gameState.getRoomId());
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void putCardsOnTable(GameState gameState) {
        PlayerInfo foundPlayer = PlayerInfo.getPlayerInfo(gameState);
        view.displayCardOnTable(gameState.getCardsOnTable().get(foundPlayer),"#yourCard");

        PlayerInfo playerInfoLowest = PlayerInfo.findPlayerWithLowestId(player.getId(), gameState.getCardsOnTable());
        view.displayCardOnTable(gameState.getCardsOnTable().get(playerInfoLowest),"#leftCard");
        view.displayNameOnTable(playerInfoLowest,1);

        PlayerInfo playerInfoMiddle = PlayerInfo.findPlayerWithMiddleId(player.getId(), gameState.getCardsOnTable());
        view.displayCardOnTable(gameState.getCardsOnTable().get(playerInfoMiddle),"#topCard");
        view.displayNameOnTable(playerInfoMiddle,2);


        PlayerInfo playerInfoHighest = PlayerInfo.findPlayerWithHighestId(player.getId(), gameState.getCardsOnTable());
        view.displayCardOnTable(gameState.getCardsOnTable().get(playerInfoHighest),"#rightCard");
        view.displayNameOnTable(playerInfoHighest,3);
    }

    public void makeMove(Card card, Player player){
        serverCommunication.sendToServer("MOVE", new Move(card, player));
    }


    public void onClose() {
        // Wywołane przy zamknięciu aplikacji, może zawierać zamykanie połączenia
        serverCommunication.closeConnection();
    }

    synchronized public void updateRoomsList(List<Room> rooms) {
        view.updateRoomListView(rooms);

        for (Room room : rooms) {
            if (room.getPlayers().contains(player)) {
                view.displayGameView(room);
                break;
            }
        }
    }

    public void updateChat(ChatMessage message){
        chatHistory.add(message);
        List<String> formattedMessages = chatHistory.stream()
                .map(chatMessage -> chatMessage.getPlayerName() + ": " + chatMessage.getMessage())
                .collect(Collectors.toList());
         view.addMessageToListView(formattedMessages);
    }
    
    public void joinRoom(Room selectedRoom){
        serverCommunication.sendToServer("JOIN_ROOM", selectedRoom.getRoomId());
    }
    
}