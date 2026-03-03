package com.mahjong.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GameStateDTO {
    private String gameId;
    private List<String> playerHand;
    private List<List<String>> playerMelds;
    private List<String> playerDiscards;
    private String drawnTile;
    private int currentPlayer;
    private String phase;
    private List<String> availableActions;
    private String lastDiscard;
    private int lastDiscardPlayer;
    private int wallRemaining;
    private int winner;
    private String winnerName;
    private List<AiPlayerInfo> aiPlayers;

    @Data
    public static class AiPlayerInfo {
        private int seat;
        private String name;
        private int tileCount;
        private List<List<String>> melds;
        private List<String> discards;
    }
}
