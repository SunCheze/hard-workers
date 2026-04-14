package services.app.src.main.java.ru.artwell.contractor.api.controller;

@Data @NoArgsConstructor @AllArgsConstructor
public class JournalEntryRequest {
    @NotNull private Long documentId;
    @NotBlank private String action;
    private String comment;
    @NotNull private Long performedByUserId;
    private String journalType; // STAMP, APPROVAL, REJECTION, STATUS_CHANGE, COMMENT
}