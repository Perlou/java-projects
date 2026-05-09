package com.example.loganalyzer.parser;

import com.example.loganalyzer.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志解析器
 */
@Component
public class LogParser {

    private static final Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s" +
                    "\\S+\\s" +
                    "\\S+\\s" +
                    "\\[(.*?)\\]\\s" +
                    "\"(\\S+)\\s(\\S+)\\s\\S+\"\\s" +
                    "(\\d{3})\\s" +
                    "(\\d+|-)\\s?" +
                    "\"?(.*?)\"?\\s" +
                    "\"(.*?)\"?");

    public LogEntry parse(String logLine) {
        if (logLine == null || logLine.isBlank()) {
            return null;
        }

        try {
            Matcher matcher = LOG_PATTERN.matcher(logLine);
            if (matcher.find()) {
                return LogEntry.builder()
                        .ip(matcher.group(1))
                        .userId(generateUserId(matcher.group(1), matcher.group(8)))
                        .timestamp(LocalDateTime.now())
                        .method(matcher.group(3))
                        .url(matcher.group(4))
                        .statusCode(Integer.parseInt(matcher.group(5)))
                        .responseSize(parseSize(matcher.group(6)))
                        .referer(matcher.group(7))
                        .userAgent(matcher.group(8))
                        .responseTime(generateResponseTime())
                        .build();
            }
        } catch (Exception e) {
            log.warn("解析日志失败: {}", logLine, e);
        }

        return null;
    }

    public LogEntry parseSimple(String logLine) {
        if (logLine == null || logLine.isBlank()) {
            return null;
        }

        try {
            String[] parts = logLine.split(",");
            if (parts.length >= 8) {
                return LogEntry.builder()
                        .ip(parts[0].trim())
                        .userId(parts[1].trim())
                        .timestamp(LocalDateTime.parse(parts[2].trim()))
                        .method(parts[3].trim())
                        .url(parts[4].trim())
                        .statusCode(Integer.parseInt(parts[5].trim()))
                        .responseSize(Long.parseLong(parts[6].trim()))
                        .responseTime(Long.parseLong(parts[7].trim()))
                        .build();
            }
        } catch (Exception e) {
            log.warn("解析简化日志失败: {}", logLine, e);
        }

        return null;
    }

    private long parseSize(String size) {
        if (size == null || size.equals("-")) {
            return 0;
        }
        return Long.parseLong(size);
    }

    private String generateUserId(String ip, String userAgent) {
        return "user_" + Math.abs((ip + userAgent).hashCode() % 10000);
    }

    private long generateResponseTime() {
        return (long) (Math.random() * 500) + 10;
    }
}
