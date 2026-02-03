package com.boyan.saa.dto;

import lombok.Data;

/**
 * 存储向量到Redis请求DTO
 */
@Data
public class VectorStoreDTO {
    /**
     * 文本内容
     */
    private String content;

    /**
     * 文档ID（可选）
     */
    private String documentId;

    /**
     * 元数据（可选，JSON格式）
     */
    private String metadata;
}
