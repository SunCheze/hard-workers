package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для одной записи истории смены ролей.
 * Используется в GET /api/users/{id}/role-history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRecordResponse {

    private String oldRole;
    private String newRole;
    private String changedBy;
    private LocalDateTime changedAt;
    private String reason;
}
