package com.mahjong.service.engine;

import com.mahjong.common.BusinessException;
import com.mahjong.dto.GameStateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 麻将核心引擎 — 管理游戏会话、处理出牌/吃碰杠胡逻辑、驱动 AI 回合
 */
@Component
public class MahjongEngine {

    private static final Logger log = LoggerFactory.getLogger(MahjongEngine.class);
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession createGame(Long userId) {
        String gameId = UUID.randomUUID().toString().substring(0, 8);
        GameSession session = GameSession.create(gameId, userId);

        List<String> deck = TileUtils.createFullDeck();
        TileUtils.shuffle(deck);
        session.setWall(deck);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                session.getHands().get(i).add(session.getWall().remove(0));
            }
            TileUtils.sortHand(session.getHands().get(i));
        }

        String drawn = session.getWall().remove(0);
        session.getHands().get(0).add(drawn);
        session.setDrawnTile(drawn);
        session.setCurrentPlayer(0);
        session.setPhase("discard");
        TileUtils.sortHand(session.getHands().get(0));

        sessions.put(gameId, session);
        log.info("新游戏创建: gameId={}, userId={}", gameId, userId);
        return session;
    }

    public GameSession getSession(String gameId) {
        GameSession s = sessions.get(gameId);
        if (s == null) throw new BusinessException("游戏不存在或已结束");
        return s;
    }

    /**
     * 玩家出牌 → AI自动轮转直到重新轮到玩家或游戏结束
     */
    public GameSession playerDiscard(String gameId, String tile) {
        GameSession s = getSession(gameId);
        if (s.getCurrentPlayer() != 0) throw new BusinessException("当前不是你的回合");
        if (!"discard".equals(s.getPhase())) throw new BusinessException("当前阶段不能出牌");

        List<String> hand = s.getHands().get(0);
        if (!hand.remove(tile)) throw new BusinessException("手中没有这张牌");

        s.getDiscards().get(0).add(tile);
        s.setLastDiscard(tile);
        s.setLastDiscardPlayer(0);
        s.setDrawnTile(null);
        s.setRoundCount(s.getRoundCount() + 1);

        if (checkOtherPlayersReaction(s, tile, 0)) return s;

        advanceToNextPlayer(s, 1);
        runAiTurns(s);

        return s;
    }

    /**
     * 玩家执行操作: chi/pong/kong/win/pass
     */
    public GameSession playerAction(String gameId, String action, String tile, List<String> tiles) {
        GameSession s = getSession(gameId);
        if (s.getCurrentPlayer() != 0 && !"pong".equals(action) && !"kong".equals(action) && !"win".equals(action)) {
            throw new BusinessException("当前不能执行此操作");
        }

        List<String> hand = s.getHands().get(0);

        switch (action) {
            case "pong" -> {
                if (s.getLastDiscard() == null) throw new BusinessException("没有可碰的牌");
                String t = s.getLastDiscard();
                if (!TileUtils.canPong(hand, t)) throw new BusinessException("无法碰这张牌");
                hand.remove(t);
                hand.remove(t);
                s.getDiscards().get(s.getLastDiscardPlayer()).remove(s.getDiscards().get(s.getLastDiscardPlayer()).size() - 1);
                s.getMelds().get(0).add(List.of(t, t, t));
                s.setLastDiscard(null);
                s.setCurrentPlayer(0);
                s.setPhase("discard");
                TileUtils.sortHand(hand);
                log.info("玩家碰牌: {}", TileUtils.getDisplayName(t));
            }
            case "kong" -> {
                if (s.getLastDiscard() != null && TileUtils.canKong(hand, s.getLastDiscard())) {
                    String t = s.getLastDiscard();
                    for (int i = 0; i < 3; i++) hand.remove(t);
                    s.getDiscards().get(s.getLastDiscardPlayer()).remove(s.getDiscards().get(s.getLastDiscardPlayer()).size() - 1);
                    s.getMelds().get(0).add(List.of(t, t, t, t));
                    s.setLastDiscard(null);
                    drawForPlayer(s, 0);
                    TileUtils.sortHand(hand);
                    log.info("玩家明杠: {}", TileUtils.getDisplayName(t));
                } else if (tile != null) {
                    long count = hand.stream().filter(t2 -> t2.equals(tile)).count();
                    if (count < 4) throw new BusinessException("无法暗杠这张牌");
                    for (int i = 0; i < 4; i++) hand.remove(tile);
                    s.getMelds().get(0).add(List.of(tile, tile, tile, tile));
                    drawForPlayer(s, 0);
                    TileUtils.sortHand(hand);
                    log.info("玩家暗杠: {}", TileUtils.getDisplayName(tile));
                } else {
                    throw new BusinessException("无法杠");
                }
            }
            case "chi" -> {
                if (s.getLastDiscardPlayer() != 3) throw new BusinessException("只能吃上家的牌");
                if (tiles == null || tiles.size() != 2) throw new BusinessException("吃牌需要指定两张手牌");
                String t = s.getLastDiscard();
                for (String ct : tiles) {
                    if (!hand.contains(ct)) throw new BusinessException("手中没有这张牌: " + ct);
                }
                for (String ct : tiles) hand.remove(ct);
                s.getDiscards().get(3).remove(s.getDiscards().get(3).size() - 1);
                List<String> meld = new ArrayList<>(tiles);
                meld.add(t);
                meld.sort(Comparator.comparingInt(TileUtils::getValue));
                s.getMelds().get(0).add(meld);
                s.setLastDiscard(null);
                s.setCurrentPlayer(0);
                s.setPhase("discard");
                TileUtils.sortHand(hand);
                log.info("玩家吃牌: {}", meld.stream().map(TileUtils::getDisplayName).toList());
            }
            case "win" -> {
                List<String> winHand = new ArrayList<>(hand);
                if (s.getLastDiscard() != null) {
                    winHand.add(s.getLastDiscard());
                }
                if (winHand.size() == 14 && TileUtils.isWinningHand(winHand)) {
                    s.setWinner(0);
                    s.setPhase("end");
                    log.info("玩家胡牌！");
                } else if (hand.size() == 14 && TileUtils.isWinningHand(hand)) {
                    s.setWinner(0);
                    s.setPhase("end");
                    log.info("玩家自摸胡牌！");
                } else {
                    throw new BusinessException("当前手牌无法胡牌");
                }
            }
            case "pass" -> {
                s.setLastDiscard(null);
                int next = (s.getLastDiscardPlayer() + 1) % 4;
                advanceToNextPlayer(s, next);
                if (s.getCurrentPlayer() != 0) {
                    runAiTurns(s);
                }
            }
            default -> throw new BusinessException("未知操作: " + action);
        }

        return s;
    }

    private boolean checkOtherPlayersReaction(GameSession s, String tile, int discarder) {
        for (int i = 1; i <= 3; i++) {
            int seat = (discarder + i) % 4;
            if (seat == 0) {
                if (TileUtils.canWinWith(s.getHands().get(0), tile)) {
                    s.setPhase("action");
                    s.setCurrentPlayer(0);
                    return true;
                }
                if (TileUtils.canPong(s.getHands().get(0), tile) || TileUtils.canKong(s.getHands().get(0), tile)) {
                    s.setPhase("action");
                    s.setCurrentPlayer(0);
                    return true;
                }
                if (discarder == 3) {
                    List<List<String>> chiOptions = TileUtils.findChi(s.getHands().get(0), tile);
                    if (!chiOptions.isEmpty()) {
                        s.setPhase("action");
                        s.setCurrentPlayer(0);
                        return true;
                    }
                }
                continue;
            }

            List<String> aiHand = s.getHands().get(seat);
            if (TileUtils.canWinWith(aiHand, tile)) {
                List<String> winHand = new ArrayList<>(aiHand);
                winHand.add(tile);
                s.getDiscards().get(discarder).remove(s.getDiscards().get(discarder).size() - 1);
                s.getHands().set(seat, winHand);
                s.setWinner(seat);
                s.setPhase("end");
                log.info("{}胡牌", GameSession.PLAYER_NAMES[seat]);
                return true;
            }

            if (TileUtils.canPong(aiHand, tile) && new Random().nextDouble() > 0.4) {
                aiHand.remove(tile);
                aiHand.remove(tile);
                s.getDiscards().get(discarder).remove(s.getDiscards().get(discarder).size() - 1);
                s.getMelds().get(seat).add(List.of(tile, tile, tile));
                s.setLastDiscard(null);
                s.setCurrentPlayer(seat);
                String aiDiscard = aiChooseDiscard(aiHand);
                aiHand.remove(aiDiscard);
                s.getDiscards().get(seat).add(aiDiscard);
                s.setLastDiscard(aiDiscard);
                s.setLastDiscardPlayer(seat);
                TileUtils.sortHand(aiHand);
                log.info("{}碰牌: {}", GameSession.PLAYER_NAMES[seat], TileUtils.getDisplayName(tile));

                if (checkOtherPlayersReaction(s, aiDiscard, seat)) return true;
                advanceToNextPlayer(s, (seat + 1) % 4);
                return false;
            }
        }
        return false;
    }

    private void runAiTurns(GameSession s) {
        int maxRounds = 12;
        int count = 0;
        while (s.getCurrentPlayer() != 0 && !"end".equals(s.getPhase()) && count < maxRounds) {
            count++;
            int seat = s.getCurrentPlayer();
            List<String> aiHand = s.getHands().get(seat);

            if (s.getWall().isEmpty()) {
                s.setPhase("end");
                s.setWinner(-1);
                log.info("牌墙已空，流局");
                return;
            }

            String drawn = s.getWall().remove(0);
            aiHand.add(drawn);
            TileUtils.sortHand(aiHand);

            if (TileUtils.isWinningHand(aiHand)) {
                s.setWinner(seat);
                s.setPhase("end");
                log.info("{}自摸胡牌", GameSession.PLAYER_NAMES[seat]);
                return;
            }

            String discard = aiChooseDiscard(aiHand);
            aiHand.remove(discard);
            s.getDiscards().get(seat).add(discard);
            s.setLastDiscard(discard);
            s.setLastDiscardPlayer(seat);
            TileUtils.sortHand(aiHand);
            s.setRoundCount(s.getRoundCount() + 1);

            if (checkOtherPlayersReaction(s, discard, seat)) return;
            advanceToNextPlayer(s, (seat + 1) % 4);
        }

        if (s.getCurrentPlayer() == 0 && !"end".equals(s.getPhase())) {
            if (s.getWall().isEmpty()) {
                s.setPhase("end");
                s.setWinner(-1);
                return;
            }
            drawForPlayer(s, 0);
        }
    }

    private void drawForPlayer(GameSession s, int seat) {
        if (s.getWall().isEmpty()) {
            s.setPhase("end");
            s.setWinner(-1);
            return;
        }
        String drawn = s.getWall().remove(0);
        s.getHands().get(seat).add(drawn);
        TileUtils.sortHand(s.getHands().get(seat));
        if (seat == 0) {
            s.setDrawnTile(drawn);
            if (TileUtils.isWinningHand(s.getHands().get(0))) {
                s.setPhase("action");
            } else {
                s.setPhase("discard");
            }
            s.setCurrentPlayer(0);
        }
    }

    private void advanceToNextPlayer(GameSession s, int next) {
        s.setCurrentPlayer(next);
        s.setPhase(next == 0 ? "draw" : "ai_turn");
    }

    /**
     * AI 出牌策略: 优先丢孤张字牌 → 孤张边张 → 随机
     */
    private String aiChooseDiscard(List<String> hand) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String t : hand) counts.merge(t, 1L, Long::sum);

        for (String t : hand) {
            if (!TileUtils.isNumberTile(t) && counts.get(t) == 1) return t;
        }
        for (String t : hand) {
            if (TileUtils.isNumberTile(t) && counts.get(t) == 1) {
                int v = TileUtils.getValue(t);
                if (v == 1 || v == 9) return t;
            }
        }
        for (String t : hand) {
            if (counts.get(t) == 1) return t;
        }
        return hand.get(hand.size() - 1);
    }

    public GameStateDTO toDTO(GameSession s) {
        GameStateDTO dto = new GameStateDTO();
        dto.setGameId(s.getId());
        dto.setPlayerHand(new ArrayList<>(s.getHands().get(0)));
        dto.setPlayerMelds(new ArrayList<>(s.getMelds().get(0)));
        dto.setPlayerDiscards(new ArrayList<>(s.getDiscards().get(0)));
        dto.setDrawnTile(s.getDrawnTile());
        dto.setCurrentPlayer(s.getCurrentPlayer());
        dto.setPhase(s.getPhase());
        dto.setLastDiscard(s.getLastDiscard());
        dto.setLastDiscardPlayer(s.getLastDiscardPlayer());
        dto.setWallRemaining(s.getWall() != null ? s.getWall().size() : 0);
        dto.setWinner(s.getWinner());
        if (s.getWinner() >= 0) {
            dto.setWinnerName(GameSession.PLAYER_NAMES[s.getWinner()]);
        }

        List<String> actions = new ArrayList<>();
        if (s.getCurrentPlayer() == 0) {
            List<String> hand = s.getHands().get(0);
            if ("action".equals(s.getPhase())) {
                if (s.getLastDiscard() != null) {
                    if (TileUtils.canWinWith(hand, s.getLastDiscard())) actions.add("win");
                    if (TileUtils.canKong(hand, s.getLastDiscard())) actions.add("kong");
                    if (TileUtils.canPong(hand, s.getLastDiscard())) actions.add("pong");
                    if (s.getLastDiscardPlayer() == 3) {
                        if (!TileUtils.findChi(hand, s.getLastDiscard()).isEmpty()) actions.add("chi");
                    }
                }
                if (hand.size() == 14 && TileUtils.isWinningHand(hand)) actions.add("win");
                actions.add("pass");
            }
            if ("discard".equals(s.getPhase())) {
                List<String> selfKongs = TileUtils.findSelfKong(hand);
                if (!selfKongs.isEmpty()) actions.add("selfkong");
                if (TileUtils.isWinningHand(hand)) actions.add("win");
            }
        }
        dto.setAvailableActions(actions);

        List<GameStateDTO.AiPlayerInfo> aiPlayers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            GameStateDTO.AiPlayerInfo ai = new GameStateDTO.AiPlayerInfo();
            ai.setSeat(i);
            ai.setName(GameSession.PLAYER_NAMES[i]);
            ai.setTileCount(s.getHands().get(i).size());
            ai.setMelds(new ArrayList<>(s.getMelds().get(i)));
            ai.setDiscards(new ArrayList<>(s.getDiscards().get(i)));
            aiPlayers.add(ai);
        }
        dto.setAiPlayers(aiPlayers);

        return dto;
    }

    public void removeSession(String gameId) {
        sessions.remove(gameId);
    }
}
