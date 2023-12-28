package com.example.hearts;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {

    private Player player;
    private List<Integer> points;

    public GameState(Player player, List<Integer> points) {
        this.player = player;
        this.points = points;
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
}
