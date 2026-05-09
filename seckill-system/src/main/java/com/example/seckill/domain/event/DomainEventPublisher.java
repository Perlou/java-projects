package com.example.seckill.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Phase 15: 领域事件发布器
 * 
 * 负责发布领域事件到：
 * 1. Spring ApplicationEventPublisher (进程内事件)
 * 2. RabbitMQ (分布式事件)
 * 
 * 事件发布策略：
 * - 本地事件：同步发布，用于同一服务内的事件处理
 * - 分布式事件：异步发布到 MQ，用于跨服务通信
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    // 事件交换机名称
    private static final String DOMAIN_EVENT_EXCHANGE = "domain.events";

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher,
            RabbitTemplate rabbitTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布领域事件（本地 + 分布式）
     */
    public void publish(DomainEvent event) {
        // 1. 发布本地事件
        publishLocal(event);

        // 2. 发布到 MQ（异步）
        publishToMq(event);
    }

    /**
     * 仅发布本地事件
     */
    public void publishLocal(DomainEvent event) {
        log.info("📢 发布本地事件: {}", event);
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 仅发布到 MQ
     */
    public void publishToMq(DomainEvent event) {
        try {
            String routingKey = buildRoutingKey(event);
            log.info("📤 发布 MQ 事件: {} -> {}", event.getEventType(), routingKey);

            rabbitTemplate.convertAndSend(
                    DOMAIN_EVENT_EXCHANGE,
                    routingKey,
                    event);
        } catch (Exception e) {
            log.error("❌ MQ 事件发布失败: {}", event, e);
            // 可以考虑将失败事件存储到本地表，后续重试
        }
    }

    /**
     * 构建路由键
     * 格式: domain.{聚合类型}.{事件类型}
     * 例如: domain.order.OrderCreatedEvent
     */
    private String buildRoutingKey(DomainEvent event) {
        String eventType = event.getEventType();
        String aggregateType = inferAggregateType(eventType);
        return String.format("domain.%s.%s", aggregateType, eventType);
    }

    /**
     * 根据事件类型推断聚合类型
     */
    private String inferAggregateType(String eventType) {
        if (eventType.startsWith("Order")) {
            return "order";
        } else if (eventType.startsWith("Seckill") || eventType.startsWith("Stock")) {
            return "seckill";
        } else if (eventType.startsWith("User")) {
            return "user";
        } else if (eventType.startsWith("Product")) {
            return "product";
        }
        return "unknown";
    }
}
