package com.mahjong.controller;

import com.mahjong.common.Result;
import com.mahjong.dto.ActionRequest;
import com.mahjong.dto.GameStateDTO;
import com.mahjong.entity.GameRecord;
import com.mahjong.service.GameService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public Result<GameStateDTO> startGame(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success("游戏开始", gameService.startNewGame(userId));
    }

    @PostMapping("/{gameId}/discard")
    public Result<GameStateDTO> discard(@PathVariable String gameId,
                                        @RequestParam String tile,
                                        Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(gameService.discard(gameId, tile, userId));
    }

    @PostMapping("/{gameId}/action")
    public Result<GameStateDTO> action(@PathVariable String gameId,
                                       @Valid @RequestBody ActionRequest request,
                                       Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(gameService.action(gameId, request.getAction(),
                request.getTile(), request.getTiles(), userId));
    }

    @GetMapping("/{gameId}/state")
    public Result<GameStateDTO> getState(@PathVariable String gameId) {
        return Result.success(gameService.getState(gameId));
    }

    @GetMapping("/history")
    public Result<List<GameRecord>> getHistory(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(gameService.getHistory(userId));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(gameService.getStats(userId));
    }
}
