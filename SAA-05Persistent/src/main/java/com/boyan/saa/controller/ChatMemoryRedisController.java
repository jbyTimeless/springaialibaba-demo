package com.boyan.saa.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
public class ChatMemoryRedisController {
    @Autowired
    @Qualifier("qwenMemoryClient")
    private ChatClient qwenMemoryClient;

    @GetMapping("/chatmemory/chat")
    public String chat(String question,String userId){
        return qwenMemoryClient.prompt(question).advisors(new Consumer<ChatClient.AdvisorSpec>() {
            @Override
            public void accept(ChatClient.AdvisorSpec advisorSpec) {
                advisorSpec.param(CONVERSATION_ID, userId);
            }
        }).call().content();

    }
}
