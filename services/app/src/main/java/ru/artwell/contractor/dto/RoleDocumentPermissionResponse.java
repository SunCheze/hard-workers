package services.app.src.main.java.ru.artwell.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDocumentPermissionResponse {
    private Long id;
    private String role;
    private Long documentTypeId;
    private String documentTypeCode;
    private String documentTypeName;
    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canApprove;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

