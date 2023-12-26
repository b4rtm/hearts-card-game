package com.example.hearts.client;

import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ServerCommunicationHandler {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(String action, Object data) {
        try {
            outputStream.writeUTF(action);
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessagesFromServer(HelloController controller) {
        try {
            while (true) {
                String action = inputStream.readUTF();
                switch (action) {
                    case "ROOMS":
                        List <Room> newRoom = (List <Room>) inputStream.readObject();
                        controller.updateRoomsList(newRoom);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        // Implementacja zamykania połączenia
    }

}
