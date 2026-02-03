package com.boyan.saa.dto;

import lombok.Data;

/**
 * 相似度搜索请求DTO
 */
@Data
public class VectorSearchDTO {
    /**
     * 查询文本
     */
    private String query;

    /**
     * 返回结果数量（默认5）
     */
    private Integer topK = 5;

    /**
     * 相似度阈值（0-1之间，默认0.7）
     */
    private Double similarityThreshold = 0.7;
}
