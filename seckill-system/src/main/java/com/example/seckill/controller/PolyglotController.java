package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.service.PolyglotPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 多模存储控制器
 * Phase 17: 分布式存储架构 - Polyglot Persistence (多模存储选型)
 */
@RestController
@RequestMapping("/api/demo/polyglot")
@Tag(name = "多模存储演示", description = "Phase 17 - 根据数据特性选择存储引擎")
public class PolyglotController {

    private final PolyglotPersistenceService polyglotService;

    public PolyglotController(PolyglotPersistenceService polyglotService) {
        this.polyglotService = polyglotService;
    }

    @GetMapping("/recommendations")
    @Operation(summary = "所有存储推荐", description = "获取各类数据的存储引擎推荐")
    public Result<Map<String, PolyglotPersistenceService.StorageRecommendation>> getAllRecommendations() {
        return Result.success("存储推荐", polyglotService.getAllRecommendations());
    }

    @GetMapping("/recommendation")
    @Operation(summary = "单类数据存储推荐", description = "获取特定数据类型的存储推荐")
    public Result<PolyglotPersistenceService.StorageRecommendation> getRecommendation(
            @Parameter(description = "数据类型，如：用户数据、订单数据、购物车等") @RequestParam String dataType) {
        PolyglotPersistenceService.StorageRecommendation rec = polyglotService.getRecommendation(dataType);
        if (rec == null) {
            return Result.fail("未找到该数据类型的推荐: " + dataType);
        }
        return Result.success("存储推荐", rec);
    }

    @PostMapping("/analyze")
    @Operation(summary = "存储需求分析", description = "根据数据特征分析推荐存储方案")
    public Result<Map<String, Object>> analyzeStorageNeeds(
            @Parameter(description = "读写比例 (读/写)") @RequestParam(defaultValue = "1.0") double readWriteRatio,
            @Parameter(description = "是否需要 ACID 事务") @RequestParam(defaultValue = "false") boolean requiresACID,
            @Parameter(description = "数据量: small, medium, large") @RequestParam(defaultValue = "small") String dataVolume,
            @Parameter(description = "Schema 是否灵活") @RequestParam(defaultValue = "false") boolean flexibleSchema,
            @Parameter(description = "是否需要全文搜索") @RequestParam(defaultValue = "false") boolean requiresFullTextSearch,
            @Parameter(description = "是否是时序数据") @RequestParam(defaultValue = "false") boolean isTimeSeries) {

        PolyglotPersistenceService.DataCharacteristics chars = new PolyglotPersistenceService.DataCharacteristics();
        chars.readWriteRatio = readWriteRatio;
        chars.requiresACID = requiresACID;
        chars.dataVolume = dataVolume;
        chars.flexibleSchema = flexibleSchema;
        chars.requiresFullTextSearch = requiresFullTextSearch;
        chars.isTimeSeries = isTimeSeries;

        return Result.success("存储分析结果", polyglotService.analyzeStorageNeeds(chars));
    }

    @GetMapping("/seckill-architecture")
    @Operation(summary = "秒杀系统存储架构", description = "获取秒杀系统的多模存储架构说明")
    public Result<Map<String, Object>> getSeckillArchitecture() {
        return Result.success("秒杀系统存储架构",
                polyglotService.getSeckillSystemArchitecture());
    }
}
