package com.boyan.saa.controller;

import com.boyan.saa.repository.MySQLChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * MySQL记忆化聊天控制器
 */
@RestController
@RequestMapping("/chatmemory/mysql")
public class ChatMemoryMySQLController {

    @Autowired
    @Qualifier("qwenMysqlMemoryClient")
    private ChatClient qwenMysqlMemoryClient;

    @Autowired
    private MySQLChatMemoryRepository mysqlChatMemoryRepository;

    /**
     * 带MySQL持久化记忆的聊天接口
     * @param question 用户问题
     * @param userId 用户ID（作为会话ID）
     * @return AI回复
     */
    @GetMapping("/chat")
    public String chat(String question, String userId) {
        // 严格参数校验
        if (!StringUtils.hasText(question)) {
            return "参数错误：question不能为空";
        }
        if (!StringUtils.hasText(userId)) {
            return "参数错误：userId不能为空";
        }

        try {
            // 调用AI并绑定会话记忆（userId作为conversationId）
            return qwenMysqlMemoryClient.prompt(question)
                    .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, userId))
                    .call()
                    .content();
        } catch (Exception e) {
            return "聊天失败：" + e.getMessage();
        }
    }

    /**
     * 清空指定用户的聊天记忆
     */
    @GetMapping("/clear")
    public String clearMemory(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "参数错误：userId不能为空";
        }
        try {
            mysqlChatMemoryRepository.deleteByConversationId(userId);
            return "已成功清空用户[" + userId + "]的MySQL聊天记忆";
        } catch (Exception e) {
            return "清空记忆失败：" + e.getMessage();
        }
    }

    /**
     * 查询所有有记忆的用户ID
     */
    @GetMapping("/list-conv-ids")
    public List<String> listConversationIds() {
        try {
            return mysqlChatMemoryRepository.findConversationIds();
        } catch (Exception e) {
            throw new RuntimeException("查询会话ID列表失败", e);
        }
    }
}