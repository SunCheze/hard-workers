package services.app.src.main.java.ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryRequest {

    @NotNull(message = "documentId is required")
    private Long documentId;

    @NotBlank(message = "action is required")
    private String action;

    private String comment;

    @NotNull(message = "performedByUserId is required")
    private Long performedByUserId;

    /** Journal type: STAMP, APPROVAL, REJECTION, STATUS_CHANGE, COMMENT */
    private String journalType;
}
