package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParticipantResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String userRole;
    private String participantRole;
    private LocalDateTime assignedAt;
}