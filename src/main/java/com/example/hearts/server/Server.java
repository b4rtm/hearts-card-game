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

/**
 * The Server class represents the Hearts game server.
 */
public class Server {

    public static final int PORT = 9997;
    private final Map<Player, ObjectOutputStream> clientOutputStreams;
    private List<Room> rooms;
    private List<ClientHandler> clientHandlers;

    /**
     * Constructs a new server with an empty list of game rooms, client handlers, and client output streams.
     */
    public Server() {
        clientOutputStreams = Collections.synchronizedMap(new HashMap<>());
        rooms = Collections.synchronizedList(new ArrayList<>());
        clientHandlers = new ArrayList<>();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Map<Player, ObjectOutputStream> getClientOutputStreams() {
        return clientOutputStreams;
    }

    /**
     * Listens to console input for server commands.
     */
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

                synchronized (rooms) {
                    ClientHandler roomClientHandler = clientHandlers.stream().filter(clientHandler -> clientHandler.getRoomId() == room.getRoomId()).findFirst().orElse(null);
                    roomClientHandler.goToNextDeal();
                    roomClientHandler.broadcastGameStateToRoom();
                }
            }
            if (input.equals("info")) {
                displayServerInfo();
            }
        }
    }

    /**
     * Displays information about the server, including the list of active game rooms and players in each room.
     */
    synchronized private void displayServerInfo() {
        System.out.println("Pokoje:");
        for (Room room : rooms) {
            System.out.println("Pokój #" + room.getRoomId());
            for (Player player : room.getPlayers()) {
                System.out.println("    GRACZ " + player.getName() + " " + player.getPoints() + " PKT");
            }
        }
    }

    /**
     * Retrieves a room based on the room number specified in the input pattern.
     *
     * @param matcher The matcher containing the room number.
     * @return The Room object corresponding to the room number.
     */
    synchronized private Room getRoomFromPattern(Matcher matcher) {
        String stringNumber = matcher.group(1);
        int roomId = Integer.parseInt(stringNumber);
        Room room = Room.getRoomById(rooms, roomId);
        return room;
    }

    /**
     * Creates a pattern matcher for the given input string.
     *
     * @param input The input string to be matched.
     * @return The Matcher object for the given input.
     */
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

    /**
     * Listens to new client connections and starts a new ClientHandler thread for each connected client.
     *
     * @param server         The server instance.
     * @param clientCounter  The counter for assigning unique IDs to clients.
     * @param serverSocket   The server socket.
     */
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
