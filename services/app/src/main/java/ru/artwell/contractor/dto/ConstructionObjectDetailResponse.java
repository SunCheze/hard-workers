package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для детального ответа по объекту строительства.
 * В отличие от ConstructionObjectResponse включает всех участников проекта
 * с их ролями и привязанными организациями.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionObjectDetailResponse {

    private Long id;
    private String objectCode;
    private String objectName;
    private String address;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    /** Участники проекта с их ролями */
    private ParticipantInfo customer;
    private ParticipantInfo contractor;
    private ParticipantInfo designer;
    private ParticipantInfo supervisor;

    /**
     * Вложенный DTO — краткая информация об участнике проекта.
     * Содержит ID пользователя, username и наименование его организации.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long userId;
        private String username;
        private String orgName;
    }
}