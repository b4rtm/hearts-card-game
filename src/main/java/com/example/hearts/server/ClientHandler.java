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

/**
 * The ClientHandler class manages the communication with a connected client.
 */
public class ClientHandler implements Runnable {

    private Player player;
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Server server;
    private Room room;

    /**
     * Constructs a new ClientHandler for the specified socket, player ID, server, and output stream.
     *
     * @param socket       The socket associated with the client.
     * @param id           The ID of the player.
     * @param server       The server instance.
     * @param outputStream The output stream for sending messages to the client.
     */
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

    /**
     * Runs the main loop for handling client messages.
     */
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
                        joinRoom(roomId);

                        if (isRoomFull()) {
                            initializeGame();
                            broadcastGameStateToRoom();
                        }
                    }
                    case "MOVE" -> {
                        Move move = (Move) inputStream.readObject();
                        synchronized (server.getRooms()) {
                            if (!HeartsRules.isMoveValid(move, findRoomById(server.getRooms(), this.room.getRoomId())))
                                break;
                            this.room.findPlayerById(move.getPlayer().getId()).getCards().remove(move.getCard());
                            setCardOnTable(move);
                            this.room.setNextTurn();
                        }
                        broadcastGameStateToRoom();
                    }
                    case "CLEAR_TABLE" -> {
                        Integer roomToCleanId = (Integer) inputStream.readObject();
                        synchronized (server.getRooms()) {
                            int looser = HeartsRules.setPointsToPlayersAfterTurn(findRoomById(server.getRooms(), roomToCleanId));
                            clearRoom(roomToCleanId, looser);
                            if (this.room.getTurnNumber() == 14) {
                                startNewDeal();
                            }
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

    /**
     * Disconnects a player based on the specified player ID.
     *
     * @param playerId The ID of the player to be disconnected.
     * @throws IOException If an I/O error occurs during the disconnection process.
     */
    synchronized private void disconnectPlayer(Integer playerId) throws IOException {
        Player playerToDelete = Player.getPlayerById(room.getPlayers(), playerId);
        server.getClientOutputStreams().remove(playerToDelete);
        if (this.room != null)
            this.room.getPlayers().remove(playerToDelete);
        socket.close();
    }

    /**
     * Starts a new deal by shuffling and dealing cards.
     */
    synchronized private void startNewDeal() {
        List<Card> deck = DeckInitializer.initializeDeck();
        DeckInitializer.dealCardsToPlayers(deck, this.room.getPlayers());
        this.room.setDealNumber(this.room.getDealNumber() + 1);
        this.room.setTurnNumber(1);

        if (this.room.getDealNumber() == 8) {
            this.room.setEndGame(true);
        }
    }

    /**
     * Clears the table after a turn and updates the game state.
     *
     * @param roomToCleanId The ID of the room to be cleaned.
     * @param looser        The ID of the player who lost the turn.
     */
    synchronized private void clearRoom(Integer roomToCleanId, int looser) {
        Room roomToClean = findRoomById(server.getRooms(), roomToCleanId);
        for (Map.Entry<PlayerInfo, Card> entry : roomToClean.getCardsOnTable().entrySet()) {
            entry.setValue(null);
        }
        this.room.setTurn(looser);
        this.room.setStartTurn(looser);
        this.room.setTurnNumber(this.room.getTurnNumber() + 1);
    }

    /**
     * Places a card on the table based on the player's move.
     *
     * @param move The move made by the player.
     */
    synchronized private void setCardOnTable(Move move) {
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

    /**
     * Initializes the game by dealing cards and setting up initial game state.
     */
    synchronized private void initializeGame() {
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

    /**
     * Checks if the room is full (has 4 players).
     *
     * @return True if the room is full, false otherwise.
     */
    private boolean isRoomFull() {
        return room.getPlayers().size() == 4;
    }

    /**
     * Handles a player joining a room by setting up the player's points and broadcasting the updated room list.
     */
    synchronized private void joinRoom(int roomId) {
        this.room = findRoomById(server.getRooms(), roomId);
        player.setPoints(0);
        room.getPlayers().add(player);
        broadcastToAll("ROOMS", server.getRooms());
    }

    /**
     * Creates a new room and broadcasts the updated room list to all clients.
     */
    synchronized private void createRoom() {
        Room newRoom = new Room(generateRoomId());
        server.getRooms().add(newRoom);
        broadcastToAll("ROOMS", server.getRooms());
    }

    /**
     * Sends the list of available rooms to the client.
     *
     * @throws IOException If an I/O error occurs during the message sending process.
     */
    synchronized private void sendRoomsToClient() throws IOException {
        outputStream.writeUTF("ROOMS");
        List<Room> rooms = new ArrayList<>(server.getRooms());
        outputStream.writeObject(rooms);
    }

    /**
     * Finds a room with the specified ID in the provided list of rooms.
     *
     * @param rooms    The list of rooms to search.
     * @param targetId The target room ID.
     * @return The room with the specified ID, or {@code null} if not found.
     */
    public Room findRoomById(List<Room> rooms, int targetId) {
        for (Room room : rooms) {
            if (room.getRoomId() == targetId) {
                return room;
            }
        }
        return null;
    }

    /**
     * Generates a unique room ID based on the current number of rooms.
     *
     * @return The generated room ID.
     */
    synchronized private int generateRoomId() {
        return server.getRooms().size() + 1;
    }

    /**
     * Broadcasts a message with the specified action and data to all connected clients.
     *
     * @param action The action of the message.
     * @param data   The data to be broadcasted.
     */
    synchronized private void broadcastToAll(String action, Object data) {
        for (ObjectOutputStream outputStr : server.getClientOutputStreams().values()) {
            try {
                sendMessage(action, data, outputStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Broadcasts the current game state to all players in the room.
     */
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

    /**
     * Proceeds to the next deal by shuffling and dealing cards.
     */
    synchronized public void goToNextDeal() {
        List<Card> deck = DeckInitializer.initializeDeck();
        DeckInitializer.dealCardsToPlayers(deck, this.room.getPlayers());
        this.room.setDealNumber(this.room.getDealNumber() + 1);
        this.room.setTurnNumber(1);
    }

    /**
     * Broadcasts a chat message to all players in the room.
     *
     * @param message The chat message to be broadcasted.
     */
    synchronized private void broadcastMessageToRoom(ChatMessage message) {
        for (Player player1 : this.room.getPlayers()) {
            try {
                sendMessage("CHAT_MESSAGE", message, server.getClientOutputStreams().get(player1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a message with the specified action and data to the specified output stream.
     *
     * @param action     The action of the message.
     * @param data       The data to be sent.
     * @param outputStr  The output stream for sending the message.
     * @throws IOException If an I/O error occurs during the message sending process.
     */
    public static void sendMessage(String action, Object data, ObjectOutputStream outputStr) throws IOException {
        outputStr.reset();
        outputStr.writeUTF(action);
        outputStr.writeObject(data);
        outputStr.flush();
    }

}
