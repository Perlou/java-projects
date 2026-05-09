package com.example.chatbot.config;

import com.example.chatbot.rag.FaqRetriever;
import com.example.chatbot.tools.OrderTools;
import com.example.chatbot.tools.ProductTools;
import com.example.chatbot.tools.TicketTools;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 演示模式的 Chat 模型
 * 
 * 基于规则匹配模拟 LLM 行为
 * 用于无 API Key 时的功能演示
 */
public class DemoChatModel implements ChatLanguageModel {

    private final OrderTools orderTools;
    private final ProductTools productTools;
    private final TicketTools ticketTools;
    private final FaqRetriever faqRetriever;

    // 订单号正则
    private static final Pattern ORDER_PATTERN = Pattern.compile("ORD-\\d{5}");

    public DemoChatModel(OrderTools orderTools, ProductTools productTools,
            TicketTools ticketTools, FaqRetriever faqRetriever) {
        this.orderTools = orderTools;
        this.productTools = productTools;
        this.ticketTools = ticketTools;
        this.faqRetriever = faqRetriever;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        String lastMessage = messages.get(messages.size() - 1).toString();
        String response = processMessage(lastMessage);
        return Response.from(AiMessage.from(response));
    }

    @Override
    public String generate(String userMessage) {
        return processMessage(userMessage);
    }

    /**
     * 处理用户消息
     */
    public String processMessage(String message) {
        String lowerMsg = message.toLowerCase();

        // 问候
        if (containsAny(lowerMsg, "你好", "hi", "hello", "嗨")) {
            return "您好！我是智能客服小助手，请问有什么可以帮您？";
        }

        // 订单查询
        Matcher orderMatcher = ORDER_PATTERN.matcher(message);
        if (orderMatcher.find()) {
            String orderId = orderMatcher.group();
            if (containsAny(lowerMsg, "物流", "快递", "到哪")) {
                return orderTools.trackLogistics(orderId);
            }
            return orderTools.queryOrder(orderId);
        }

        if (containsAny(lowerMsg, "订单", "查询订单")) {
            return "请提供您的订单号（格式如 ORD-12345），我帮您查询。";
        }

        // 商品查询
        if (containsAny(lowerMsg, "iphone", "手机", "airpods", "耳机",
                "macbook", "电脑", "apple watch", "手表", "ipad")) {
            String product = extractProductName(lowerMsg);
            if (containsAny(lowerMsg, "有货", "库存")) {
                return productTools.checkStock(product);
            }
            return productTools.queryProduct(product);
        }

        if (containsAny(lowerMsg, "商品", "产品", "价格", "多少钱")) {
            return "请告诉我您想了解的商品名称，例如：iPhone 15、AirPods 等。";
        }

        // 退款/换货
        if (containsAny(lowerMsg, "退款", "退货", "换货", "售后")) {
            if (orderMatcher.find()) {
                String orderId = orderMatcher.group();
                if (containsAny(lowerMsg, "换货")) {
                    return ticketTools.createExchangeTicket(orderId, "用户申请换货", "待确认");
                }
                return ticketTools.createRefundTicket(orderId, "用户申请退款");
            }
            return "请提供您要处理的订单号，我来帮您创建售后工单。";
        }

        // FAQ 查询
        if (containsAny(lowerMsg, "政策", "流程", "怎么", "如何", "规则",
                "发票", "配送", "支付", "会员", "保修")) {
            for (String keyword : new String[] { "退货", "退款", "换货", "配送",
                    "发票", "支付", "会员", "售后", "客服", "隐私" }) {
                if (lowerMsg.contains(keyword)) {
                    return faqRetriever.searchFaq(keyword);
                }
            }
            return faqRetriever.searchFaq(message);
        }

        // 帮助
        if (containsAny(lowerMsg, "帮助", "功能", "能做什么")) {
            return """
                    我可以帮您：
                    1. 📦 查询订单状态和物流信息
                    2. 🛍️ 了解商品详情和库存
                    3. 💰 处理退款、换货申请
                    4. ❓ 回答常见问题

                    请直接告诉我您的需求~
                    """;
        }

        // 投诉
        if (containsAny(lowerMsg, "投诉", "不满", "差评")) {
            return ticketTools.createComplaintTicket(message, "");
        }

        // 默认回复
        return "不好意思，我没有完全理解您的问题。您可以尝试：\n" +
                "• 查询订单：告诉我订单号\n" +
                "• 商品咨询：告诉我商品名称\n" +
                "• 售后服务：说明您的需求\n\n" +
                "或者输入 '帮助' 查看我的功能。";
    }

    /**
     * 检查是否包含任意关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从文本中提取商品名称
     */
    private String extractProductName(String text) {
        if (text.contains("iphone 15 pro"))
            return "iphone 15 pro";
        if (text.contains("iphone15pro"))
            return "iphone 15 pro";
        if (text.contains("iphone 15"))
            return "iphone 15";
        if (text.contains("iphone15"))
            return "iphone 15";
        if (text.contains("iphone"))
            return "iphone 15";
        if (text.contains("airpods"))
            return "airpods";
        if (text.contains("macbook"))
            return "macbook";
        if (text.contains("apple watch") || text.contains("手表"))
            return "apple watch";
        if (text.contains("ipad"))
            return "ipad";
        if (text.contains("手机"))
            return "iphone 15";
        if (text.contains("耳机"))
            return "airpods";
        if (text.contains("电脑"))
            return "macbook";
        return text;
    }
}
