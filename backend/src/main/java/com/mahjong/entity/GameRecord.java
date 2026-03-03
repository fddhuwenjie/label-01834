package com.mahjong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game_record")
public class GameRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String result;
    private Integer rounds;
    private Integer score;
    private Integer duration;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
