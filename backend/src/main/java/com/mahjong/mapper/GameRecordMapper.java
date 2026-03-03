package com.mahjong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mahjong.entity.GameRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {
    Map<String, Object> getStatsByUserId(Long userId);
}
