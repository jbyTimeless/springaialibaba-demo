package com.boyan.saa.controller;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

@RestController
public class Text2VoiceController {

    @Resource
    private SpeechSynthesisModel speechSynthesisModel;

    public static final String BAILIAN_VOICE_MODEL = "cosyvoice-v2";

    public static final String BAILIAN_VOICE_TIMBER = "longhuhu";


    @GetMapping("/t2v/voice")
    public String text2Voice(@RequestParam(name = "msg",defaultValue = "温馨提醒，支付宝到账一亿元") String msg) {
        String filePath = "D:\\voice\\" + System.currentTimeMillis() + ".mp3";

        DashScopeSpeechSynthesisOptions options = DashScopeSpeechSynthesisOptions.builder()
                .model(BAILIAN_VOICE_MODEL)
                .voice(BAILIAN_VOICE_TIMBER)
                .build();

        SpeechSynthesisResponse response = speechSynthesisModel.call(new SpeechSynthesisPrompt(msg, options));

        ByteBuffer byteBuffer = response.getResult().getOutput().getAudio();
        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath)){
            fileOutputStream.write(byteBuffer.array());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return filePath;
    }
}
