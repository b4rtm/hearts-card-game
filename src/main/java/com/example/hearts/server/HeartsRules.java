package com.example.hearts.server;

import com.example.hearts.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The HeartsRules class defines rules for the Hearts card game.
 */
public class HeartsRules {

    /**
     * Checks if a move is valid according to Hearts game rules.
     *
     * @param move The move to be checked.
     * @param room The current game room.
     * @return True if the move is valid, false otherwise.
     */
    public static boolean isMoveValid(Move move, Room room) {

        if (isHeartsFirst(move, room))
            return false;
        if (isTableEmpty(move, room))
            return true;

        Card firstCardOnTable = getStartCard(room);

        if (isSameColor(firstCardOnTable, move.getCard())) {
            return true;
        } else if (hasCardInColor(move.getPlayer(), firstCardOnTable.getSuit())) {
            return false;
        } else {
            return true;
        }

    }

    /**
     *  Checks if first card in turn in 2, 5, or 7 deal have heart suit.
     *
     * @param move The move to be checked.
     * @param room The current game room.
     * @return True if the first card is heart, false otherwise.
     */
    private static boolean isHeartsFirst(Move move, Room room) {
        return isTableEmpty(move, room) && move.getCard().getSuit() == Suit.HEARTS && move.getPlayer().hasCardsOtherThanHearts() && (room.getDealNumber() == 2 || room.getDealNumber() == 5 || room.getDealNumber() == 7);
    }

    /**
     * Sets points to players after a turn based on Hearts game rules.
     *
     * @param room The current game room.
     * @return The ID of the player who takes the cards in this turn.
     */
    public static int setPointsToPlayersAfterTurn(Room room) {
        Card startCard = getStartCard(room);
        Player player = getPlayerWhoTakesCards(room, startCard);
        if (room.getDealNumber() == 1) {
            dealOneService(player);
        } else if (room.getDealNumber() == 2) {
            dealTwoService(room, player);
        } else if (room.getDealNumber() == 3) {
            dealThreeService(room, player);
        } else if (room.getDealNumber() == 4) {
            dealFourService(room, player);
        } else if (room.getDealNumber() == 5) {
            dealFiveService(room, player);
        } else if (room.getDealNumber() == 6) {
            dealSixService(room, player);
        } else if (room.getDealNumber() == 7) {
            dealSevenService(room, player);
        }
        return player.getId();
    }

    private static void dealSevenService(Room room, Player player) {
        dealOneService(player);
        dealTwoService(room, player);
        dealThreeService(room, player);
        dealFourService(room, player);
        dealFiveService(room, player);
        dealSixService(room, player);
    }

    private static void dealSixService(Room room, Player player) {
        if (room.getTurnNumber() == 7 || room.getTurnNumber() == 13)
            player.setPoints(player.getPoints() - 75);
    }

    private static void dealFiveService(Room room, Player player) {
        for (Card card : room.getCardsOnTable().values()) {
            if (card.getRank() == Rank.KING && card.getSuit() == Suit.HEARTS)
                player.setPoints(player.getPoints() - 150);
        }
    }

    private static void dealFourService(Room room, Player player) {
        for (Card card : room.getCardsOnTable().values()) {
            if (card.getRank() == Rank.KING || card.getRank() == Rank.JACK)
                player.setPoints(player.getPoints() - 30);
        }
    }

    private static void dealThreeService(Room room, Player player) {
        for (Card card : room.getCardsOnTable().values()) {
            if (card.getRank() == Rank.QUEEN)
                player.setPoints(player.getPoints() - 60);
        }
    }

    private static void dealTwoService(Room room, Player player) {
        for (Card card : room.getCardsOnTable().values()) {
            if (card.getSuit() == Suit.HEARTS)
                player.setPoints(player.getPoints() - 20);
        }
    }

    private static void dealOneService(Player player) {
        player.setPoints(player.getPoints() - 20);
    }


    /**
     * Returns player which takes cards in current turn.
     *
     * @param room The current game room.
     * @param startCard The card which was first put on table.
     * @return The player who takes cards in current turn.
     */
    private static Player getPlayerWhoTakesCards(Room room, Card startCard) {
        Card oldestCard = getLeadingCard(room.getCardsOnTable().values(), startCard);
        PlayerInfo playerInfo = getPlayerInfoByCard(room.getCardsOnTable(), oldestCard);
        return Player.getPlayerById(room.getPlayers(), playerInfo.getId());
    }

    /**
     * Gets the leading card from the cards on the table.
     *
     * @param cards      The cards on the table.
     * @param startCard  The start card for this turn.
     * @return The leading card.
     */
    public static Card getLeadingCard(Collection<Card> cards, Card startCard) {
        List<Card> cardsInStartColor = getCardsInColor(cards, startCard.getSuit());
        return getTheOldestCard(cardsInStartColor);
    }

    /**
     * Gets the cards of a specific color from the given collection.
     *
     * @param cardsOnTable The cards on the table.
     * @param suit         The color to filter for.
     * @return A list of cards in the specified color.
     */
    private static List<Card> getCardsInColor(Collection<Card> cardsOnTable, Suit suit) {
        return cardsOnTable.stream().filter(card -> card.getSuit() == suit).collect(Collectors.toList());
    }

    /**
     * Gets the oldest card from the given collection based on rank.
     *
     * @param cardsOnTable The cards on the table.
     * @return The oldest card.
     */
    private static Card getTheOldestCard(Collection<Card> cardsOnTable) {
        return cardsOnTable.stream()
                .max(Comparator.comparing(Card::getRank))
                .orElse(null);
    }

    /**
     * Gets the player info associated with a specific card on the table.
     *
     * @param cardsOnTable The cards on the table.
     * @param targetCard   The card to search for.
     * @return The player info associated with the target card.
     */
    private static PlayerInfo getPlayerInfoByCard(Map<PlayerInfo, Card> cardsOnTable, Card targetCard) {
        return cardsOnTable.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().equals(targetCard))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the start card for the current turn.
     *
     * @param room The current game room.
     * @return The start card for this turn.
     */
    private static Card getStartCard(Room room) {
        PlayerInfo playerInfo = PlayerInfo.getPlayerInfoById(room.getCardsOnTable().keySet(), room.getStartTurn());
        return room.getCardsOnTable().get(playerInfo);
    }

    /**
     * Checks if the table is empty for the current turn.
     *
     * @param move The move made by a player.
     * @param room The current game room.
     * @return True if the table is empty, false otherwise.
     */
    private static boolean isTableEmpty(Move move, Room room) {
        return room.getStartTurn() == move.getPlayer().getId();
    }

    /**
     * Checks if a player has a card in a specific color.
     *
     * @param player The player to check.
     * @param color  The color to check for.
     * @return True if the player has a card in the specified color, false otherwise.
     */
    public static boolean hasCardInColor(Player player, Suit color) {
        return player.getCards().stream().anyMatch(card -> card.getSuit() == color);
    }

    /**
     * Checks if two cards have the same color.
     *
     * @param card1 The first card.
     * @param card2 The second card.
     * @return True if both cards have the same color, false otherwise.
     */
    public static boolean isSameColor(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

}
