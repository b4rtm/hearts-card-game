package com.example.hearts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {

    private int roomId;
    private Player player;

    private Map<PlayerInfo, Card> cardsOnTable = new HashMap<>();
    private int turn;
    private List<Integer> pointsList;

    public GameState(int roomId, Player player, Map<PlayerInfo, Card> cardsOnTable, int turn, List<Integer> pointsList) {
        this.roomId = roomId;
        this.player = player;
        this.cardsOnTable = cardsOnTable;
        this.turn = turn;
        this.pointsList = pointsList;
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


    public Map<PlayerInfo, Card> getCardsOnTable() {
        return cardsOnTable;
    }

    public void setCardsOnTable(Map<PlayerInfo, Card> cardsOnTable) {
        this.cardsOnTable = cardsOnTable;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<Integer> getPointsList() {
        return pointsList;
    }

    public void setPointsList(List<Integer> pointsList) {
        this.pointsList = pointsList;
    }
}
