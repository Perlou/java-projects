package com.example.chatbot.model;

import java.util.List;

/**
 * 商品实体
 */
public class Product {

    private String productId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private List<String> colors;
    private String description;

    public Product() {
    }

    public Product(String productId, String name, String category,
            double price, int stock) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        String stockStatus = stock > 0 ? "有货(" + stock + "件)" : "缺货";
        return String.format(
                "商品: %s, 价格: ¥%.2f, 库存: %s",
                name, price, stockStatus);
    }
}
