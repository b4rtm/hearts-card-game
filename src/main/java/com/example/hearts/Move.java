package com.example.hearts;

import java.io.Serializable;

public class Move implements Serializable {

    private Card card;
    private Player player;
//    private Room room;

    public Move(Card card, Player player) {
        this.card = card;
        this.player = player;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

}
