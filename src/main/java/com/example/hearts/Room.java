package com.example.hearts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room implements Serializable  {


    private int roomId;
    private List<Player> players = new ArrayList<>();
    private Map<Integer, Card> cardsOnTable = new HashMap<>();

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

    public Map<Integer, Card> getCardsOnTable() {
        return cardsOnTable;
    }

    public void setCardsOnTable(Map<Integer, Card> cardsOnTable) {
        this.cardsOnTable = cardsOnTable;
    }

    @Override
    public String toString() {
        return "#" + roomId + "   " +  (players != null ? players.size() : 0) + "/4";
    }
}
