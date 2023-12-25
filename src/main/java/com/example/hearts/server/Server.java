package com.example.hearts.server;

import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static final int PORT = 9997;
    private final List<ObjectOutputStream> clientOutputStreams;
    private List<Room> rooms;


    public Server() {
        clientOutputStreams = new ArrayList<>();
        rooms = new ArrayList<>();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<ObjectOutputStream> getClientOutputStreams() {
        return clientOutputStreams;
    }

    public static void main(String[] args) {

        Server server= new Server();

        int clientCounter = 0;
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("serwer zaczął działać");
            while (!serverSocket.isClosed()){
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println("zaakceptowano klienta o id " + clientCounter);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    server.clientOutputStreams.add(out);

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
