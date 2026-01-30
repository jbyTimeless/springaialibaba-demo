package com.boyan.saa.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StreamOutputController {

    @Autowired
    @Qualifier("deepseek")
    private ChatModel deepseek;

    @Autowired
    @Qualifier("qwen")
    private ChatModel qwen;

    @Autowired
    @Qualifier("deepseekClient")
    private ChatClient deepseekClient;

    @Autowired
    @Qualifier("qwenClient")
    private ChatClient qwenClient;

    @GetMapping("/stream/deepseek1")
    public Flux<String> deepseek1(@RequestParam(value = "question",defaultValue = "你叫什么名字？") String question) {
        return deepseek.stream( question);
    }

    @GetMapping("/stream/qwen1")
    public Flux<String> qwen1(@RequestParam(value = "question",defaultValue = "你叫什么名字？") String question) {
        return qwen.stream( question);
    }

    @GetMapping("/stream/deepseek2")
    public Flux<String> deepseek2(@RequestParam(value = "question",defaultValue = "你叫什么名字？") String question) {
        return deepseekClient.prompt().user( question).stream().content();
    }

    @GetMapping("/stream/qwen2")
    public Flux<String> qwen2(@RequestParam(value = "question",defaultValue = "你叫什么名字？") String question) {
        return qwenClient.prompt().user( question).stream().content();
    }


}
