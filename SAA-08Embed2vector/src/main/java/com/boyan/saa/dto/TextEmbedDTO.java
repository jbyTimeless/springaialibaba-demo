package com.boyan.saa.dto;

import lombok.Data;

/**
 * 文本向量化请求DTO
 */
@Data
public class TextEmbedDTO {
    /**
     * 需要向量化的文本
     */
    private String text;
}
