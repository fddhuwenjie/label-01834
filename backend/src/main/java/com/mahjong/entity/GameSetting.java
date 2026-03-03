package com.mahjong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("game_setting")
public class GameSetting {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String settingKey;
    private String settingValue;
}
