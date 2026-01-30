package com.boyan.saa.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class OllamaController {
    @Autowired
    @Qualifier("ollamaChatModel")
    private ChatModel chatModel ;

    @GetMapping("/ollama/chat")
    public String doChat(@RequestParam(value = "msg", defaultValue = "你是谁") String msg) {
        return chatModel.call(msg);
    }

    @GetMapping("/ollama/streamchat")
    public Flux<String> stream(@RequestParam(value = "msg", defaultValue = "你是谁") String msg) {
        return chatModel.stream(msg);
    }

}
