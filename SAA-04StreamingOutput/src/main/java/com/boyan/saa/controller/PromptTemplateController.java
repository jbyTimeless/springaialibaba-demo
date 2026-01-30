package com.boyan.saa.controller;

import com.boyan.saa.entity.record.StudentRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@RestController
public class PromptTemplateController {

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

    @Value("classpath:/prompttemplate/moban.txt")
    private Resource userTemplate;

    @GetMapping("/prompttemplate/chat")
    public Flux<String> chat(String topic, String output_format, String word_count) {
        PromptTemplate promptTemplate = new PromptTemplate(userTemplate);

        // 修复：先检查 null 值，再使用 StringUtil
        if (topic == null || output_format == null || word_count == null) {
            return Flux.just("参数为空");
        }

        // 或者使用 StringUtils.hasText() 检查 null 和空字符串
        if (!StringUtils.hasText(topic) || !StringUtils.hasText(output_format) || !StringUtils.hasText(word_count)) {
            return Flux.just("请输入正确的参数");
        }


        Prompt prompt = promptTemplate.create(Map.of(
                "topic", topic,
                "output_format", output_format,
                "word_count", word_count
        ));
        return deepseekClient.prompt(prompt).stream().content();
    }

    @GetMapping("/prompttemplate/chat2")
    public String chat2(String systemTopic, String userTopic){
        if(systemTopic == null || userTopic == null){
            return "参数为空";
        }
        if(!StringUtils.hasText(systemTopic) || !StringUtils.hasText(userTopic)){
            return "请输入正确的参数";
        }
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("你是{systemTopic}助手，只回答{systemTopic}其它无可奉告，以HTML格式输出结果。");
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("systemTopic", systemTopic)) ;
        PromptTemplate userPromptTemplate = new PromptTemplate("解释一下{userTopic}");
        Message userMessage = userPromptTemplate.createMessage(Map.of("userTopic", userTopic)) ;
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return deepseekClient.prompt(prompt).call().content();


    }

    @GetMapping("/prompttemplate/chat3")
    public Flux<String> chat3(String question){
        return deepseekClient.prompt(question)
                .system("你是碧蓝航线的白凤，我的老婆。回答不少于300字")
                .stream()
                .content();

    }

    @GetMapping("/prompttemplate/chat4")
    public String chat4(String question){
        if(question == null){
            return "参数为空";
        }
        if(!StringUtils.hasText(question)){
            return "请输入正确的参数";
        }
        SystemMessage systemMessage = new SystemMessage("你是碧蓝航线的白凤，我的老婆。回答不少于300字");
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        String result = deepseekClient.prompt(prompt).call().content();
        System.out.println( result);
        return result;
    }

    @GetMapping("/structuredoutput/chat5")
    public StudentRecord chat5(@RequestParam(name = "sname") String sname,
                               @RequestParam(name = "email") String email){
        String stringTemplate = "学号1001，我叫{sname},大学专业计算机科学与技术，我的邮箱是{email}";
        return qwenClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(stringTemplate)
                        .param("sname", sname)
                        .param("email", email)).call().entity(StudentRecord.class);
    }

}
