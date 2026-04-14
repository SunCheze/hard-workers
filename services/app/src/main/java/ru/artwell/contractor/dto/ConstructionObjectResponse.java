package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для краткого ответа со списком объектов строительства.
 * Не включает информацию об участниках (для экономии).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionObjectResponse {

    private Long id;
    private String objectCode;
    private String objectName;
    private String address;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}

