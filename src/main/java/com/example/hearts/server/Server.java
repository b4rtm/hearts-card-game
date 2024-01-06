package com.example.hearts.server;

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
    private List<ClientHandler> clientHandlers = new ArrayList<>();

    public Server() {
        clientOutputStreams = Collections.synchronizedMap(new HashMap<>());
        rooms = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Map<Player, ObjectOutputStream> getClientOutputStreams() {
        return clientOutputStreams;
    }

    private void listenToRequests() {
        while (true) {
            System.out.println("Napisz \"info\" aby wyświetlić stan serwera lub \"nastepna {numer pokoju}\" aby przejść do kolejnego rozdania");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            Matcher matcher = createPattern(input);
            if (matcher.matches()) {
                Room room = getRoomFromPattern(matcher);
                if (room == null)
                    continue;

                ClientHandler roomClientHandler = clientHandlers.stream().filter(clientHandler -> clientHandler.getRoomId() == room.getRoomId()).findFirst().orElse(null);
                roomClientHandler.goToNextDeal();
                roomClientHandler.broadcastGameStateToRoom();
            }
            if (input.equals("info")) {
                displayServerInfo();
            }
        }
    }

    private void displayServerInfo() {
        System.out.println("Pokoje:");
        for (Room room : rooms) {
            System.out.println("Pokój #" + room.getRoomId());
            for (Player player : room.getPlayers()) {
                System.out.println("    GRACZ " + player.getName() + " " + player.getPoints() + " PKT");
            }
        }
    }

    private Room getRoomFromPattern(Matcher matcher) {
        String stringNumber = matcher.group(1);
        int roomId = Integer.parseInt(stringNumber);
        Room room = Room.getRoomById(rooms, roomId);
        return room;
    }

    private static Matcher createPattern(String input) {
        Pattern pattern = Pattern.compile("nastepna (\\d+)");
        Matcher matcher = pattern.matcher(input);
        return matcher;
    }

    public static void main(String[] args) {
        Server server = new Server();

        int clientCounter = 0;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("serwer zaczął działać");
            Thread displayGameInfo = new Thread(() -> server.listenToRequests());
            displayGameInfo.start();

            listenToNewConnections(server, clientCounter, serverSocket);
        } catch (IOException e) {
            System.out.println("nie udało się stworzyć socketa");
        }
    }

    private static void listenToNewConnections(Server server, int clientCounter, ServerSocket serverSocket) {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("zaakceptowano klienta o id " + clientCounter);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ClientHandler clientHandler = new ClientHandler(socket, clientCounter, server, out);
                server.clientHandlers.add(clientHandler);
                clientCounter++;

                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                System.out.println("nie udało się zaakceptować klienta");
            }
        }
    }
}
