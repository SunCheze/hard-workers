package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления профиля пользователя (без смены пароля и роли).
 * Используется в PUT /api/users/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String fullName;
    private Long organizationId;
    private String email;
}