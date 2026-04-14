package services.app.src.main.java.ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkVolumeResponse {
    private Long id;
    private Long constructionObjectId;
    private String constructionObjectName;
    private Long documentId;
    private String documentNumber;
    private String workType;
    private BigDecimal volume;
    private String unit;
    private boolean approved;
    private Long approvedByUserId;
    private String approvedByUsername;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
