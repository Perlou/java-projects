package com.example.chatbot.tools;

import com.example.chatbot.model.Product;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品相关工具
 * 
 * 提供商品查询、库存查询、推荐等功能
 */
public class ProductTools {

    // 模拟商品数据库
    private final Map<String, Product> productDatabase = new HashMap<>();

    public ProductTools() {
        initMockData();
    }

    private void initMockData() {
        // iPhone 系列
        Product iphone15 = new Product("P001", "iPhone 15", "手机", 6999.0, 100);
        iphone15.setColors(List.of("黑色", "白色", "蓝色", "粉色", "绿色"));
        iphone15.setDescription("6.1英寸超视网膜XDR显示屏，A16仿生芯片");
        productDatabase.put(iphone15.getProductId(), iphone15);
        productDatabase.put("iphone 15", iphone15);
        productDatabase.put("iphone15", iphone15);

        Product iphone15Pro = new Product("P002", "iPhone 15 Pro", "手机", 8999.0, 50);
        iphone15Pro.setColors(List.of("原色钛金属", "蓝色钛金属", "白色钛金属", "黑色钛金属"));
        iphone15Pro.setDescription("6.1英寸，A17 Pro芯片，钛金属设计");
        productDatabase.put(iphone15Pro.getProductId(), iphone15Pro);
        productDatabase.put("iphone 15 pro", iphone15Pro);
        productDatabase.put("iphone15pro", iphone15Pro);

        // AirPods 系列
        Product airpods = new Product("P003", "AirPods Pro 2", "耳机", 1899.0, 200);
        airpods.setColors(List.of("白色"));
        airpods.setDescription("主动降噪，自适应音频，USB-C充电盒");
        productDatabase.put(airpods.getProductId(), airpods);
        productDatabase.put("airpods", airpods);
        productDatabase.put("airpods pro", airpods);

        // MacBook 系列
        Product macbook = new Product("P004", "MacBook Air M3", "电脑", 9499.0, 30);
        macbook.setColors(List.of("午夜色", "星光色", "深空灰", "银色"));
        macbook.setDescription("13.6英寸，M3芯片，8GB内存，256GB存储");
        productDatabase.put(macbook.getProductId(), macbook);
        productDatabase.put("macbook", macbook);
        productDatabase.put("macbook air", macbook);

        // Apple Watch
        Product watch = new Product("P005", "Apple Watch Series 9", "手表", 3299.0, 80);
        watch.setColors(List.of("午夜色", "星光色", "银色", "粉色", "红色"));
        watch.setDescription("45mm，GPS，血氧监测，心电图");
        productDatabase.put(watch.getProductId(), watch);
        productDatabase.put("apple watch", watch);

        // iPad
        Product ipad = new Product("P006", "iPad Pro 12.9", "平板", 9299.0, 0); // 缺货
        ipad.setColors(List.of("深空灰", "银色"));
        ipad.setDescription("12.9英寸Liquid Retina XDR，M2芯片");
        productDatabase.put(ipad.getProductId(), ipad);
        productDatabase.put("ipad", ipad);
        productDatabase.put("ipad pro", ipad);
    }

    /**
     * 查询商品信息
     */
    @Tool("查询商品的详细信息，包括价格、库存、颜色等")
    public String queryProduct(@P("商品名称，如 iPhone 15、AirPods") String productName) {
        // 模糊匹配
        String key = productName.toLowerCase().trim();
        Product product = productDatabase.get(key);

        if (product == null) {
            // 尝试部分匹配
            for (Map.Entry<String, Product> entry : productDatabase.entrySet()) {
                if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                    product = entry.getValue();
                    break;
                }
            }
        }

        if (product == null) {
            return "未找到商品: " + productName + "。请尝试搜索其他关键词。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("商品信息:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("名称: ").append(product.getName()).append("\n");
        sb.append("价格: ¥").append(String.format("%.0f", product.getPrice())).append("\n");

        if (product.isInStock()) {
            sb.append("库存: 有货 (").append(product.getStock()).append("件)\n");
        } else {
            sb.append("库存: 暂时缺货\n");
        }

        if (product.getColors() != null && !product.getColors().isEmpty()) {
            sb.append("颜色: ").append(String.join("、", product.getColors())).append("\n");
        }

        if (product.getDescription() != null) {
            sb.append("描述: ").append(product.getDescription()).append("\n");
        }

        sb.append("配送: 下单后24小时内发货\n");

        return sb.toString();
    }

    /**
     * 检查商品库存
     */
    @Tool("检查指定商品是否有货")
    public String checkStock(@P("商品名称") String productName) {
        String key = productName.toLowerCase().trim();
        Product product = productDatabase.get(key);

        if (product == null) {
            return "未找到商品: " + productName;
        }

        if (product.isInStock()) {
            return product.getName() + " 目前有货，库存 " + product.getStock() + " 件。";
        } else {
            return product.getName() + " 目前缺货，预计下周到货。您可以选择到货通知。";
        }
    }

    /**
     * 比较商品
     */
    @Tool("比较两个商品的价格和特性")
    public String compareProducts(
            @P("第一个商品名称") String product1,
            @P("第二个商品名称") String product2) {

        Product p1 = productDatabase.get(product1.toLowerCase().trim());
        Product p2 = productDatabase.get(product2.toLowerCase().trim());

        if (p1 == null || p2 == null) {
            return "无法比较，请确认商品名称正确。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("商品对比:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("%-15s | %-15s%n", p1.getName(), p2.getName()));
        sb.append(String.format("¥%.0f           | ¥%.0f%n", p1.getPrice(), p2.getPrice()));
        sb.append(String.format("%-15s | %-15s%n",
                p1.isInStock() ? "有货" : "缺货",
                p2.isInStock() ? "有货" : "缺货"));

        double diff = p1.getPrice() - p2.getPrice();
        if (diff > 0) {
            sb.append(p2.getName() + " 便宜 ¥" + String.format("%.0f", Math.abs(diff)));
        } else if (diff < 0) {
            sb.append(p1.getName() + " 便宜 ¥" + String.format("%.0f", Math.abs(diff)));
        } else {
            sb.append("价格相同");
        }

        return sb.toString();
    }

    /**
     * 获取商品推荐
     */
    @Tool("根据分类推荐热门商品")
    public String recommendProducts(@P("商品类别，如：手机、耳机、电脑") String category) {
        List<Product> products = productDatabase.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .distinct()
                .sorted((a, b) -> Double.compare(b.getPrice(), a.getPrice()))
                .limit(3)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            return "暂无 " + category + " 类别的商品推荐。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" 热门推荐:\n");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(String.format("%d. %s - ¥%.0f%n", i + 1, p.getName(), p.getPrice()));
        }

        return sb.toString();
    }
}
