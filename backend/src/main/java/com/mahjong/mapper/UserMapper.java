package com.mahjong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mahjong.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByNickname(String nickname);
}
