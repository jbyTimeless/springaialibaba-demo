package com.boyan.saa.config;

import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class InitVectorDatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(InitVectorDatabaseConfig.class);

    @jakarta.annotation.Resource
    private VectorStore vectorStore;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("classpath:ops.txt")
    private Resource opsFile;

    @PostConstruct
    public void init() {

        TextReader textReader = new TextReader(opsFile);
        textReader.setCharset(Charset.defaultCharset());

        List<Document> list = new TokenTextSplitter().transform(textReader.read());

        String sourceMetadata = (String) textReader.getCustomMetadata().get("source");

        String textHash = SecureUtil.md5(sourceMetadata);
        String redisKey = "vector-store:" + textHash;

        Boolean retFlag = redisTemplate.opsForValue().setIfAbsent(redisKey, "1");
        if (Boolean.TRUE.equals(retFlag)) {
            vectorStore.add(list);
        } else {
            log.info("skip add vector store");
        }

    }
}
