package com.example.hearts;

import com.example.hearts.server.DeckInitializer;
import com.example.hearts.server.HeartsRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HeartsGameTest {

    private Room room =  new Room(1);

    @BeforeEach
    public void initializeRoom(){
        Player player1 = new Player(1);
        Player player2 = new Player(2);
        Player player3 = new Player(3);
        Player player4 = new Player(4);

        room.getPlayers().add(player1);
        room.getPlayers().add(player2);
        room.getPlayers().add(player3);
        room.getPlayers().add(player4);
    }


    @Test
    public void testLeadingCard() {
        Card card1 = new Card(Rank.EIGHT, Suit.DIAMONDS);
        Card card2 = new Card(Rank.JACK, Suit.DIAMONDS);
        Card card3 = new Card(Rank.ACE, Suit.DIAMONDS);
        Card card4 = new Card(Rank.TWO, Suit.DIAMONDS);

        List<Card> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
        cards.add(card4);

        assertEquals(card3, HeartsRules.getLeadingCard(cards, card2));
    }

    @Test
    public void testDeal1(){
        room.setStartTurn(1);
        room.setDealNumber(1);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.TWO, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-20, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal2(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setDealNumber(2);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.TWO, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-20, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal3(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setDealNumber(3);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-60, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal4(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setDealNumber(4);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.KING, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.KING, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-90, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal5(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setDealNumber(5);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.KING, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-150, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal6(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setTurnNumber(7);
        room.setDealNumber(6);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-75, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testSecondDeal6(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setTurnNumber(8);
        room.setDealNumber(6);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(0, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testThirdDeal6(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setTurnNumber(13);
        room.setDealNumber(6);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.JACK, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-75, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }

    @Test
    public void testDeal7(){
        room.getPlayers().forEach(player -> player.setPoints(0));

        room.setStartTurn(1);
        room.setTurnNumber(13);
        room.setDealNumber(7);

        room.getCardsOnTable().put(new PlayerInfo(1),  new Card(Rank.EIGHT, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(2),  new Card(Rank.KING, Suit.HEARTS));
        room.getCardsOnTable().put(new PlayerInfo(3),  new Card(Rank.ACE, Suit.DIAMONDS));
        room.getCardsOnTable().put(new PlayerInfo(4),  new Card(Rank.QUEEN, Suit.DIAMONDS));

        HeartsRules.setPointsToPlayersAfterTurn(room);

        assertEquals(-355, Player.getPlayerById(room.getPlayers(),3).getPoints());
    }
}
