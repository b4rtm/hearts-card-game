package com.example.hearts.client;

import com.example.hearts.ChatMessage;
import com.example.hearts.GameState;
import com.example.hearts.Player;
import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Handles communication with the server, including sending and receiving messages.
 */
public class ServerCommunicationHandler {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    /**
     * Connects to the server at the specified address and port.
     *
     * @param serverAddress The address of the server.
     * @param port          The port to connect to.
     */
    public void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends a message to the server with the specified action and data.
     *
     * @param action Title of the action serviced by the server.
     * @param data   The data associated with the action.
     */
    public void sendToServer(String action, Object data) {
        try {
            outputStream.writeUTF(action);
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads messages from the server and updates the controller.
     *
     * @param controller The controller.
     */
    synchronized public void readMessagesFromServer(Controller controller) {
        try {
            while (socket.isConnected()) {
                String action = inputStream.readUTF();
                switch (action) {
                    case "PLAYER" -> controller.setPlayer((Player) inputStream.readObject());
                    case "ROOMS" -> {
                        List<Room> newRoom = (List<Room>) inputStream.readObject();
                        controller.updateRoomsList(newRoom);
                    }
                    case "CHAT_MESSAGE" -> {
                        ChatMessage message = (ChatMessage) inputStream.readObject();
                        controller.updateChat(message);
                    }
                    case "GAME_STATE" -> {
                        GameState gameState = (GameState) inputStream.readObject();
                        controller.updateGameView(gameState);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (socket.isClosed())
                System.out.println("socket closed");
            else
                e.printStackTrace();
        }
    }

    /**
     * Closes the connection to the server.
     *
     * @param id The ID of the player to disconnect.
     * @throws IOException If an IO error occurs.
     */
    public void closeConnection(int id) throws IOException {
        sendToServer("QUIT", id);
        socket.close();
    }

}
