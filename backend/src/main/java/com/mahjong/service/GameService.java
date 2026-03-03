package com.mahjong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mahjong.dto.GameStateDTO;
import com.mahjong.entity.GameRecord;
import com.mahjong.mapper.GameRecordMapper;
import com.mahjong.service.engine.GameSession;
import com.mahjong.service.engine.MahjongEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private final MahjongEngine engine;
    private final GameRecordMapper gameRecordMapper;
    private final UserService userService;

    public GameService(MahjongEngine engine, GameRecordMapper gameRecordMapper, UserService userService) {
        this.engine = engine;
        this.gameRecordMapper = gameRecordMapper;
        this.userService = userService;
    }

    public GameStateDTO startNewGame(Long userId) {
        GameSession session = engine.createGame(userId);
        return engine.toDTO(session);
    }

    public GameStateDTO discard(String gameId, String tile, Long userId) {
        GameSession session = engine.playerDiscard(gameId, tile);
        if ("end".equals(session.getPhase())) {
            saveGameRecord(session, userId);
        }
        return engine.toDTO(session);
    }

    public GameStateDTO action(String gameId, String action, String tile, List<String> tiles, Long userId) {
        GameSession session = engine.playerAction(gameId, action, tile, tiles);
        if ("end".equals(session.getPhase())) {
            saveGameRecord(session, userId);
        }
        return engine.toDTO(session);
    }

    public GameStateDTO getState(String gameId) {
        GameSession session = engine.getSession(gameId);
        return engine.toDTO(session);
    }

    public List<GameRecord> getHistory(Long userId) {
        return gameRecordMapper.selectList(
            new LambdaQueryWrapper<GameRecord>()
                .eq(GameRecord::getUserId, userId)
                .orderByDesc(GameRecord::getCreatedAt)
                .last("LIMIT 50")
        );
    }

    public Map<String, Object> getStats(Long userId) {
        return gameRecordMapper.getStatsByUserId(userId);
    }

    private void saveGameRecord(GameSession session, Long userId) {
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setRounds(session.getRoundCount());
        record.setCreatedAt(LocalDateTime.now());

        boolean playerWon = session.getWinner() == 0;
        boolean draw = session.getWinner() == -1;

        if (draw) {
            record.setResult("draw");
            record.setScore(0);
        } else if (playerWon) {
            record.setResult("win");
            record.setScore(10);
        } else {
            record.setResult("lose");
            record.setScore(-5);
        }

        int seconds = (int) Duration.between(session.getCreateTime(), LocalDateTime.now()).getSeconds();
        record.setDuration(seconds);
        gameRecordMapper.insert(record);
        userService.updateGameStats(userId, playerWon);
        engine.removeSession(session.getId());
        log.info("游戏记录已保存: userId={}, result={}", userId, record.getResult());
    }
}
