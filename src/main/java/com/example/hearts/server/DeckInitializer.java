package com.example.hearts.server;

import com.example.hearts.Card;
import com.example.hearts.Player;
import com.example.hearts.Rank;
import com.example.hearts.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The DeckInitializer class provides methods for initializing and dealing cards in a deck.
 */
public class DeckInitializer {

    /**
     * Initializes a shuffled deck of cards.
     *
     * @return The list of initialized and shuffled cards.
     */
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

    /**
     * Converts the Rank enum to a string representation.
     *
     * @param rank The Rank enum value.
     * @return The string representation of the Rank.
     */
    private static String convertRankToString(Rank rank) {
        return switch (rank) {
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case TEN -> "10";
            default -> rank.toString().toLowerCase();
        };
    }

    /**
     * Deals cards from the deck to the specified list of players.
     *
     * @param deck    The deck of cards.
     * @param players The list of players to whom cards will be dealt.
     */
    public static void dealCardsToPlayers(List<Card> deck, List<Player> players) {
        players.forEach(player -> player.getCards().clear());
        for (int i = 0; i < players.size(); i++) {
            for (int j = i; j < deck.size(); j += players.size()) {
                players.get(i).getCards().add(deck.get(j));
            }
        }
    }
}
