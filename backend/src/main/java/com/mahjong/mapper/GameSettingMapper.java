package com.mahjong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mahjong.entity.GameSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GameSettingMapper extends BaseMapper<GameSetting> {
}
