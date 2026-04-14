package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа с данными организации.
 * Используется во всех GET/POST/PUT-эндпоинтах организаций.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;
    private String orgName;
    private String orgShortName;
    private String orgType;
    private String inn;
    private String kpp;
    private String legalAddress;
    private boolean active;
}