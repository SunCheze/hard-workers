package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeRequest {

    @NotBlank(message = "typeCode is required")
    private String typeCode;

    @NotBlank(message = "typeName is required")
    private String typeName;

    private String category;

    private String xsdSchemaPath;
}