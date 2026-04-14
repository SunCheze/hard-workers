package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с профилем пользователя.
 * Содержит как данные пользователя, так и информацию о его организации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String role;
    private OrganizationResponse organization;
    private String email;
    private boolean active;
    private LocalDateTime lastLogin;
}
