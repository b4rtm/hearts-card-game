package com.example.hearts.server;

import com.example.hearts.*;

import java.util.*;
import java.util.stream.Collectors;

public class HeartsRules {


    public static boolean isMoveValid(Move move, Room room){

        if(isTableEmpty(move, room)) //todo kier
            return true;

        Card firstCardOnTable = getStartCard(room);

        if(isSameColor(firstCardOnTable, move.getCard())){
            System.out.println("1");
            return true;
        }
        else if(hasCardInColor(move.getPlayer(),firstCardOnTable.getSuit())){
            System.out.println("2");
            return false;
        }
        else{
            System.out.println("3");
            return true;
        }

    }

    public static void setPointsToPlayersAfterTurn(Room room){


        Card startCard = getStartCard(room);

        if(room.getDealNumber() == 1){
            List<Card> cardsInStartColor = getCardsInColor(room.getCardsOnTable().values(), startCard.getSuit());
            Card oldestCard = getTheOldestCard(cardsInStartColor);
            PlayerInfo playerInfo = getPlayerInfoByCard(room.getCardsOnTable(), oldestCard);
            Player player = Player.getPlayerById(room.getPlayers(), playerInfo.getId());
            player.setPoints(player.getPoints() - 20);

        }
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
