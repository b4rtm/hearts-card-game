package com.example.hearts;

import java.io.Serializable;

public class JoinRoomRequest implements Serializable {
    private int playerId;
    private int roomId;

    public JoinRoomRequest(int playerId, int roomId) {
        this.playerId = playerId;
        this.roomId = roomId;
    }

    public JoinRoomRequest(int playerId) {
        this.playerId = playerId;
    }


}
