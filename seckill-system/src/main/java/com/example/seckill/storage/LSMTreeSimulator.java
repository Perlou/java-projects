package com.example.seckill.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LSM-Tree 存储引擎模拟器
 * Phase 17: 分布式存储架构 - LSM-Tree 原理演示
 * 
 * LSM-Tree (Log-Structured Merge-Tree) 结构:
 * 
 * 写入 → [MemTable (内存)]
 * ↓ 满了刷盘
 * [Immutable MemTable]
 * ↓
 * [Level 0 SSTables] - 可能有重叠
 * ↓ Compaction
 * [Level 1 SSTables] - 有序且不重叠
 * ↓
 * [Level 2...]
 * 
 * 特点:
 * - 写优化：顺序写入，高吞吐
 * - 读取需查多层：先 MemTable，后 SSTables
 * - 后台 Compaction 合并文件
 */
@Component
public class LSMTreeSimulator {

    private static final Logger log = LoggerFactory.getLogger(LSMTreeSimulator.class);

    // MemTable: 内存中的有序数据结构 (使用跳表实现)
    private ConcurrentSkipListMap<String, String> memTable = new ConcurrentSkipListMap<>();

    // Immutable MemTable (等待刷盘)
    private ConcurrentSkipListMap<String, String> immutableMemTable = null;

    // SSTable 层级结构 (Level 0, 1, 2...)
    private final List<List<SortedMap<String, String>>> levels = new ArrayList<>();

    // MemTable 大小阈值
    private static final int MEMTABLE_SIZE_THRESHOLD = 5;

    // Level 0 文件数阈值 (触发 Compaction)
    private static final int LEVEL0_COMPACTION_THRESHOLD = 3;

    // 写操作计数器
    private final AtomicInteger writeCount = new AtomicInteger(0);

    // 读写锁
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // 统计信息
    private final AtomicInteger memTableReads = new AtomicInteger(0);
    private final AtomicInteger sstableReads = new AtomicInteger(0);
    private final AtomicInteger compactionCount = new AtomicInteger(0);

    public LSMTreeSimulator() {
        // 初始化 3 个 Level
        for (int i = 0; i < 3; i++) {
            levels.add(new ArrayList<>());
        }
        log.info("LSM-Tree 模拟器初始化完成");
    }

