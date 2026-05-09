package com.example.chatbot.model;

import java.time.LocalDateTime;

/**
 * 订单实体
 */
public class Order {

    private String orderId;
    private String userId;
    private String productName;
    private double amount;
    private OrderStatus status;
    private String trackingNumber;
    private LocalDateTime createTime;
    private LocalDateTime estimatedDelivery;

    public enum OrderStatus {
        PENDING("待付款"),
        PAID("已付款"),
        SHIPPING("配送中"),
        DELIVERED("已签收"),
        CANCELLED("已取消"),
        REFUNDING("退款中"),
        REFUNDED("已退款");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造方法
    public Order() {
    }

    public Order(String orderId, String userId, String productName,
            double amount, OrderStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    @Override
    public String toString() {
        return String.format(
                "订单号: %s, 商品: %s, 金额: ¥%.2f, 状态: %s",
                orderId, productName, amount, status.getDescription());
    }
}
