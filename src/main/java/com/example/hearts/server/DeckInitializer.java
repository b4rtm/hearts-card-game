package com.example.hearts.server;

import com.example.hearts.Card;
import com.example.hearts.Player;
import com.example.hearts.Rank;
import com.example.hearts.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckInitializer {

    public static List<Card> initializeDeck() {
        List<Card> deck = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                String rankString = convertRankToString(rank);
                String suitString = suit.toString().toLowerCase();
                String imagePath = rankString + "_of_" + suitString + ".png";

                Card card = new Card(rank, suit, imagePath);
                deck.add(card);
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    private static String convertRankToString(Rank rank) {
        switch (rank) {
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "10";
            default:
                return rank.toString().toLowerCase();
        }
    }

    public static void dealCardsToPlayers(List<Card> deck, List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            for (int j = i; j < deck.size(); j += players.size()) {
                players.get(i).getCards().add(deck.get(j));
            }
        }
    }
}
