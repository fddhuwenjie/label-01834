package com.mahjong.controller;

import com.mahjong.common.Result;
import com.mahjong.dto.LoginRequest;
import com.mahjong.entity.User;
import com.mahjong.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request.getNickname(), request.getPassword()));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody LoginRequest request) {
        return Result.success("注册成功", userService.register(request.getNickname(), request.getPassword()));
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }
}
