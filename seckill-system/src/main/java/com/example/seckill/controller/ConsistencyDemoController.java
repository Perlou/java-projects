package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.entity.Product;
import com.example.seckill.service.ConsistencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 一致性模型演示控制器
 * Phase 17: 分布式存储架构 - 一致性级别对比
 */
@RestController
@RequestMapping("/api/demo/consistency")
@Tag(name = "一致性模型演示", description = "Phase 17 - 不同一致性级别对比演示")
public class ConsistencyDemoController {

    private final ConsistencyService consistencyService;

    public ConsistencyDemoController(ConsistencyService consistencyService) {
        this.consistencyService = consistencyService;
    }

    /**
     * 强一致性读取
     */
    @GetMapping("/strong")
    @Operation(summary = "强一致性读取", description = "直接读取数据库，始终返回最新数据。适用于金融交易、库存扣减等场景。")
    public Result<Product> readStrong(
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        Product product = consistencyService.readStrong(productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success("强一致性读取成功", product);
    }

    /**
     * 最终一致性读取
     */
    @GetMapping("/eventual")
    @Operation(summary = "最终一致性读取", description = "优先读取缓存，可能返回短暂过期数据。适用于商品浏览、文章阅读等场景。")
    public Result<Product> readEventual(
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        Product product = consistencyService.readEventual(productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success("最终一致性读取成功", product);
    }

    /**
     * 读己之写 - 写入
     */
    @PostMapping("/write")
    @Operation(summary = "读己之写 - 写入", description = "写入数据后存入会话缓存，确保同一会话能立即读到。")
    public Result<String> writeWithSession(
            @Parameter(description = "会话ID（可选，自动生成）") @RequestParam(required = false) String sessionId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "新名称") @RequestParam String newName) {

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().substring(0, 8);
        }

        consistencyService.writeWithSession(sessionId, productId, newName);
        return Result.success("写入成功，sessionId: " + sessionId, sessionId);
    }

    /**
     * 读己之写 - 读取
     */
    @GetMapping("/read-your-writes")
    @Operation(summary = "读己之写 - 读取", description = "优先从会话缓存读取，确保能读到自己刚写入的数据。")
    public Result<Product> readYourWrites(
            @Parameter(description = "会话ID") @RequestParam String sessionId,
            @Parameter(description = "商品ID") @RequestParam Long productId) {

        Product product = consistencyService.readYourWrites(sessionId, productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success("读己之写读取成功", product);
    }

    /**
     * 获取一致性模型说明
     */
    @GetMapping("/models")
    @Operation(summary = "一致性模型说明", description = "获取各种一致性模型的对比信息")
    public Result<Map<String, Object>> getConsistencyModels() {
        return Result.success("一致性模型信息", consistencyService.getConsistencyModelsInfo());
    }

    /**
     * 一致性对比测试
     * 同时用不同一致性级别读取同一数据，观察差异
     */
    @GetMapping("/compare")
    @Operation(summary = "一致性对比测试", description = "使用不同一致性级别读取同一数据，对比结果")
    public Result<Map<String, Object>> compareConsistency(
            @Parameter(description = "商品ID") @RequestParam Long productId) {

        long startStrong = System.currentTimeMillis();
        Product strong = consistencyService.readStrong(productId);
        long durationStrong = System.currentTimeMillis() - startStrong;

        long startEventual = System.currentTimeMillis();
        Product eventual = consistencyService.readEventual(productId);
        long durationEventual = System.currentTimeMillis() - startEventual;

        Map<String, Object> result = Map.of(
                "strongConsistency", Map.of(
                        "data", strong != null ? strong : "NOT_FOUND",
                        "durationMs", durationStrong,
                        "source", "Database"),
                "eventualConsistency", Map.of(
                        "data", eventual != null ? eventual : "NOT_FOUND",
                        "durationMs", durationEventual,
                        "source", "Cache/Database"),
                "analysis", Map.of(
                        "strongFaster", durationStrong < durationEventual,
                        "performanceGap", Math.abs(durationStrong - durationEventual) + "ms",
                        "note", "首次请求缓存未命中，后续请求最终一致性会更快"));

        return Result.success("一致性对比结果", result);
    }
}
