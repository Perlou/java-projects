package com.example.riskcontrol.model;

import java.time.LocalDateTime;

/**
 * 登录事件模型
 * 
 * 用于 CEP 模式匹配 - 连续登录失败检测
 */
public class LoginEvent {

    private String eventId;
    private String userId;
    private String status; // SUCCESS, FAIL
    private String ipAddress;
    private String deviceId;
    private String city;
    private LocalDateTime eventTime;
    private String failReason; // 失败原因

    public LoginEvent() {
        this.eventTime = LocalDateTime.now();
    }

    public LoginEvent(String eventId, String userId, String status,
            String ipAddress, String city, LocalDateTime eventTime) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.ipAddress = ipAddress;
        this.city = city;
        this.eventTime = eventTime;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public boolean isFailed() {
        return "FAIL".equals(status);
    }

    @Override
    public String toString() {
        return "LoginEvent{userId='" + userId + "', status='" + status +
                "', city='" + city + "', eventTime=" + eventTime + "}";
    }
}