    /**
     * 写入数据
     * 流程: 写入 MemTable → 如果满了则转为 Immutable → 异步刷盘
     */
    public void put(String key, String value) {
        lock.writeLock().lock();
        try {
            // 1. 写入 MemTable
            memTable.put(key, value);
            writeCount.incrementAndGet();
            log.info("[LSM 写入] key={}, value={}, memTableSize={}",
                    key, value, memTable.size());

            // 2. 检查是否需要刷盘
            if (memTable.size() >= MEMTABLE_SIZE_THRESHOLD) {
                flushToImmutable();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 读取数据
     * 流程: MemTable → Immutable MemTable → Level 0 → Level 1 → Level 2...
     */
    public String get(String key) {
        lock.readLock().lock();
        try {
            // 1. 查询 MemTable
            String value = memTable.get(key);
            if (value != null) {
                memTableReads.incrementAndGet();
                log.info("[LSM 读取] key={} 在 MemTable 命中", key);
                return value;
            }

            // 2. 查询 Immutable MemTable
            if (immutableMemTable != null) {
                value = immutableMemTable.get(key);
                if (value != null) {
                    memTableReads.incrementAndGet();
                    log.info("[LSM 读取] key={} 在 Immutable MemTable 命中", key);
                    return value;
                }
            }

            // 3. 查询 SSTables (从 Level 0 到 Level N)
            for (int level = 0; level < levels.size(); level++) {
                List<SortedMap<String, String>> sstables = levels.get(level);
                for (SortedMap<String, String> sst : sstables) {
                    value = sst.get(key);
                    if (value != null) {
                        sstableReads.incrementAndGet();
                        log.info("[LSM 读取] key={} 在 Level {} SSTable 命中", key, level);
                        return value;
                    }
                }
            }

            log.info("[LSM 读取] key={} 未找到", key);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 删除数据 (写入墓碑标记)
     */
    public void delete(String key) {
        // LSM-Tree 使用墓碑标记删除
        put(key, "__TOMBSTONE__");
        log.info("[LSM 删除] key={} 已标记为墓碑", key);
    }

    /**
     * 手动触发刷盘
     */
    public void flush() {
        lock.writeLock().lock();
        try {
            if (!memTable.isEmpty()) {
                flushToImmutable();
            }
            if (immutableMemTable != null) {
                flushToDisk();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 手动触发 Compaction
     */
    public void compact() {
        lock.writeLock().lock();
        try {
            doCompaction();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取 LSM-Tree 状态
     */
    public Map<String, Object> getStatus() {
        lock.readLock().lock();
        try {
            Map<String, Object> status = new LinkedHashMap<>();

            // MemTable 状态
            status.put("memTable", Map.of(
                    "size", memTable.size(),
                    "threshold", MEMTABLE_SIZE_THRESHOLD,
                    "keys", new ArrayList<>(memTable.keySet())));

            // Immutable MemTable 状态
            status.put("immutableMemTable", Map.of(
                    "exists", immutableMemTable != null,
                    "size", immutableMemTable != null ? immutableMemTable.size() : 0));

            // SSTable 各层状态
            List<Map<String, Object>> levelStats = new ArrayList<>();
            for (int i = 0; i < levels.size(); i++) {
                List<SortedMap<String, String>> level = levels.get(i);
                int totalKeys = level.stream().mapToInt(Map::size).sum();
                levelStats.add(Map.of(
                        "level", i,
                        "sstableCount", level.size(),
                        "totalKeys", totalKeys,
                        "compactionThreshold", i == 0 ? LEVEL0_COMPACTION_THRESHOLD : "N/A"));
            }
            status.put("levels", levelStats);

            // 统计信息
            status.put("stats", Map.of(
                    "totalWrites", writeCount.get(),
                    "memTableReads", memTableReads.get(),
                    "sstableReads", sstableReads.get(),
                    "compactionCount", compactionCount.get()));

            // LSM-Tree 特性说明
            status.put("characteristics", Map.of(
                    "writeAmplification", "数据可能被多次写入 (Compaction)",
                    "readAmplification", "可能需要查询多个 Level",
                    "spaceAmplification", "数据在多个 Level 暂时共存"));

            return status;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 重置模拟器
     */
    public void reset() {
        lock.writeLock().lock();
        try {
            memTable.clear();
            immutableMemTable = null;
            for (List<SortedMap<String, String>> level : levels) {
                level.clear();
            }
            writeCount.set(0);
            memTableReads.set(0);
            sstableReads.set(0);
            compactionCount.set(0);
            log.info("[LSM 重置] 模拟器已重置");
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ========== 内部方法 ==========

    private void flushToImmutable() {
        // 如果已有 Immutable，先刷盘
        if (immutableMemTable != null) {
            flushToDisk();
        }

        // MemTable → Immutable MemTable
        immutableMemTable = memTable;
        memTable = new ConcurrentSkipListMap<>();
        log.info("[LSM 刷盘] MemTable 转为 Immutable, size={}", immutableMemTable.size());
    }

    private void flushToDisk() {
        if (immutableMemTable == null || immutableMemTable.isEmpty()) {
            return;
        }

        // 创建新的 SSTable (模拟写入磁盘)
        SortedMap<String, String> newSSTable = new TreeMap<>(immutableMemTable);
        levels.get(0).add(newSSTable);
        immutableMemTable = null;

        log.info("[LSM 刷盘] Immutable MemTable 写入 Level 0, 当前 Level 0 文件数={}",
                levels.get(0).size());

        // 检查是否需要 Compaction
        if (levels.get(0).size() >= LEVEL0_COMPACTION_THRESHOLD) {
            doCompaction();
        }
    }

    private void doCompaction() {
        List<SortedMap<String, String>> level0 = levels.get(0);
        if (level0.isEmpty()) {
            return;
        }

        log.info("[LSM Compaction] 开始合并 Level 0 ({} 个文件) 到 Level 1", level0.size());

        // 合并 Level 0 所有文件
        SortedMap<String, String> merged = new TreeMap<>();
        for (SortedMap<String, String> sst : level0) {
            merged.putAll(sst);
        }

        // 移除墓碑
        merged.entrySet().removeIf(e -> "__TOMBSTONE__".equals(e.getValue()));

        // 清空 Level 0，添加到 Level 1
        level0.clear();
        levels.get(1).add(merged);
        compactionCount.incrementAndGet();

        log.info("[LSM Compaction] 完成，合并后 {} 个键值对写入 Level 1", merged.size());
    }
}
