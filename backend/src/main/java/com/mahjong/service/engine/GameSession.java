package com.mahjong.service.engine;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 单局游戏会话状态
 * 0号位 = 玩家, 1~3号位 = AI (AI-东/AI-南/AI-西)
 */
@Data
public class GameSession {
    private String id;
    private Long userId;
    private List<String> wall;
    private List<List<String>> hands;
    private List<List<String>> discards;
    private List<List<List<String>>> melds;
    private int currentPlayer;
    private int dealerSeat;
    private String lastDiscard;
    private int lastDiscardPlayer = -1;
    private String phase;
    private int winner = -1;
    private String drawnTile;
    private LocalDateTime createTime;
    private int roundCount;

    public static GameSession create(String id, Long userId) {
        GameSession s = new GameSession();
        s.id = id;
        s.userId = userId;
        s.hands = new ArrayList<>();
        s.discards = new ArrayList<>();
        s.melds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            s.hands.add(new ArrayList<>());
            s.discards.add(new ArrayList<>());
            s.melds.add(new ArrayList<>());
        }
        s.phase = "dealing";
        s.createTime = LocalDateTime.now();
        s.dealerSeat = 0;
        s.roundCount = 0;
        return s;
    }

    public static final String[] PLAYER_NAMES = {"玩家", "AI-东", "AI-南", "AI-西"};
}
