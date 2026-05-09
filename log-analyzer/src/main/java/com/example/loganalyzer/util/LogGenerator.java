package com.example.loganalyzer.util;

import com.example.loganalyzer.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 日志生成器
 */
@Component
public class LogGenerator {

    private static final Logger log = LoggerFactory.getLogger(LogGenerator.class);

    private static final String[] PAGES = {
            "/", "/index.html", "/products", "/products/list",
            "/products/detail", "/cart", "/checkout", "/order",
            "/user/login", "/user/register", "/user/profile",
            "/search", "/about", "/contact", "/api/v1/data"
    };

    private static final String[] IP_POOL = {
            "192.168.1.", "192.168.2.", "10.0.0.", "172.16.0."
    };

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15) Safari/605.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) Mobile/15E148",
            "Mozilla/5.0 (Android 14; Mobile) Chrome/120.0"
    };

    private static final int[] STATUS_CODES = { 200, 200, 200, 200, 200, 301, 302, 400, 404, 500 };

    public List<LogEntry> generate(int count) {
        log.info("开始生成 {} 条模拟日志...", count);

        List<LogEntry> logs = new ArrayList<>(count);
        LocalDateTime now = LocalDateTime.now();
        Random random = ThreadLocalRandom.current();

        List<String> userIds = new ArrayList<>();
        int userCount = Math.max(count / 5, 100);
        for (int i = 0; i < userCount; i++) {
            userIds.add("user_" + (1000 + i));
        }

        for (int i = 0; i < count; i++) {
            String ip = IP_POOL[random.nextInt(IP_POOL.length)] + random.nextInt(256);
            String userId = userIds.get(random.nextInt(userIds.size()));
            String page = selectPage(random);
            int statusCode = STATUS_CODES[random.nextInt(STATUS_CODES.length)];

            LogEntry entry = LogEntry.builder()
                    .ip(ip)
                    .userId(userId)
                    .timestamp(now.minusMinutes(random.nextInt(60 * 24)))
                    .method(random.nextDouble() < 0.9 ? "GET" : "POST")
                    .url(page)
                    .statusCode(statusCode)
                    .responseSize(random.nextLong(1000, 50000))
                    .responseTime(random.nextLong(10, 500))
                    .userAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .referer(random.nextDouble() < 0.3 ? selectPage(random) : "-")
                    .build();

            logs.add(entry);
        }

        log.info("日志生成完成，共 {} 条", logs.size());
        return logs;
    }

    private String selectPage(Random random) {
        double r = random.nextDouble();
        if (r < 0.3) {
            return PAGES[0];
        } else if (r < 0.5) {
            return PAGES[random.nextInt(4) + 1];
        } else {
            return PAGES[random.nextInt(PAGES.length)];
        }
    }
}
