package com.example.hearts;

import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {


    private int roomId;
    private List<Player> players;

    public Room(int id) {
        this.roomId = id;
    }

    public Room(int roomId, List<Player> players) {
        this.roomId = roomId;
        this.players = players;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Room ID: " + roomId + ", Players: " +  (players != null ? players.size() : 0);
    }
}
