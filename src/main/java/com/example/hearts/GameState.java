package com.example.hearts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {

    private int roomId;
    private Player player;
    private List<Integer> points;
    private Map<Integer, Card> cardsOnTable = new HashMap<>();
    private int turn;

    public GameState(int roomId, Player player, List<Integer> points, Map<Integer, Card> cardsOnTable, int turn) {
        this.roomId = roomId;
        this.player = player;
        this.points = points;
        this.cardsOnTable = cardsOnTable;
        this.turn = turn;
    }


    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public Map<Integer, Card> getCardsOnTable() {
        return cardsOnTable;
    }

    public void setCardsOnTable(Map<Integer, Card> cardsOnTable) {
        this.cardsOnTable = cardsOnTable;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}
