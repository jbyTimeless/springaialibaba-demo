package com.boyan.saa.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * VectorStore 配置类
 * 手动配置 RedisVectorStore，使用 Builder 模式创建实例
 *
 * @author boyan
 * @date 2026-02-03
 */
@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.index-name:default-index}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix:default-prefix}")
    private String prefix;

    @Value("${spring.ai.vectorstore.redis.initialize-schema:true}")
    private boolean initializeSchema;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * 创建 JedisPooled Bean
     * 
     * @return JedisPooled 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public JedisPooled jedisPooled() {
        return new JedisPooled(redisHost, redisPort);
    }

    /**
     * 创建 VectorStore Bean
     * 使用 Builder 模式创建 RedisVectorStore，避免自动维度检测导致的 DashScope API 调用失败
     * 
     * @param jedisPooled    Jedis连接池
     * @param embeddingModel DashScope 嵌入模型
     * @return VectorStore 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(JedisPooled jedisPooled, DashScopeEmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(initializeSchema)
                .build();
    }
}
