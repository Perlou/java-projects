package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.storage.LSMTreeSimulator;
import com.example.seckill.storage.ShardingStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分布式存储演示控制器
 * Phase 17: 分布式存储架构 - LSM-Tree 和数据分片
 */
@RestController
@RequestMapping("/api/demo/storage")
@Tag(name = "分布式存储演示", description = "Phase 17 - LSM-Tree 和数据分片策略演示")
public class DistributedStorageController {

    private final LSMTreeSimulator lsmTree;
    private final ShardingStrategy shardingStrategy;

    public DistributedStorageController(LSMTreeSimulator lsmTree,
            ShardingStrategy shardingStrategy) {
        this.lsmTree = lsmTree;
        this.shardingStrategy = shardingStrategy;
    }

    // ========== LSM-Tree 演示 ==========

    @PostMapping("/lsm/write")
    @Operation(summary = "LSM-Tree 写入", description = "写入数据到 MemTable，当 MemTable 满时自动刷盘到 SSTable")
    public Result<String> lsmWrite(
            @Parameter(description = "键") @RequestParam String key,
            @Parameter(description = "值") @RequestParam String value) {
        lsmTree.put(key, value);
        return Result.success("写入成功", "key=" + key + ", value=" + value);
    }

    @GetMapping("/lsm/read")
    @Operation(summary = "LSM-Tree 读取", description = "按顺序查询 MemTable → Immutable → SSTables")
    public Result<String> lsmRead(
            @Parameter(description = "键") @RequestParam String key) {
        String value = lsmTree.get(key);
        if (value == null) {
            return Result.fail("键不存在: " + key);
        }
        return Result.success("读取成功", value);
    }

    @DeleteMapping("/lsm/delete")
    @Operation(summary = "LSM-Tree 删除", description = "使用墓碑标记删除（不立即物理删除，等待 Compaction）")
    public Result<String> lsmDelete(
            @Parameter(description = "键") @RequestParam String key) {
        lsmTree.delete(key);
        return Result.success("删除成功（墓碑标记）", key);
    }

    @PostMapping("/lsm/flush")
    @Operation(summary = "LSM-Tree 刷盘", description = "手动将 MemTable 刷入 SSTable")
    public Result<Map<String, Object>> lsmFlush() {
        lsmTree.flush();
        return Result.success("刷盘完成", lsmTree.getStatus());
    }

    @PostMapping("/lsm/compact")
    @Operation(summary = "LSM-Tree Compaction", description = "手动触发 Level 0 合并到 Level 1")
    public Result<Map<String, Object>> lsmCompact() {
        lsmTree.compact();
        return Result.success("Compaction 完成", lsmTree.getStatus());
    }

    @GetMapping("/lsm/status")
    @Operation(summary = "LSM-Tree 状态", description = "查看 MemTable、各层 SSTable 和统计信息")
    public Result<Map<String, Object>> lsmStatus() {
        return Result.success("LSM-Tree 状态", lsmTree.getStatus());
    }

    @PostMapping("/lsm/reset")
    @Operation(summary = "LSM-Tree 重置", description = "清空所有数据，重置模拟器")
    public Result<String> lsmReset() {
        lsmTree.reset();
        return Result.success("重置完成", null);
    }

    // ========== 数据分片演示 ==========

    @GetMapping("/sharding/consistent-hash")
    @Operation(summary = "一致性哈希分片", description = "使用一致性哈希算法确定数据应存储的节点")
    public Result<Map<String, Object>> consistentHash(
            @Parameter(description = "数据键") @RequestParam String key) {
        return Result.success("一致性哈希结果",
                shardingStrategy.getConsistentHashInfo(key));
    }

    @GetMapping("/sharding/range")
    @Operation(summary = "范围分片", description = "按订单ID范围确定数据应存储的分片")
    public Result<Map<String, Object>> rangeSharding(
            @Parameter(description = "订单ID") @RequestParam Long orderId) {
        return Result.success("范围分片结果",
                shardingStrategy.getRangeShardingInfo(orderId));
    }

    @GetMapping("/sharding/hash-mod")
    @Operation(summary = "哈希取模分片", description = "使用 hash(key) % N 确定分片")
    public Result<Map<String, Object>> hashMod(
            @Parameter(description = "数据键") @RequestParam String key,
            @Parameter(description = "分片数量") @RequestParam(defaultValue = "4") int shardCount) {
        return Result.success("哈希取模结果",
                shardingStrategy.getHashModInfo(key, shardCount));
    }

    @GetMapping("/sharding/compare")
    @Operation(summary = "分片策略对比", description = "获取各种分片策略的对比信息")
    public Result<Map<String, Object>> compareStrategies() {
        return Result.success("分片策略对比",
                shardingStrategy.getStrategiesComparison());
    }

    @PostMapping("/sharding/add-node")
    @Operation(summary = "添加一致性哈希节点", description = "向一致性哈希环添加新节点")
    public Result<String> addNode(
            @Parameter(description = "节点名称") @RequestParam String nodeName) {
        shardingStrategy.addNode(nodeName);
        return Result.success("节点添加成功", nodeName);
    }

    @DeleteMapping("/sharding/remove-node")
    @Operation(summary = "移除一致性哈希节点", description = "从一致性哈希环移除节点")
    public Result<String> removeNode(
            @Parameter(description = "节点名称") @RequestParam String nodeName) {
        shardingStrategy.removeNode(nodeName);
        return Result.success("节点移除成功", nodeName);
    }

    // ========== 分布式存储概念 ==========

    @GetMapping("/concepts")
    @Operation(summary = "分布式存储概念", description = "获取分布式存储的核心概念说明")
    public Result<Map<String, Object>> getConcepts() {
        return Result.success("分布式存储概念", Map.of(
                "CAP", Map.of(
                        "description", "CAP 定理：分布式系统只能同时满足一致性、可用性、分区容错性中的两个",
                        "choices", Map.of(
                                "CP", "放弃可用性，如 ZooKeeper、HBase",
                                "AP", "放弃强一致性，如 Cassandra、DynamoDB",
                                "CA", "放弃分区容错（单机数据库）")),
                "LSM-Tree", Map.of(
                        "description", "日志结构合并树，写优化的存储结构",
                        "components", Map.of(
                                "MemTable", "内存中的有序数据结构",
                                "SSTable", "磁盘上的有序不可变文件",
                                "Compaction", "后台合并文件，删除重复数据"),
                        "amplifications", Map.of(
                                "writeAmplification", "数据可能被多次写入",
                                "readAmplification", "可能需要查询多层",
                                "spaceAmplification", "数据在多层暂时共存")),
                "NewSQL", Map.of(
                        "description", "新型分布式关系数据库，兼具 SQL 和分布式能力",
                        "examples", "TiDB, CockroachDB, YugabyteDB",
                        "features", Map.of(
                                "horizontalScaling", "水平扩展",
                                "distributedTransaction", "分布式事务",
                                "sqlCompatible", "兼容标准 SQL")),
                "Raft", Map.of(
                        "description", "易于理解的分布式共识算法",
                        "roles", Map.of(
                                "Leader", "处理客户端请求，复制日志",
                                "Follower", "被动接收 Leader 的日志",
                                "Candidate", "发起选举"),
                        "features", Map.of(
                                "leaderElection", "Leader 选举",
                                "logReplication", "日志复制",
                                "safety", "安全性保证"))));
    }
}
