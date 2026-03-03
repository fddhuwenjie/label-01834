package com.mahjong.controller;

import com.mahjong.common.Result;
import com.mahjong.service.SettingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public Result<Map<String, String>> getSettings(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(settingService.getSettings(userId));
    }

    @PutMapping
    public Result<Void> updateSetting(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        body.forEach((key, value) -> settingService.updateSetting(userId, key, value));
        return Result.success("设置已更新", null);
    }
}
