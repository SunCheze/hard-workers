package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAttributeResponse {
    private Long id;
    private Long documentVersionId;
    private String attributeName;
    private String attributeValue;
    private String attributeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}