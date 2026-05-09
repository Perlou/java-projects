package com.example.chatbot.tools;

import com.example.chatbot.model.Ticket;
import com.example.chatbot.model.Ticket.TicketType;
import com.example.chatbot.model.Ticket.TicketStatus;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工单相关工具
 * 
 * 提供创建工单、查询工单状态等售后服务功能
 */
public class TicketTools {

    // 模拟工单数据库
    private final Map<String, Ticket> ticketDatabase = new HashMap<>();
    // 订单-工单映射
    private final Map<String, List<String>> orderTicketMap = new HashMap<>();

    /**
     * 创建退款工单
     */
    @Tool("为指定订单创建退款申请工单")
    public String createRefundTicket(
            @P("订单号") String orderId,
            @P("退款原因") String reason) {

        String ticketId = Ticket.generateTicketId();
        Ticket ticket = new Ticket(ticketId, orderId, "current_user",
                TicketType.REFUND, reason);

        ticketDatabase.put(ticketId, ticket);
        orderTicketMap.computeIfAbsent(orderId, k -> new ArrayList<>()).add(ticketId);

        return String.format(
                "已为订单 %s 创建退款工单:\n" +
                        "- 工单号: %s\n" +
                        "- 类型: 退款\n" +
                        "- 状态: 待处理\n" +
                        "- 预计处理时间: 1-3个工作日\n\n" +
                        "我们会通过短信通知您处理结果。",
                orderId, ticketId);
    }

    /**
     * 创建换货工单
     */
    @Tool("为指定订单创建换货申请工单")
    public String createExchangeTicket(
            @P("订单号") String orderId,
            @P("换货原因") String reason,
            @P("期望更换的商品或规格") String newProduct) {

        String ticketId = Ticket.generateTicketId();
        Ticket ticket = new Ticket(ticketId, orderId, "current_user",
                TicketType.EXCHANGE, reason + " -> 期望更换为: " + newProduct);

        ticketDatabase.put(ticketId, ticket);
        orderTicketMap.computeIfAbsent(orderId, k -> new ArrayList<>()).add(ticketId);

        return String.format(
                "已为订单 %s 创建换货工单:\n" +
                        "- 工单号: %s\n" +
                        "- 类型: 换货\n" +
                        "- 期望商品: %s\n" +
                        "- 状态: 待处理\n\n" +
                        "请将原商品寄回，我们收到后会尽快为您处理。",
                orderId, ticketId, newProduct);
    }

    /**
     * 创建投诉工单
     */
    @Tool("创建客户投诉工单")
    public String createComplaintTicket(
            @P("投诉内容") String complaint,
            @P("关联订单号，如果有的话") String orderId) {

        String ticketId = Ticket.generateTicketId();
        String orderRef = (orderId == null || orderId.isEmpty()) ? "无" : orderId;

        Ticket ticket = new Ticket(ticketId, orderRef, "current_user",
                TicketType.COMPLAINT, complaint);
        ticket.setStatus(TicketStatus.PROCESSING); // 投诉优先处理

        ticketDatabase.put(ticketId, ticket);

        return String.format(
                "已创建投诉工单:\n" +
                        "- 工单号: %s\n" +
                        "- 类型: 投诉\n" +
                        "- 状态: 处理中 (优先处理)\n\n" +
                        "我们非常重视您的反馈，客服主管会在24小时内与您联系。",
                ticketId);
    }

    /**
     * 查询工单状态
     */
    @Tool("根据工单号查询工单处理状态")
    public String queryTicket(@P("工单号") String ticketId) {
        Ticket ticket = ticketDatabase.get(ticketId);

        if (ticket == null) {
            return "未找到工单号为 " + ticketId + " 的工单。";
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("工单详情:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("工单号: ").append(ticket.getTicketId()).append("\n");
        sb.append("类型: ").append(ticket.getType().getDescription()).append("\n");
        sb.append("状态: ").append(ticket.getStatus().getDescription()).append("\n");
        sb.append("关联订单: ").append(ticket.getOrderId()).append("\n");
        sb.append("创建时间: ").append(ticket.getCreateTime().format(fmt)).append("\n");

        if (ticket.getResolution() != null) {
            sb.append("处理结果: ").append(ticket.getResolution()).append("\n");
        }

        // 添加状态说明
        switch (ticket.getStatus()) {
            case PENDING -> sb.append("\n⏳ 您的工单正在排队等待处理，预计1-3个工作日内处理。");
            case PROCESSING -> sb.append("\n🔄 客服正在处理您的工单，请耐心等待。");
            case RESOLVED -> sb.append("\n✅ 工单已处理完成，如有问题请联系客服。");
            case REJECTED -> sb.append("\n❌ 工单审核未通过，详情请查看处理结果。");
            case CLOSED -> sb.append("\n📁 工单已关闭。");
        }

        return sb.toString();
    }

    /**
     * 查询订单关联的工单
     */
    @Tool("查询指定订单的所有售后工单")
    public String queryOrderTickets(@P("订单号") String orderId) {
        List<String> ticketIds = orderTicketMap.get(orderId);

        if (ticketIds == null || ticketIds.isEmpty()) {
            return "订单 " + orderId + " 暂无售后工单记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("订单 ").append(orderId).append(" 的售后记录:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");

        for (String ticketId : ticketIds) {
            Ticket ticket = ticketDatabase.get(ticketId);
            if (ticket != null) {
                sb.append(String.format("- %s | %s | %s%n",
                        ticket.getTicketId(),
                        ticket.getType().getDescription(),
                        ticket.getStatus().getDescription()));
            }
        }

        return sb.toString();
    }

    /**
     * 取消工单
     */
    @Tool("取消指定的售后工单")
    public String cancelTicket(@P("工单号") String ticketId) {
        Ticket ticket = ticketDatabase.get(ticketId);

        if (ticket == null) {
            return "未找到工单: " + ticketId;
        }

        if (ticket.getStatus() == TicketStatus.RESOLVED ||
                ticket.getStatus() == TicketStatus.CLOSED) {
            return "工单 " + ticketId + " 已处理完成，无法取消。";
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolution("用户主动取消");

        return "工单 " + ticketId + " 已取消。如有其他需要，请随时联系客服。";
    }
}
