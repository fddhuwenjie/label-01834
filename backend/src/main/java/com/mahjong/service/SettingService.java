package com.mahjong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mahjong.entity.GameSetting;
import com.mahjong.mapper.GameSettingMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingService {

    private final GameSettingMapper settingMapper;

    private static final Map<String, String> DEFAULTS = Map.of(
        "ai_speed", "normal",
        "sound_enabled", "true",
        "auto_sort", "true",
        "theme", "classic"
    );

    public SettingService(GameSettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    public Map<String, String> getSettings(Long userId) {
        List<GameSetting> list = settingMapper.selectList(
            new LambdaQueryWrapper<GameSetting>().eq(GameSetting::getUserId, userId)
        );
        Map<String, String> result = new HashMap<>(DEFAULTS);
        for (GameSetting s : list) {
            result.put(s.getSettingKey(), s.getSettingValue());
        }
        return result;
    }

    public void updateSetting(Long userId, String key, String value) {
        GameSetting existing = settingMapper.selectOne(
            new LambdaQueryWrapper<GameSetting>()
                .eq(GameSetting::getUserId, userId)
                .eq(GameSetting::getSettingKey, key)
        );
        if (existing != null) {
            existing.setSettingValue(value);
            settingMapper.updateById(existing);
        } else {
            GameSetting s = new GameSetting();
            s.setUserId(userId);
            s.setSettingKey(key);
            s.setSettingValue(value);
            settingMapper.insert(s);
        }
    }
}
