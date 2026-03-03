package com.mahjong.config;

import com.mahjong.entity.User;
import com.mahjong.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时初始化测试账号，确保 BCrypt 哈希与运行时编码器一致
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createUserIfNotExists("admin", "admin123");
        createUserIfNotExists("user", "user123");
        log.info("测试账号初始化完成");
    }

    private void createUserIfNotExists(String nickname, String password) {
        User existing = userMapper.findByNickname(nickname);
        if (existing != null) {
            if (!passwordEncoder.matches(password, existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode(password));
                existing.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(existing);
                log.info("已校正测试账号密码: {}", nickname);
            }
            return;
        }
        User u = new User();
        u.setNickname(nickname);
        u.setPassword(passwordEncoder.encode(password));
        u.setAvatar("");
        u.setTotalGames(0);
        u.setWinGames(0);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);
        log.info("已创建测试账号: {}", nickname);
    }
}
