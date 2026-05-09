package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.entity.SeckillGoods;
import com.example.seckill.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀接口
 */
@RestController
@RequestMapping("/api/seckill")
@Tag(name = "秒杀接口", description = "秒杀相关操作")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /**
     * 获取秒杀商品列表
     */
    @GetMapping("/goods")
    @Operation(summary = "获取秒杀商品列表")
    public Result<List<SeckillGoods>> listGoods() {
        List<SeckillGoods> goods = seckillService.listSeckillGoods();
        return Result.success(goods);
    }

    /**
     * 获取秒杀商品详情
     */
    @GetMapping("/goods/{goodsId}")
    @Operation(summary = "获取秒杀商品详情")
    public Result<SeckillGoods> getGoods(
            @Parameter(description = "商品ID") @PathVariable Long goodsId) {
        SeckillGoods goods = seckillService.getSeckillGoods(goodsId);
        return Result.success(goods);
    }

    /**
     * 执行秒杀
     */
    @PostMapping("/do")
    @Operation(summary = "执行秒杀")
    public Result<String> doSeckill(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long goodsId) {
        return seckillService.doSeckill(userId, goodsId);
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result")
    @Operation(summary = "查询秒杀结果")
    public Result<Object> getSeckillResult(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long goodsId) {
        return seckillService.getSeckillResult(userId, goodsId);
    }

    /**
     * 重置秒杀（测试用）
     */
    @PostMapping("/reset/{goodsId}")
    @Operation(summary = "重置秒杀（测试用）")
    public Result<String> resetSeckill(
            @Parameter(description = "商品ID") @PathVariable Long goodsId) {
        seckillService.resetSeckill(goodsId);
        return Result.success("重置成功");
    }

    // ========== 管理接口 ==========

    /**
     * 创建秒杀商品
     */
    @PostMapping("/goods")
    @Operation(summary = "创建秒杀商品")
    public Result<SeckillGoods> createGoods(@RequestBody SeckillGoods goods) {
        SeckillGoods created = seckillService.createSeckillGoods(goods);
        return Result.success(created);
    }

    /**
     * 更新秒杀商品
     */
    @PutMapping("/goods/{goodsId}")
    @Operation(summary = "更新秒杀商品")
    public Result<SeckillGoods> updateGoods(
            @PathVariable Long goodsId,
            @RequestBody SeckillGoods goods) {
        goods.setId(goodsId);
        SeckillGoods updated = seckillService.updateSeckillGoods(goods);
        return Result.success(updated);
    }

    /**
     * 删除秒杀商品
     */
    @DeleteMapping("/goods/{goodsId}")
    @Operation(summary = "删除秒杀商品")
    public Result<String> deleteGoods(@PathVariable Long goodsId) {
        seckillService.deleteSeckillGoods(goodsId);
        return Result.success("删除成功");
    }

    /**
     * 获取秒杀订单列表
     */
    @GetMapping("/orders")
    @Operation(summary = "获取秒杀订单列表")
    public Result<List<com.example.seckill.entity.SeckillOrder>> listOrders() {
        return Result.success(seckillService.listSeckillOrders());
    }
}
