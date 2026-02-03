package com.boyan.saa.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class InitVectorDatabaseConfig {
    @jakarta.annotation.Resource
    private VectorStore vectorStore;

    @Value("classpath:ops.txt")
    private Resource opsFile;

    @PostConstruct
    public void init() throws Exception {
        TextReader textReader = new TextReader(opsFile);
        textReader.setCharset(Charset.defaultCharset());

        List<Document> list = new TokenTextSplitter().transform(textReader.read());

        vectorStore.add(list);

    }
}
