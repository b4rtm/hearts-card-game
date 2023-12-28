package com.example.hearts;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit && Objects.equals(imagePath, card.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit, imagePath);
    }
}
