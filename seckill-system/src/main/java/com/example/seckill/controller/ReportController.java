package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.dto.ProductRankDTO;
import com.example.seckill.dto.SalesReportDTO;
import com.example.seckill.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表接口 (Phase 11: 订单管理)
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "报表统计", description = "销售报表、热销商品")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    @Operation(summary = "按日销售统计")
    public Result<List<SalesReportDTO>> getDailySalesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(reportService.getDailySalesReport(startDate, endDate));
    }

    @GetMapping("/top-products")
    @Operation(summary = "热销商品排行")
    public Result<List<ProductRankDTO>> getTopProducts(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(reportService.getTopProducts(days, limit));
    }

    @DeleteMapping("/cache")
    @Operation(summary = "清除报表缓存")
    public Result<String> clearCache() {
        reportService.clearReportCache();
        return Result.success("缓存已清除");
    }
}
