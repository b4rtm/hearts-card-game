package com.example.hearts.server;

import com.example.hearts.*;

import java.util.*;
import java.util.stream.Collectors;

public class HeartsRules {


    public static boolean isMoveValid(Move move, Room room){

        if(isHeartsFirst(move, room))
            return false;

        if(isTableEmpty(move, room))
            return true;

        Card firstCardOnTable = getStartCard(room);

        if(isSameColor(firstCardOnTable, move.getCard())){
            return true;
        }
        else if(hasCardInColor(move.getPlayer(),firstCardOnTable.getSuit())){
            return false;
        }
        else{
            return true;
        }

    }

    private static boolean isHeartsFirst(Move move, Room room) {
        return isTableEmpty(move, room) && move.getCard().getSuit() == Suit.HEARTS && move.getPlayer().hasCardsOtherThanHearts() && (room.getDealNumber() == 2 || room.getDealNumber() == 5 || room.getDealNumber() == 7);
    }

    public static int setPointsToPlayersAfterTurn(Room room){


        Card startCard = getStartCard(room);
        Player player = getPlayerWhoTakesCards(room, startCard);
        if(room.getDealNumber() == 1){
            player.setPoints(player.getPoints() - 20);
        }
        else if(room.getDealNumber() == 2){
            for (Card card : room.getCardsOnTable().values()){
                if(card.getSuit() == Suit.HEARTS)
                    player.setPoints(player.getPoints() - 20);
            }
        }
        else if(room.getDealNumber() == 3){
            for (Card card : room.getCardsOnTable().values()){
                if(card.getRank() == Rank.QUEEN)
                    player.setPoints(player.getPoints() - 60);
            }
        }
        else if(room.getDealNumber() == 4){
            for (Card card : room.getCardsOnTable().values()){
                if(card.getRank() == Rank.KING || card.getRank() == Rank.JACK)
                    player.setPoints(player.getPoints() - 30);
            }
        }
        else if(room.getDealNumber() == 5){
            for (Card card : room.getCardsOnTable().values()){
                if(card.getRank() == Rank.KING && card.getSuit() == Suit.HEARTS)
                    player.setPoints(player.getPoints() - 150);
            }
        }
        else if(room.getDealNumber() == 6){

            if(room.getTurnNumber() == 7 || room.getTurnNumber() == 13)
                player.setPoints(player.getPoints() - 75);

        }
        else if(room.getDealNumber() == 7){
            player.setPoints(player.getPoints() - 20);

            for (Card card : room.getCardsOnTable().values()){
                if(card.getSuit() == Suit.HEARTS)
                    player.setPoints(player.getPoints() - 20);
                if(card.getRank() == Rank.QUEEN)
                    player.setPoints(player.getPoints() - 60);
                if(card.getRank() == Rank.KING || card.getRank() == Rank.JACK)
                    player.setPoints(player.getPoints() - 30);
                if(card.getRank() == Rank.KING && card.getSuit() == Suit.HEARTS)
                    player.setPoints(player.getPoints() - 150);
            }

            int turn = 13 - player.getCards().size();
            if(turn == 7 || turn == 13)
                player.setPoints(player.getPoints() - 75);
        }
        return player.getId();
    }

    private static Player getPlayerWhoTakesCards(Room room, Card startCard) {
        Card oldestCard = getLeadingCard(room.getCardsOnTable().values(), startCard);
        PlayerInfo playerInfo = getPlayerInfoByCard(room.getCardsOnTable(), oldestCard);
        Player player = Player.getPlayerById(room.getPlayers(), playerInfo.getId());
        return player;
    }

    public static Card getLeadingCard(Collection<Card> cards, Card startCard) {
        List<Card> cardsInStartColor = getCardsInColor(cards, startCard.getSuit());
        Card oldestCard = getTheOldestCard(cardsInStartColor);
        return oldestCard;
    }

    private static List<Card> getCardsInColor(Collection<Card> cardsOnTable, Suit suit) {
        return cardsOnTable.stream().filter(card -> card.getSuit() == suit).collect(Collectors.toList());
    }

    private static Card getTheOldestCard(Collection<Card> cardsOnTable) {
        return cardsOnTable.stream()
                .max(Comparator.comparing(Card::getRank))
                .orElse(null);
    }

    private static PlayerInfo getPlayerInfoByCard(Map<PlayerInfo, Card> cardsOnTable, Card targetCard) {
        return cardsOnTable.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().equals(targetCard))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static Card getStartCard(Room room) {
        PlayerInfo playerInfo = PlayerInfo.getPlayerInfoById(room.getCardsOnTable().keySet(), room.getStartTurn());
        return room.getCardsOnTable().get(playerInfo);
    }

    private static boolean isTableEmpty(Move move, Room room) {
        return room.getStartTurn() == move.getPlayer().getId();
    }

    public static boolean hasCardInColor(Player player, Suit color) {
        return player.getCards().stream().anyMatch(card -> card.getSuit() == color);
    }

    public static boolean isSameColor(Card card1, Card card2){
        return card1.getSuit() == card2.getSuit();
    }

}
