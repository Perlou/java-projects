package com.example.analytics.service;

import com.example.analytics.model.UserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据采集服务
 * 
 * 负责接收和存储用户行为数据，供 Spark 分析使用。
 * 
 * 【主要功能】
 * - 接收单条用户行为数据
 * - 批量接收用户行为数据
 * - 生成模拟测试数据
 * - 提供数据读取接口
 * 
 * @author Java Course
 * 
 */
@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    // 内存数据存储（生产环境应使用 Kafka 等消息队列）
    private final List<UserAction> dataBuffer = new CopyOnWriteArrayList<>();

    // 模拟数据生成器
    private final Random random = new Random();

    // 预定义的测试数据
    private static final String[] USER_IDS = { "user001", "user002", "user003", "user004", "user005",
            "user006", "user007", "user008", "user009", "user010" };
    private static final String[] PAGE_IDS = { "home", "product_list", "product_detail", "cart",
            "checkout", "payment", "order_success", "search", "category", "profile" };
    private static final String[] PRODUCT_IDS = { "P001", "P002", "P003", "P004", "P005",
            "P006", "P007", "P008", "P009", "P010" };
    private static final String[] DEVICE_TYPES = { "PC", "Mobile", "Tablet" };
    private static final String[] CHANNELS = { "direct", "search", "social", "email", "ads" };

    /**
     * 接收单条用户行为数据
     */
    public void ingest(UserAction action) {
        if (action.getTimestamp() == null) {
            action.setTimestamp(LocalDateTime.now());
        }
        dataBuffer.add(action);
        log.debug("接收数据: userId={}, action={}, page={}",
                action.getUserId(), action.getActionType(), action.getPageId());
    }

    /**
     * 批量接收用户行为数据
     */
    public int batchIngest(List<UserAction> actions) {
        LocalDateTime now = LocalDateTime.now();
        for (UserAction action : actions) {
            if (action.getTimestamp() == null) {
                action.setTimestamp(now);
            }
        }
        dataBuffer.addAll(actions);
        log.info("批量接收数据: {} 条", actions.size());
        return actions.size();
    }

    /**
     * 获取所有缓冲数据
     */
    public List<UserAction> getAllData() {
        return new ArrayList<>(dataBuffer);
    }

    /**
     * 获取最近 N 条数据
     */
    public List<UserAction> getRecentData(int limit) {
        int size = dataBuffer.size();
        if (size <= limit) {
            return new ArrayList<>(dataBuffer);
        }
        return new ArrayList<>(dataBuffer.subList(size - limit, size));
    }

    /**
     * 获取数据量
     */
    public int getDataCount() {
        return dataBuffer.size();
    }

    /**
     * 清空数据缓冲区
     */
    public void clearBuffer() {
        dataBuffer.clear();
        log.info("数据缓冲区已清空");
    }

    /**
     * 生成模拟测试数据
     * 
     * @param count 生成数据条数
     * @return 生成的数据列表
     */
    public List<UserAction> generateTestData(int count) {
        log.info("生成 {} 条模拟数据...", count);

        List<UserAction> testData = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(1);

        for (int i = 0; i < count; i++) {
            UserAction action = UserAction.builder()
                    .userId(randomElement(USER_IDS))
                    .actionType(randomActionType())
                    .pageId(randomElement(PAGE_IDS))
                    .productId(random.nextDouble() > 0.3 ? randomElement(PRODUCT_IDS) : null)
                    .timestamp(baseTime.plusSeconds(random.nextInt(3600)))
                    .sessionId(UUID.randomUUID().toString().substring(0, 8))
                    .deviceType(randomElement(DEVICE_TYPES))
                    .duration(random.nextInt(300) + 1)
                    .channel(randomElement(CHANNELS))
                    .build();

            testData.add(action);
        }

        // 添加到缓冲区
        dataBuffer.addAll(testData);
        log.info("已生成并添加 {} 条模拟数据到缓冲区", count);

        return testData;
    }

    /**
     * 生成用户路径模拟数据（同一用户的连续行为）
     */
    public List<UserAction> generateUserJourneyData(String userId, int steps) {
        List<UserAction> journey = new ArrayList<>();
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime time = LocalDateTime.now();
        String deviceType = randomElement(DEVICE_TYPES);
        String channel = randomElement(CHANNELS);

        // 模拟典型用户路径：首页 -> 搜索/列表 -> 详情 -> 购物车 -> 结算 -> 支付
        String[] typicalPath = { "home", "search", "product_list", "product_detail",
                "cart", "checkout", "payment", "order_success" };

        for (int i = 0; i < Math.min(steps, typicalPath.length); i++) {
            UserAction action = UserAction.builder()
                    .userId(userId)
                    .actionType(getActionTypeForPage(typicalPath[i]))
                    .pageId(typicalPath[i])
                    .productId(i >= 2 ? randomElement(PRODUCT_IDS) : null)
                    .timestamp(time.plusSeconds(i * 30L))
                    .sessionId(sessionId)
                    .deviceType(deviceType)
                    .duration(random.nextInt(60) + 10)
                    .channel(channel)
                    .build();

            journey.add(action);
        }

        dataBuffer.addAll(journey);
        log.info("已生成用户 {} 的 {} 步行为路径", userId, journey.size());

        return journey;
    }

    // 辅助方法
    private String randomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private UserAction.ActionType randomActionType() {
        UserAction.ActionType[] types = UserAction.ActionType.values();
        return types[random.nextInt(types.length)];
    }

    private UserAction.ActionType getActionTypeForPage(String page) {
        return switch (page) {
            case "home", "product_list", "product_detail", "category" -> UserAction.ActionType.VIEW;
            case "search" -> UserAction.ActionType.SEARCH;
            case "cart" -> UserAction.ActionType.ADD_CART;
            case "checkout", "payment", "order_success" -> UserAction.ActionType.PURCHASE;
            default -> UserAction.ActionType.CLICK;
        };
    }
}
