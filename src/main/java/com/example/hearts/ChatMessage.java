package com.example.hearts;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private int playerId;
    private String playerName;
    private String message;

    public ChatMessage(int playerId, String playerName, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
