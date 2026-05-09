package com.example.chatbot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 工单实体
 */
public class Ticket {

    private String ticketId;
    private String orderId;
    private String userId;
    private TicketType type;
    private TicketStatus status;
    private String description;
    private LocalDateTime createTime;
    private String resolution;

    public enum TicketType {
        REFUND("退款"),
        EXCHANGE("换货"),
        REPAIR("维修"),
        COMPLAINT("投诉"),
        INQUIRY("咨询");

        private final String description;

        TicketType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TicketStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        RESOLVED("已解决"),
        CLOSED("已关闭"),
        REJECTED("已拒绝");

        private final String description;

        TicketStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Ticket() {
        this.createTime = LocalDateTime.now();
        this.status = TicketStatus.PENDING;
    }

    public Ticket(String ticketId, String orderId, String userId,
            TicketType type, String description) {
        this();
        this.ticketId = ticketId;
        this.orderId = orderId;
        this.userId = userId;
        this.type = type;
        this.description = description;
    }

    // 生成工单号
    public static String generateTicketId() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 1000);
        return String.format("T-%s-%03d", date, random);
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return String.format(
                "工单号: %s, 类型: %s, 状态: %s, 关联订单: %s",
                ticketId, type.getDescription(), status.getDescription(), orderId);
    }
}
