package ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponse {
    private Long id;
    private Long documentId;
    private String action;
    private String comment;
    private Long performedByUserId;
    private String performedByUsername;
    private LocalDateTime performedAt;
    private String journalType;
}

