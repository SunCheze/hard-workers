package ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParticipantRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "participantRole is required")
    private String participantRole;
}