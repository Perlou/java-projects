package com.example.analytics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户行为数据模型
 * 
 * 模拟用户在平台上的行为数据，用于 Spark 分析处理。
 * 
 * 【数据字段说明】
 * - userId: 用户唯一标识
 * - actionType: 行为类型（VIEW/CLICK/PURCHASE/SEARCH 等）
 * - pageId: 页面标识
 * - productId: 商品 ID（可选）
 * - timestamp: 行为发生时间
 * - sessionId: 会话 ID
 * - deviceType: 设备类型
 * - duration: 停留时长（秒）
 * 
 * 【注意事项】
 * - 实现 Serializable 接口，支持 Spark 序列化
 * - 使用基本类型的包装类，避免 null 问题
 * 
 * @author Java Course
 * 
 */
public class UserAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据 ID */
    private String id;

    /** 用户 ID */
    private String userId;

    /** 行为类型 */
    private ActionType actionType;

    /** 页面 ID */
    private String pageId;

    /** 商品 ID（可选） */
    private String productId;

    /** 行为时间戳 */
    private LocalDateTime timestamp;

    /** 会话 ID */
    private String sessionId;

    /** 设备类型 */
    private String deviceType;

    /** 页面停留时长（秒） */
    private Integer duration;

    /** 来源渠道 */
    private String channel;

    /**
     * 行为类型枚举
     */
    public enum ActionType {
        /** 浏览页面 */
        VIEW,
        /** 点击 */
        CLICK,
        /** 搜索 */
        SEARCH,
        /** 加入购物车 */
        ADD_CART,
        /** 购买 */
        PURCHASE,
        /** 收藏 */
        FAVORITE,
        /** 分享 */
        SHARE
    }

    // 构造方法
    public UserAction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public UserAction(String userId, ActionType actionType, String pageId) {
        this();
        this.userId = userId;
        this.actionType = actionType;
        this.pageId = pageId;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UserAction action = new UserAction();

        public Builder userId(String userId) {
            action.userId = userId;
            return this;
        }

        public Builder actionType(ActionType actionType) {
            action.actionType = actionType;
            return this;
        }

        public Builder pageId(String pageId) {
            action.pageId = pageId;
            return this;
        }

        public Builder productId(String productId) {
            action.productId = productId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            action.timestamp = timestamp;
            return this;
        }

        public Builder sessionId(String sessionId) {
            action.sessionId = sessionId;
            return this;
        }

        public Builder deviceType(String deviceType) {
            action.deviceType = deviceType;
            return this;
        }

        public Builder duration(Integer duration) {
            action.duration = duration;
            return this;
        }

        public Builder channel(String channel) {
            action.channel = channel;
            return this;
        }

        public UserAction build() {
            return action;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "UserAction{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", actionType=" + actionType +
                ", pageId='" + pageId + '\'' +
                ", productId='" + productId + '\'' +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", duration=" + duration +
                ", channel='" + channel + '\'' +
                '}';
    }
}
