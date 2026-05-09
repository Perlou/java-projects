package com.example.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckill.common.Result;
import com.example.seckill.dto.CreateOrderDTO;
import com.example.seckill.entity.Order;
import com.example.seckill.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口 (Phase 11: 订单管理)
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "创建订单、支付、取消")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public Result<Order> createOrder(@RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(dto));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单")
    public Result<Order> payOrder(@PathVariable Long id) {
        return Result.success(orderService.payOrder(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Order> cancelOrder(@PathVariable Long id) {
        return Result.success(orderService.cancelOrder(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrder(@PathVariable Long id) {
        return Result.success(orderService.findOrderWithItems(id));
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "根据订单号查询")
    public Result<Order> getOrderByNo(@PathVariable String orderNo) {
        return Result.success(orderService.findByOrderNo(orderNo));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订单列表")
    public Result<List<Order>> getUserOrders(@PathVariable Long userId) {
        return Result.success(orderService.findByUserId(userId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询订单")
    public Result<Page<Order>> getOrderPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.findPage(pageNum, pageSize));
    }
}
