package com.example.chatbot.tools;

import com.example.chatbot.model.Order;
import com.example.chatbot.model.Order.OrderStatus;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单相关工具
 * 
 * 提供订单查询、物流跟踪等功能
 * 使用 @Tool 注解让 AI 能够调用这些方法
 */
public class OrderTools {

    // 模拟订单数据库
    private final Map<String, Order> orderDatabase = new HashMap<>();

    public OrderTools() {
        initMockData();
    }

    /**
     * 初始化模拟数据
     */
    private void initMockData() {
        Order order1 = new Order("ORD-12345", "user1", "iPhone 15 Pro", 8999.0, OrderStatus.SHIPPING);
        order1.setTrackingNumber("SF1234567890");
        order1.setEstimatedDelivery(LocalDateTime.now().plusDays(1));
        orderDatabase.put(order1.getOrderId(), order1);

        Order order2 = new Order("ORD-12346", "user1", "AirPods Pro", 1899.0, OrderStatus.DELIVERED);
        order2.setTrackingNumber("SF1234567891");
        orderDatabase.put(order2.getOrderId(), order2);

        Order order3 = new Order("ORD-12347", "user2", "MacBook Air", 9499.0, OrderStatus.PAID);
        orderDatabase.put(order3.getOrderId(), order3);

        Order order4 = new Order("ORD-12348", "user1", "Apple Watch", 3299.0, OrderStatus.PENDING);
        orderDatabase.put(order4.getOrderId(), order4);
    }

    /**
     * 查询订单状态
     */
    @Tool("根据订单号查询订单状态和详细信息")
    public String queryOrder(@P("订单号，格式如 ORD-12345") String orderId) {
        Order order = orderDatabase.get(orderId);

        if (order == null) {
            return "未找到订单号为 " + orderId + " 的订单，请检查订单号是否正确。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("订单信息:\n");
        sb.append("- 订单号: ").append(order.getOrderId()).append("\n");
        sb.append("- 商品: ").append(order.getProductName()).append("\n");
        sb.append("- 金额: ¥").append(String.format("%.2f", order.getAmount())).append("\n");
        sb.append("- 状态: ").append(order.getStatus().getDescription()).append("\n");

        if (order.getTrackingNumber() != null) {
            sb.append("- 物流单号: ").append(order.getTrackingNumber()).append("\n");
        }

        if (order.getEstimatedDelivery() != null) {
            String eta = order.getEstimatedDelivery()
                    .format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));
            sb.append("- 预计送达: ").append(eta).append("\n");
        }

        return sb.toString();
    }

    /**
     * 查询物流信息
     */
    @Tool("查询订单的物流跟踪信息")
    public String trackLogistics(@P("订单号") String orderId) {
        Order order = orderDatabase.get(orderId);

        if (order == null) {
            return "未找到订单 " + orderId;
        }

        if (order.getTrackingNumber() == null) {
            return "订单 " + orderId + " 尚未发货，暂无物流信息。";
        }

        // 模拟物流轨迹
        StringBuilder sb = new StringBuilder();
        sb.append("物流信息 (单号: ").append(order.getTrackingNumber()).append(")\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        if (order.getStatus() == OrderStatus.DELIVERED) {
            sb.append("✓ ").append(now.minusHours(2).format(fmt))
                    .append(" 已签收，感谢您的购买\n");
        }
        if (order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED) {
            sb.append("● ").append(now.minusHours(5).format(fmt))
                    .append(" 正在派送中，快递员：张师傅 138****1234\n");
            sb.append("● ").append(now.minusDays(1).format(fmt))
                    .append(" 到达 北京市朝阳区 转运中心\n");
            sb.append("● ").append(now.minusDays(1).minusHours(8).format(fmt))
                    .append(" 从 上海市 发出\n");
            sb.append("● ").append(now.minusDays(2).format(fmt))
                    .append(" 商品已出库\n");
        }

        return sb.toString();
    }

    /**
     * 获取订单对象 (内部使用)
     */
    public Order getOrder(String orderId) {
        return orderDatabase.get(orderId);
    }

    /**
     * 更新订单状态 (内部使用)
     */
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderDatabase.get(orderId);
        if (order != null) {
            order.setStatus(status);
        }
    }
}
