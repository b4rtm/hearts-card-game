package com.example.hearts;

import java.io.Serializable;

public class Card implements Serializable {

    private Rank rank;
    private Suit suit;
    private String imagePath;


    public Card(Rank rank, Suit suit, String imagePath) {
        this.rank = rank;
        this.suit = suit;
        this.imagePath = imagePath;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public String getImagePath() {
        return imagePath;
    }
}
