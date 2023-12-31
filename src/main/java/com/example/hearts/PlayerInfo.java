package com.example.hearts;

import java.io.Serializable;
import java.util.*;

public class PlayerInfo implements Serializable {

    private int id;
    private String name;
    private int points;

    public PlayerInfo(int id, String name, int points) {
        this.id = id;
        this.name = name;
        this.points = points;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo that = (PlayerInfo) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static PlayerInfo findPlayerWithLowestId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .min(Comparator.comparingInt(PlayerInfo::getId))
                .orElse(null);
    }

    public static PlayerInfo findPlayerWithMiddleId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .sorted(Comparator.comparingInt(PlayerInfo::getId))
                .skip(1) // Zawsze pomijamy drugi element, bo zawsze są cztery elementy
                .findFirst()
                .orElse(null);
    }

    public static PlayerInfo findPlayerWithHighestId(int excludedId, Map<PlayerInfo, Card> cardsOnTable) {
        return cardsOnTable.keySet().stream()
                .filter(playerInfo -> playerInfo.getId() != excludedId)
                .max(Comparator.comparingInt(PlayerInfo::getId))
                .orElse(null);
    }

    public static PlayerInfo getPlayerInfo(GameState gameState) {
        PlayerInfo foundPlayer = null;
        Set<PlayerInfo> playerInfos = gameState.getCardsOnTable().keySet();
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo.getId() == gameState.getPlayer().getId()) {
                foundPlayer = playerInfo;
                break;
            }
        }
        return foundPlayer;
    }

    public static PlayerInfo getPlayerInfoById(Set <PlayerInfo> playerInfoList, int id){
        for (PlayerInfo playerInfo: playerInfoList){
            if(playerInfo.id == id)
                return playerInfo;
        }
        return null;
    }
}
