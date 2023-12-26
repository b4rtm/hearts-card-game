package com.example.hearts;

import java.io.Serializable;
import java.util.List;

public class Player implements Serializable {

    private int id;
    private String name;
    private List<Card> cards;

    public Player(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
