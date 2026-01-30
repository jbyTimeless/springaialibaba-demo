package com.boyan.saa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 适配Spring AI的MySQL聊天记忆仓库（修复序列化、SQL、异常处理）
 */
public class MySQLChatMemoryRepository implements ChatMemoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    // 预编译List<Message>的类型（避免重复创建）
    private final CollectionType messageListType;

    // SQL语句（适配chat_memory表）
    private static final String FIND_CONVERSATION_IDS_SQL = "SELECT DISTINCT conversation_id FROM chat_memory";
    private static final String FIND_BY_CONV_ID_SQL = "SELECT messages FROM chat_memory WHERE conversation_id = ? LIMIT 1";
    private static final String SAVE_ALL_SQL = "REPLACE INTO chat_memory (conversation_id, user_id, role, content, messages) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_BY_CONV_ID_SQL = "DELETE FROM chat_memory WHERE conversation_id = ?";

    // 构造器注入
    public MySQLChatMemoryRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        // 预构建List<Message>的类型（关键：修复反序列化类型问题）
        this.messageListType = objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class);
    }

    /**
     * 查询所有会话ID（用户ID）
     */
    @Override
    public List<String> findConversationIds() {
        try {
            return jdbcTemplate.queryForList(FIND_CONVERSATION_IDS_SQL, String.class);
        } catch (Exception e) {
            throw new RuntimeException("查询所有会话ID失败：" + e.getMessage(), e);
        }
    }
    /**
     * 根据会话ID查询消息列表（核心：修复反序列化逻辑）
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 参数校验
        if (!StringUtils.hasText(conversationId)) {
            return Collections.emptyList();
        }

        try {
            // 查询messages字段的JSON字符串
            List<String> resultList = jdbcTemplate.queryForList(FIND_BY_CONV_ID_SQL, String.class, conversationId);
            Optional<String> messagesJson = resultList.stream().findFirst();

            // 反序列化JSON为Message列表
            if (messagesJson.isPresent() && StringUtils.hasText(messagesJson.get())) {
                String jsonStr = messagesJson.get();
                // 临时禁用FAIL_ON_UNKNOWN_PROPERTIES，让Jackson根据@class字段自动处理多态
                ObjectMapper tempMapper = objectMapper.copy();
                tempMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return tempMapper.readValue(jsonStr,
                        tempMapper.getTypeFactory().constructCollectionType(List.class, Message.class));
            }
            return Collections.emptyList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化Message列表失败（会话ID：" + conversationId + "）：" + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("查询对话记忆失败（会话ID：" + conversationId + "）：" + e.getMessage(), e);
        }
    }

    /**
     * 保存/更新对话记忆（修复content空值、完善异常）
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 参数校验
        if (!StringUtils.hasText(conversationId) || messages == null || messages.isEmpty()) {
            return;
        }

        try {
            // 序列化Message列表为JSON
            String messagesJson = objectMapper.writeValueAsString(messages);

            // 取最后一条消息的角色和内容（修复空值问题）
            Message lastMessage = messages.get(messages.size() - 1);
            String role = lastMessage.getClass().getSimpleName().replace("Message", "").toUpperCase();

            // 安全地获取文本内容
            String content = Optional.ofNullable(lastMessage.getText()).orElse("");

            // 插入/更新数据（REPLACE基于唯一索引uk_conversation_id）
            jdbcTemplate.update(SAVE_ALL_SQL,
                    conversationId,          // 会话ID
                    conversationId,          // user_id 复用会话ID（可根据业务调整）
                    role,                    // 最后一条消息的角色
                    content,                 // 最后一条消息的内容（非空）
                    messagesJson             // 核心：消息列表JSON
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化Message列表失败（会话ID：" + conversationId + "）：" + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("保存对话记忆失败（会话ID：" + conversationId + "）：" + e.getMessage(), e);
        }
    }

    /**
     * 根据会话ID删除记忆
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            try {
                jdbcTemplate.update(DELETE_BY_CONV_ID_SQL, conversationId);
            } catch (Exception e) {
                throw new RuntimeException("删除对话记忆失败（会话ID：" + conversationId + "）：" + e.getMessage(), e);
            }
        }
    }
}