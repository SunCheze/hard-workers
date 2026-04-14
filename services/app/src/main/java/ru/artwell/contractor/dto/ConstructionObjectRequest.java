package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для запроса на создание или обновление объекта строительства.
 * Используется в POST /api/construction-objects и PUT /api/construction-objects/{id}.
 * Поля-ссылки на участников (customer, contractor и т.д.)
 * содержат ID пользователей, которые привязываются на уровне сервиса.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionObjectRequest {

    @NotBlank(message = "objectCode is required")
    private String objectCode;

    @NotBlank(message = "objectName is required")
    private String objectName;

    private String address;

    /** ID пользователя — заказчик */
    private Long customerUserId;

    /** ID пользователя — подрядчик */
    private Long contractorUserId;

    /** ID пользователя — проектировщик */
    private Long designerUserId;

    /** ID пользователя — стройконтроль */
    private Long supervisorUserId;

    /** Статус: active / completed / suspended */
    private String status;

    /** Плановая дата начала работ */
    private LocalDate startDate;

    /** Плановая дата окончания */
    private LocalDate endDate;
}
