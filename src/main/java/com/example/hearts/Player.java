package com.example.hearts;

import java.io.Serializable;
import java.util.List;

public class Player implements Serializable {

    private int id;
    private List<Card> cards;

    public Player(int id) {
        this.id = id;
    }
}
