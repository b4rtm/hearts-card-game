package com.example.hearts.client;

import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerCommunicationHandler {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public void connectToServer(String serverAddress, int port) {
        try {
            // Nawiązywanie połączenia z serwerem
            socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // Obsługa błędów, np. informacja dla użytkownika o problemie z połączeniem
        }
    }

    public void sendToServer(Object data) {
        try {
            // Wysyłanie danych do serwera
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Obsługa błędów, np. informacja dla użytkownika o problemie z wysłaniem danych
        }
    }

    public Object receiveFromServer() {
        // Implementacja odbierania danych od serwera
        return null;
    }

    public void closeConnection() {
        // Implementacja zamykania połączenia
    }

    public ArrayList<Room> receiveRooms() {
        try {
            return (ArrayList<Room>) inputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException();
        }

    }
}
