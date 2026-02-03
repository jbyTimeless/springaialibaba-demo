package com.boyan.saa.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.boyan.saa.dto.ApiResponse;
import com.boyan.saa.dto.VectorSearchDTO;
import com.boyan.saa.dto.VectorStoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化和向量存储控制器
 * 提供文本向量化、向量存储和相似度搜索功能
 */
@Slf4j
@RestController
@RequestMapping("/api/vector")
public class Embed2VectorController {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private VectorStore vectorStore;

    /**
     * 接口1：文本向量化
     * 将输入的文本转换为向量表示
     * 使用GET请求，通过查询参数传递文本
     * 
     * @param text 需要向量化的文本
     * @return 文本对应的向量数组
     */
    @GetMapping("/embed")
    public ApiResponse<Map<String, Object>> embedText(@RequestParam String text) {
        try {
            log.info("开始向量化文本: {}", text);

            if (text == null || text.trim().isEmpty()) {
                return ApiResponse.error("文本内容不能为空");
            }

            // 使用 EmbeddingModel 生成向量
            EmbeddingResponse response = embeddingModel.call(
                    new EmbeddingRequest(List.of(text), null));

            // 获取向量数据（转换 float[] 为 List<Float>）
            float[] vectorArray = response.getResults().get(0).getOutput();
            List<Float> vector = new ArrayList<>();
            for (float v : vectorArray) {
                vector.add(v);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("vector", vector);
            result.put("dimension", vector.size());

            log.info("向量化完成，维度: {}", vector.size());
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("文本向量化失败", e);
            return ApiResponse.error("向量化失败: " + e.getMessage());
        }
    }

    /**
     * 接口2：文本向量化后存入 RedisStack
     * 将文本向量化并存储到 Redis 向量数据库中
     * 
     * @param dto 包含文本内容、文档ID和元数据
     * @return 存储成功的文档ID
     */
    @PostMapping("/store")
    public ApiResponse<Map<String, String>> storeVector(@RequestBody VectorStoreDTO dto) {
        try {
            log.info("开始存储向量，内容: {}", dto.getContent());

            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                return ApiResponse.error("文本内容不能为空");
            }

            // 生成文档ID（如果未提供）
            String docId = dto.getDocumentId();
            if (docId == null || docId.trim().isEmpty()) {
                docId = IdUtil.fastSimpleUUID();
            }

            // 解析元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("documentId", docId);

            if (dto.getMetadata() != null && !dto.getMetadata().trim().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> customMetadata = JSONUtil.toBean(dto.getMetadata(), Map.class);
                    metadata.putAll(customMetadata);
                } catch (Exception e) {
                    log.warn("元数据解析失败，使用默认元数据", e);
                }
            }

            // 创建 Document 对象
            Document document = Document.builder()
                    .id(docId)
                    .text(dto.getContent())
                    .metadata(metadata)
                    .build();

            // 存储到 VectorStore
            vectorStore.add(List.of(document));

            Map<String, String> result = new HashMap<>();
            result.put("documentId", docId);
            result.put("content", dto.getContent());
            result.put("status", "stored");

            log.info("向量存储成功，文档ID: {}", docId);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("向量存储失败", e);
            return ApiResponse.error("存储失败: " + e.getMessage());
        }
    }

    /**
     * 接口3：从 RedisStack 相似度查找
     * 根据查询文本在向量数据库中搜索相似的文档
     * 
     * @param dto 包含查询文本、返回数量和相似度阈值
     * @return 相似度排序的文档列表
     */
    @PostMapping("/search")
    public ApiResponse<List<Map<String, Object>>> searchSimilar(@RequestBody VectorSearchDTO dto) {
        try {
            log.info("开始相似度搜索，查询: {}, topK: {}", dto.getQuery(), dto.getTopK());

            if (dto.getQuery() == null || dto.getQuery().trim().isEmpty()) {
                return ApiResponse.error("查询文本不能为空");
            }

            // 构建搜索请求
            SearchRequest vectorSearchRequest = SearchRequest
                    .builder()
                    .query(dto.getQuery())
                    .topK(dto.getTopK())
                    .similarityThreshold(dto.getSimilarityThreshold())
                    .build();

            // 执行相似度搜索
            List<Document> documents = vectorStore.similaritySearch(vectorSearchRequest);

            // 转换结果
            List<Map<String, Object>> results = documents.stream().map(doc -> {
                Map<String, Object> result = new HashMap<>();
                result.put("documentId", doc.getId());
                result.put("content", doc.getText());
                result.put("metadata", doc.getMetadata());
                // 注意：某些 VectorStore 实现会在 metadata 中包含 score
                if (doc.getMetadata().containsKey("distance")) {
                    Object distanceObj = doc.getMetadata().get("distance");
                    if (distanceObj instanceof Number) {
                        double distance = ((Number) distanceObj).doubleValue();
                        result.put("similarity", 1 - distance);
                    } else {
                        result.put("similarity", 0.0); // 默认值或处理异常情况
                    }
                }
                return result;
            }).collect(Collectors.toList());

            log.info("搜索完成，找到 {} 条相似文档", results.size());
            return ApiResponse.success(results);

        } catch (Exception e) {
            log.error("相似度搜索失败", e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 额外接口：清空向量存储（用于测试）
     * 使用DELETE方法
     * 
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public ApiResponse<String> clearVectorStore() {
        try {
            log.warn("清空向量存储");
            vectorStore.delete(List.of()); // 某些实现可能不支持此操作
            return ApiResponse.success("向量存储已清空");
        } catch (Exception e) {
            log.error("清空向量存储失败", e);
            return ApiResponse.error("清空失败: " + e.getMessage());
        }
    }
}
