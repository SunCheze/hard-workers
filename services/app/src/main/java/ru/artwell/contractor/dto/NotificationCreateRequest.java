package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "title is required")
    private String title;

    private String message;

    /** Тип: DOCUMENT_UPLOADED, APPROVAL_REQUIRED, APPROVED, REJECTED, STATUS_CHANGED, MENTION, SYSTEM */
    private String type;
}
