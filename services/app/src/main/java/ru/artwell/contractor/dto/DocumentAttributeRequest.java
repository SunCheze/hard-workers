package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAttributeRequest {

    @NotBlank(message = "attributeName is required")
    private String attributeName;

    private String attributeValue;

    private String attributeType;
}