package com.example.hearts.server;

import com.example.hearts.Card;
import com.example.hearts.GameState;
import com.example.hearts.Player;
import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    public static final int PORT = 9997;
    private final Map<Player, ObjectOutputStream> clientOutputStreams;
    private List<Room> rooms;


    public Server() {
        clientOutputStreams = Collections.synchronizedMap(new HashMap<>());
        rooms = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Map<Player,ObjectOutputStream> getClientOutputStreams() {
        return clientOutputStreams;
    }

    private void listenToRequests(){
        while (true){
            System.out.println("Napisz \"info\" aby wyświetlić stan serwera lub \"nastepna {numer pokoju}\" aby przejść do kolejnego rozdania");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            Pattern pattern = Pattern.compile("nastepna (\\d+)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String stringNumber = matcher.group(1);
                int roomId = Integer.parseInt(stringNumber);
                Room room = Room.getRoomById(rooms,roomId);
                if(room == null)
                    continue;
                List<Card> deck = DeckInitializer.initializeDeck();
                DeckInitializer.dealCardsToPlayers(deck, room.getPlayers());
                room.setDealNumber(room.getDealNumber()+1);
                room.setTurnNumber(1);
                broadcastGameStateToRoom(room);
            }
            if(input.equals("info")){
                System.out.println("Pokoje:");
                for (Room room : rooms){
                    System.out.println("Pokój #" + room.getRoomId());
                    for (Player player : room.getPlayers()){
                        System.out.println("    GRACZ " + player.getName() + " " + player.getPoints() + " PKT");
                    }
                }
            }
        }
    }

    private void broadcastGameStateToRoom(Room room) {
        List<Integer> pointsList = new ArrayList<>();
        for (Player player : room.getPlayers()) {
            pointsList.add(player.getPoints());
        }
        for (Player player1 : room.getPlayers()) {
            try {
                GameState gameState = new GameState(room.getRoomId(), player1, room.getCardsOnTable(), room.getTurn(), pointsList, room.isEndGame(), room.getDealNumber());

                ClientHandler.sendMessage("GAME_STATE", gameState, clientOutputStreams.get(player1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server= new Server();

        int clientCounter = 0;
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("serwer zaczął działać");
            Thread displayGameInfo = new Thread(() -> {
                server.listenToRequests();
            });
            displayGameInfo.start();

            while (!serverSocket.isClosed()){
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println("zaakceptowano klienta o id " + clientCounter);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());


                    ClientHandler clientHandler = new ClientHandler(socket, clientCounter, server, out);
                    clientCounter++;

                    Thread thread = new Thread(clientHandler);
                    thread.start();
                }
                catch (IOException e){
                    System.out.println("nie udało się zaakceptować klienta");
                }
            }
        }
        catch (IOException e){
            System.out.println("nie udało się stworzyć socketa");
        }
    }
}
