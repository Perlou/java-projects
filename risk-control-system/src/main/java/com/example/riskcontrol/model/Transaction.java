package com.example.riskcontrol.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易事件模型
 * 
 * 【Flink 概念对应】
 * 在 Flink 中，这就是流处理的基本数据单元 - Event
 * 每条交易记录都携带事件时间（eventTime），用于事件时间语义处理
 */
public class Transaction {

    private String transactionId; // 交易 ID
    private String userId; // 用户 ID
    private String merchantId; // 商户 ID
    private BigDecimal amount; // 交易金额
    private String currency; // 币种
    private String city; // 交易城市
    private String country; // 交易国家
    private String deviceId; // 设备 ID
    private String ipAddress; // IP 地址
    private String transactionType; // 交易类型: PAYMENT, TRANSFER, WITHDRAWAL
    private String channel; // 渠道: APP, WEB, POS
    private LocalDateTime eventTime; // 事件时间（Flink Event Time）
    private LocalDateTime processTime; // 处理时间（Flink Processing Time）

    // 默认构造函数
    public Transaction() {
        this.processTime = LocalDateTime.now();
    }

    // 全参数构造函数
    public Transaction(String transactionId, String userId, String merchantId,
            BigDecimal amount, String currency, String city,
            String country, String deviceId, String ipAddress,
            String transactionType, String channel, LocalDateTime eventTime) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.city = city;
        this.country = country;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.transactionType = transactionType;
        this.channel = channel;
        this.eventTime = eventTime;
        this.processTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", city='" + city + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
