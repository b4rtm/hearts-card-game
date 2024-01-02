package com.example.hearts.server;

import com.example.hearts.Player;
import com.example.hearts.PlayerInfo;
import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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

    private void displayGameInfo(){
        while (true){
            System.out.println("Napisz \"info\" aby wyświetlić stan serwera");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if(input.equals("info")){
                System.out.println("Pokoje:");
                for (Room room : rooms){
                    System.out.println("Pokój #" + room.getRoomId());
                    for (Player player : room.getPlayers()){
                        System.out.println("    " + player.getName() + " " + player.getPoints() + " PKT");
                    }
                }
            }

        }
    }

    public static void main(String[] args) {
        
        

        Server server= new Server();

        int clientCounter = 0;
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("serwer zaczął działać");
            Thread displayGameInfo = new Thread(() -> {
                server.displayGameInfo();
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
