package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на создание пользователя.
 * Используется в POST /api/users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    private String fullName;

    /**
     * Роль: ADMIN, CUSTOMER, TECH_CUSTOMER, CONTRACTOR,
     * SUB_CONTRACTOR, DESIGNER, SUPERVISOR.
     */
    private String role;

    /** ID организации, к которой привязывается пользователь */
    private Long organizationId;

    private String email;
}

