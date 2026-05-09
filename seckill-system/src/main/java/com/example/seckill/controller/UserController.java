package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.entity.User;
import com.example.seckill.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户接口 (Phase 11: 订单管理)
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<User> register(@RequestParam String username,
            @RequestParam String password) {
        return Result.success(userService.register(username, password));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<User> login(@RequestParam String username,
            @RequestParam String password) {
        return Result.success(userService.login(username, password));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    @GetMapping
    @Operation(summary = "获取所有用户")
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.findAll());
    }
}
