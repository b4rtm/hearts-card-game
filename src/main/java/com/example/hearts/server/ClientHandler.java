package com.example.hearts.server;

import com.example.hearts.Player;
import com.example.hearts.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{

    private Player player;
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Server server;

    public ClientHandler(Socket socket, int id, Server server, ObjectOutputStream outputStream) {
        this.socket = socket;
        this.player = new Player(id);
        this.server = server;
        this.outputStream = outputStream;
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
        try {
            String message = (String) inputStream.readObject();
            System.out.println(message);
            server.getRooms().add(new Room(5));
            List<Player> players = new ArrayList<>();
            players.add(player);
            server.getRooms().get(0).setPlayers(players);
            outputStream.writeObject(server.getRooms());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("nie udało się wysłać obiektu");
            return;
        }

//        while(socket.isConnected()){
//            Reservation newReservation = listenToNewOrders();
//            updateNewOrder(newReservation);
//            if(newReservation.getHour() == 0) {
//                server.getClientOutputStreams().set(newReservation.getClientId(),null);
//                break;
//            }
//        }
//        close();
    }

//    private void updateNewOrder(Reservation newReservation) { // rozsyla wszystkim
//        for (ObjectOutputStream out : server.getClientOutputStreams()){
//            if (out == null)
//                continue;
//            try {
//                out.writeObject(newReservation);
//            } catch (IOException e) {
//                System.out.println("nie udało się wysłać obiektu");
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private synchronized Reservation listenToNewOrders() {
//        try {
//            Reservation newReservation = (Reservation) inputStream.readObject();
//
//            if(newReservation.getHour() == 0)
//                return newReservation;
//
//            for (Reservation reservation : server.getSchedule()){
//                if (searchReservation(newReservation, reservation)){
//                    int index = server.getSchedule().indexOf(reservation);
//                    server.getSchedule().set(index, newReservation);
//                    return newReservation;
//                }
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            System.out.println("nie udało się odczytać obiektu");
//            throw new RuntimeException();
//        }
//        return null;
//    }
//
//    private static boolean searchReservation(Reservation newReservation, Reservation reservation) {
//        return reservation.getHour() == newReservation.getHour() && reservation.getDayOfWeek() == newReservation.getDayOfWeek();
//    }
//
//    public void close(){
//        try{
//            if(socket != null){
//                socket.close();
//            }
//            if(outputStream != null){
//                outputStream.close();
//            }
//            if(inputStream != null){
//                inputStream.close();
//            }
//        }
//        catch (IOException e){
//            System.out.println("Nie udało się zamknąć zasobu");
//        }
//    }

}
