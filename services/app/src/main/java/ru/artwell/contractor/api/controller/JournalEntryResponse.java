package services.app.src.main.java.ru.artwell.contractor.api.controller;

@Data @NoArgsConstructor @AllArgsConstructor
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