package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponse {
    private Long id;
    private String typeCode;
    private String typeName;
    private String category;
    private String xsdSchemaPath;
    private boolean active;
}