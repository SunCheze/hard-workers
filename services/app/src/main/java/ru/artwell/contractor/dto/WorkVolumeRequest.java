package services.app.src.main.java.ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkVolumeRequest {

    private Long constructionObjectId;
    private String constructionObjectName;

    @NotNull(message = "documentId is required")
    private Long documentId;

    @NotBlank(message = "workType is required")
    private String workType;

    @NotNull(message = "volume is required")
    private BigDecimal volume;

    /** Единица измерения: м3, м2, т, шт */
    private String unit;
}
