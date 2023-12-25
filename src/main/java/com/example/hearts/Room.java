package com.example.hearts;

import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {


    private int id;
    private List<Player> players;

    public Room(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Room ID: " + id + ", Players: " +  (players != null ? players.size() : 0);
    }
}
