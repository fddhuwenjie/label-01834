package com.mahjong.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ActionRequest {
    @NotBlank(message = "操作类型不能为空")
    private String action;
    private String tile;
    private List<String> tiles;
}
