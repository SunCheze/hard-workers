package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionDetailResponse {
    private Long id;
    private Long documentId;
    private String documentNumber;
    private String documentTypeCode;
    private int versionNumber;
    private String xmlFileName;
    private long xmlFileSize;
    private String validationStatus;
    private String validationErrors;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    private Long previousVersionId;
}