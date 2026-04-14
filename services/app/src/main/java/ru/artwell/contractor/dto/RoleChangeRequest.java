package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на смену роли пользователя.
 * Используется в PATCH /api/users/{id}/role.
 * Причина изменения записывается в role_assignment_history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {

    @NotBlank(message = "role is required")
    private String role;

    /** Причина смены роли (опционально, для аудита) */
    private String reason;
}

