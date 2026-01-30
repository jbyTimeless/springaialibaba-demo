package com.boyan.saa.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatClientController {

    @Autowired
    @Qualifier("dashscopeChatClient")
    private ChatClient dashscopeChatClient;


    @GetMapping("/chatClient/chat")
    public String doChat(@RequestParam(value = "msg", defaultValue = "你是谁") String msg) {
        return dashscopeChatClient.prompt().user( msg).call().content();
    }

    @GetMapping("/chatClient/streamchat")
    public Flux<String> stream(@RequestParam(value = "msg", defaultValue = "你是谁") String msg) {
        return dashscopeChatClient.prompt().user( msg).stream().content();
    }

}
