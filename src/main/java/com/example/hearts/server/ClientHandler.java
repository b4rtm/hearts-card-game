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

import static java.lang.Thread.sleep;

public class ClientHandler implements Runnable{

    private Player player;
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Server server;

//    private Room room;
    private Integer roomId;
//    private GameState gameState;

    public ClientHandler(Socket socket, int id, Server server, ObjectOutputStream outputStream) {

        this.socket = socket;
        this.player = new Player(id);
        this.server = server;
        this.outputStream = outputStream;
        server.getClientOutputStreams().put(this.player, outputStream);
        try{
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.out.println("nie udało się utworzyć zasobu");
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
//        try {
//            String message = (String) inputStream.readObject();
//            System.out.println(message);
//            server.getRooms().add(new Room(5));
//            List<Player> players = new ArrayList<>();
//            players.add(player);
//            server.getRooms().get(0).setPlayers(players);
//            outputStream.writeUTF("ROOMS");
//            outputStream.writeObject(server.getRooms());
//            sleep(15000);
//            server.getRooms().add(new Room(6));
//            List<Player> players2 = new ArrayList<>();
//            players2.add(new Player(44));
//            server.getRooms().get(1).setPlayers(players2);
//            outputStream.writeUTF("ROOMS");
//            List <Room> rooms = new ArrayList<>(server.getRooms());
//            outputStream.writeObject(rooms);
//
//        } catch (IOException | ClassNotFoundException | InterruptedException e) {
//            System.out.println("nie udało się wysłać obiektu");
//            return;
//        }

        try {
            outputStream.writeUTF("ROOMS");
            List <Room> rooms = new ArrayList<>(server.getRooms());
            outputStream.writeObject(rooms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        while(socket.isConnected()){
            try {
                String action = inputStream.readUTF();
                switch (action){
                    case "NAME":
                        String name = (String) inputStream.readObject();
                        player.setName(name);
                        System.out.println("new player : " + name);
                        sendMessage("PLAYER", player, outputStream);
                        break;
                    case "CREATE_ROOM":
                        inputStream.readObject();
                        Room newRoom = new Room(generateRoomId());
                        server.getRooms().add(newRoom);
                        broadcastToAll("ROOMS", server.getRooms());
                        break;
                    case "JOIN_ROOM":
                        Integer roomId = (Integer) inputStream.readObject();
                        Room room = findRoomById(server.getRooms(), roomId);
                        room.getPlayers().add(player);
                        broadcastToAll("ROOMS", server.getRooms());
                        this.roomId = roomId;

                        if(room.getPlayers().size() == 4){
                            List<Card> deck = DeckInitializer.initializeDeck();
                            DeckInitializer.dealCardsToPlayers(deck, room.getPlayers());

                            for (Player player1: room.getPlayers()) {
                                room.getCardsOnTable().put(new PlayerInfo(player1.getId(), player1.getName(), player1.getPoints()), null);
                            }
                            server.getRooms().set(server.getRooms().indexOf(findRoomById(server.getRooms(),this.roomId)),room);
                            getRoomFromServerById().setTurn(room.getPlayers().get(0).getId());
                            getRoomFromServerById().setStartTurn(room.getPlayers().get(0).getId());
                            getRoomFromServerById().setDealNumber(1);
                            broadcastGameStateToRoom();
                        }
                        break;
                    case "MOVE":
                        Move move = (Move) inputStream.readObject();
                        if(!HeartsRules.isMoveValid(move, findRoomById(server.getRooms(), this.roomId)) )
                            break;
                        getRoomFromServerById().findPlayerById(move.getPlayer().getId()).getCards().remove(move.getCard());


                        PlayerInfo foundPlayer = null;
                        Set<PlayerInfo> playerInfos = getRoomFromServerById().getCardsOnTable().keySet();
                        for (PlayerInfo playerInfo : playerInfos) {
                            if (playerInfo.getId() == move.getPlayer().getId()) {
                                foundPlayer = playerInfo;
                                break;
                            }
                        }
                        getRoomFromServerById().getCardsOnTable().replace(foundPlayer, move.getCard());

                        getRoomFromServerById().setNextTurn();



                        System.out.println(getRoomFromServerById().getTurn());
                        broadcastGameStateToRoom();

                        break;
                    case "CLEAR_TABLE":
                        Integer roomToCleanId = (Integer) inputStream.readObject();

                        int looser = HeartsRules.setPointsToPlayersAfterTurn(findRoomById(server.getRooms(), roomToCleanId));
                        System.out.println("looser: " + looser);
                        Room roomToClean = findRoomById(server.getRooms(), roomToCleanId);

                        for (Map.Entry<PlayerInfo, Card> entry : roomToClean.getCardsOnTable().entrySet()) {
                            entry.setValue(null);
                        }

                        getRoomFromServerById().setTurn(looser);
                        getRoomFromServerById().setStartTurn(looser);


                        if(getRoomFromServerById().getPlayers().stream().allMatch(player1 -> player1.getCards().size() == 0)){
                            List<Card> deck = DeckInitializer.initializeDeck();
                            DeckInitializer.dealCardsToPlayers(deck, getRoomFromServerById().getPlayers());
                            getRoomFromServerById().setDealNumber(getRoomFromServerById().getDealNumber()+1);
                            System.out.println("hahaha");
                        }



                        broadcastGameStateToRoom();
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException(e);
            }
        }


    }

    public Room findRoomById(List<Room> rooms, int targetId) {
        for (Room room : rooms) {
            if (room.getRoomId() == targetId) {
                return room;
            }
        }
        return null;
    }


    private int generateRoomId(){
        return server.getRooms().size() + 1;
    }

    private void broadcastToAll(String action, Object data) {
        for (ObjectOutputStream outputStr : server.getClientOutputStreams().values()) {
            try {
                sendMessage(action, data, outputStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastGameStateToRoom() {
        List<Integer> pointsList = new ArrayList<>();
        for (Player player : getRoomFromServerById().getPlayers()) {
            pointsList.add(player.getPoints());
        }
        for (Player player1 : getRoomFromServerById().getPlayers()) {
            try {
                GameState gameState = new GameState(getRoomFromServerById().getRoomId(), player1, getRoomFromServerById().getCardsOnTable(), getRoomFromServerById().getTurn(), pointsList);

                sendMessage("GAME_STATE", gameState, server.getClientOutputStreams().get(player1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Room getRoomFromServerById() {
        return findRoomById(server.getRooms(), this.roomId);
    }

    private static void sendMessage(String action, Object data, ObjectOutputStream outputStr) throws IOException {
        outputStr.reset();
        outputStr.writeUTF(action);
        outputStr.writeObject(data);
        outputStr.flush();
    }

}
