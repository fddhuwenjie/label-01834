package com.mahjong.service;

import com.mahjong.common.BusinessException;
import com.mahjong.entity.User;
import com.mahjong.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Map<String, Object> login(String nickname, String password) {
        User user = userMapper.findByNickname(nickname);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtService.generateToken(user.getId());
        log.info("用户登录成功: {}", nickname);
        return Map.of("token", token, "userId", user.getId(), "nickname", user.getNickname());
    }

    public Map<String, Object> register(String nickname, String password) {
        if (userMapper.findByNickname(nickname) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatar("");
        user.setTotalGames(0);
        user.setWinGames(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        String token = jwtService.generateToken(user.getId());
        log.info("新用户注册: {}", nickname);
        return Map.of("token", token, "userId", user.getId(), "nickname", user.getNickname());
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    public void updateGameStats(Long userId, boolean isWin) {
        User user = getUserById(userId);
        user.setTotalGames(user.getTotalGames() + 1);
        if (isWin) user.setWinGames(user.getWinGames() + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
