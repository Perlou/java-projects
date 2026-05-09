package com.example.chatbot.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话记忆管理器
 * 
 * 为每个用户维护独立的对话记忆
 * 支持会话隔离、清理等功能
 */
public class ChatMemoryManager {

    // 用户会话记忆存储
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    // 每个会话保留的最大消息数
    private final int maxMessages;

    public ChatMemoryManager() {
        this(20); // 默认保留20条消息
    }

    public ChatMemoryManager(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * 获取或创建用户的对话记忆
     * 
     * 这个方法可以作为 ChatMemoryProvider 使用:
     * AiServices.builder(...)
     * .chatMemoryProvider(memoryManager::getOrCreateMemory)
     * .build();
     */
    public ChatMemory getOrCreateMemory(Object userId) {
        String id = String.valueOf(userId);
        return memories.computeIfAbsent(id, this::createNewMemory);
    }

    /**
     * 创建新的对话记忆
     */
    private ChatMemory createNewMemory(String userId) {
        return MessageWindowChatMemory.builder()
                .id(userId)
                .maxMessages(maxMessages)
                .build();
    }

    /**
     * 清除指定用户的对话记忆
     */
    public void clearMemory(String userId) {
        ChatMemory memory = memories.remove(userId);
        if (memory != null) {
            memory.clear();
        }
    }

    /**
     * 清除所有用户的对话记忆
     */
    public void clearAll() {
        memories.values().forEach(ChatMemory::clear);
        memories.clear();
    }

    /**
     * 获取当前活跃的会话数量
     */
    public int getActiveSessionCount() {
        return memories.size();
    }

    /**
     * 检查用户是否有活跃会话
     */
    public boolean hasSession(String userId) {
        return memories.containsKey(userId);
    }

    /**
     * 获取用户会话的消息数量
     */
    public int getMessageCount(String userId) {
        ChatMemory memory = memories.get(userId);
        return memory != null ? memory.messages().size() : 0;
    }
}
