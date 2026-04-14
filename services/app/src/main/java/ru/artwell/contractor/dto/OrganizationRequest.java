package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на создание или обновление организации.
 * Используется в POST /api/organizations и PUT /api/organizations/{id}.
 * Поля с @NotBlank проверяются Bean Validation перед попаданием в сервис.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    @NotBlank(message = "orgName is required")
    private String orgName;

    private String orgShortName;

    /**
     * Тип организации: CUSTOMER, TECH_CUSTOMER, CONTRACTOR,
     * SUB_CONTRACTOR, DESIGNER, SUPERVISOR.
     */
    private String orgType;

    /** ИНН (10 или 12 цифр). Проверка формата — на уровне сервиса. */
    private String inn;

    /** КПП (9 цифр) */
    private String kpp;

    /** Юридический адрес */
    private String legalAddress;
}