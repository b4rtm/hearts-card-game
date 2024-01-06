package com.example.hearts.server;

import com.example.hearts.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientHandler implements Runnable {

    private Player player;
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Server server;
    private Room room;

    public ClientHandler(Socket socket, int id, Server server, ObjectOutputStream outputStream) {
        this.socket = socket;
        this.player = new Player(id);
        this.server = server;
        this.outputStream = outputStream;
        server.getClientOutputStreams().put(this.player, outputStream);
        try {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("nie udało się utworzyć zasobu");
            throw new RuntimeException(e);
        }
    }

    public Integer getRoomId() {
        return room.getRoomId();
    }

    @Override
    public void run() {
        try {
            sendRoomsToClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (socket.isConnected()) {
            try {
                String action = inputStream.readUTF();
                switch (action) {
                    case "NAME" -> {
                        String name = (String) inputStream.readObject();
                        player.setName(name);
                        sendMessage("PLAYER", player, outputStream);
                    }
                    case "CREATE_ROOM" -> {
                        inputStream.readObject();
                        createRoom();
                    }
                    case "CHAT_MESSAGE" -> {
                        ChatMessage message = (ChatMessage) inputStream.readObject();
                        broadcastMessageToRoom(message);
                    }
                    case "JOIN_ROOM" -> {
                        Integer roomId = (Integer) inputStream.readObject();
                        this.room = findRoomById(server.getRooms(), roomId);
                        joinRoom();

                        if (isRoomFull()) {
                            initializeGame();
                            broadcastGameStateToRoom();
                        }
                    }
                    case "MOVE" -> {
                        Move move = (Move) inputStream.readObject();
                        if (!HeartsRules.isMoveValid(move, findRoomById(server.getRooms(), this.room.getRoomId())))
                            break;
                        this.room.findPlayerById(move.getPlayer().getId()).getCards().remove(move.getCard());
                        setCardOnTable(move);
                        this.room.setNextTurn();
                        broadcastGameStateToRoom();
                    }
                    case "CLEAR_TABLE" -> {
                        Integer roomToCleanId = (Integer) inputStream.readObject();
                        int looser = HeartsRules.setPointsToPlayersAfterTurn(findRoomById(server.getRooms(), roomToCleanId));
                        clearRoom(roomToCleanId, looser);
                        if (this.room.getTurnNumber() == 14) {
                            startNewDeal();
                        }
                        broadcastGameStateToRoom();
                    }
                    case "QUIT" -> {
                        Integer playerId = (Integer) inputStream.readObject();
                        disconnectPlayer(playerId);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnectPlayer(Integer playerId) throws IOException {
        Player playerToDelete = Player.getPlayerById(room.getPlayers(), playerId);
        server.getClientOutputStreams().remove(playerToDelete);
        if (this.room != null)
            this.room.getPlayers().remove(playerToDelete);
        socket.close();
    }

    private void startNewDeal() {
        List<Card> deck = DeckInitializer.initializeDeck();
        DeckInitializer.dealCardsToPlayers(deck, this.room.getPlayers());
        this.room.setDealNumber(this.room.getDealNumber() + 1);
        this.room.setTurnNumber(1);

        if (this.room.getDealNumber() == 8) {
            this.room.setEndGame(true);
        }
    }

    private void clearRoom(Integer roomToCleanId, int looser) {
        Room roomToClean = findRoomById(server.getRooms(), roomToCleanId);
        for (Map.Entry<PlayerInfo, Card> entry : roomToClean.getCardsOnTable().entrySet()) {
            entry.setValue(null);
        }
        this.room.setTurn(looser);
        this.room.setStartTurn(looser);
        this.room.setTurnNumber(this.room.getTurnNumber() + 1);
    }

    private void setCardOnTable(Move move) {
        PlayerInfo foundPlayer = null;
        Set<PlayerInfo> playerInfos = this.room.getCardsOnTable().keySet();
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo.getId() == move.getPlayer().getId()) {
                foundPlayer = playerInfo;
                break;
            }
        }
        this.room.getCardsOnTable().replace(foundPlayer, move.getCard());
    }

    private void initializeGame() {
        List<Card> deck = DeckInitializer.initializeDeck();
        DeckInitializer.dealCardsToPlayers(deck, room.getPlayers());

        for (Player player1 : room.getPlayers()) {
            room.getCardsOnTable().put(new PlayerInfo(player1.getId(), player1.getName(), player1.getPoints()), null);
        }
        server.getRooms().set(server.getRooms().indexOf(findRoomById(server.getRooms(), this.room.getRoomId())), room);
        this.room.setTurn(room.getPlayers().get(0).getId());
        this.room.setStartTurn(room.getPlayers().get(0).getId());
        this.room.setDealNumber(1);
        this.room.setTurnNumber(1);
    }

    private boolean isRoomFull() {
        return room.getPlayers().size() == 4;
    }

    private void joinRoom() {
        player.setPoints(0);
        room.getPlayers().add(player);
        broadcastToAll("ROOMS", server.getRooms());
    }

    private void createRoom() {
        Room newRoom = new Room(generateRoomId());
        server.getRooms().add(newRoom);
        broadcastToAll("ROOMS", server.getRooms());
    }

    private void sendRoomsToClient() throws IOException {
        outputStream.writeUTF("ROOMS");
        List<Room> rooms = new ArrayList<>(server.getRooms());
        outputStream.writeObject(rooms);
    }

    public Room findRoomById(List<Room> rooms, int targetId) {
        for (Room room : rooms) {
            if (room.getRoomId() == targetId) {
                return room;
            }
        }
        return null;
    }


    private int generateRoomId() {
        return server.getRooms().size() + 1;
    }

    synchronized private void broadcastToAll(String action, Object data) {
        for (ObjectOutputStream outputStr : server.getClientOutputStreams().values()) {
            try {
                sendMessage(action, data, outputStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void broadcastGameStateToRoom() {
        List<Integer> pointsList = new ArrayList<>();
        for (Player player : this.room.getPlayers()) {
            pointsList.add(player.getPoints());
        }
        for (Player player1 : this.room.getPlayers()) {
            try {
                GameState gameState = new GameState(room.getRoomId(), player1, room.getCardsOnTable(), room.getTurn(), pointsList, room.isEndGame(), room.getDealNumber());
                sendMessage("GAME_STATE", gameState, server.getClientOutputStreams().get(player1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void goToNextDeal() {
        List<Card> deck = DeckInitializer.initializeDeck();
        DeckInitializer.dealCardsToPlayers(deck, this.room.getPlayers());
        this.room.setDealNumber(this.room.getDealNumber() + 1);
        this.room.setTurnNumber(1);
    }

    synchronized private void broadcastMessageToRoom(ChatMessage message) {
        for (Player player1 : this.room.getPlayers()) {
            try {
                sendMessage("CHAT_MESSAGE", message, server.getClientOutputStreams().get(player1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendMessage(String action, Object data, ObjectOutputStream outputStr) throws IOException {
        outputStr.reset();
        outputStr.writeUTF(action);
        outputStr.writeObject(data);
        outputStr.flush();
    }

}
