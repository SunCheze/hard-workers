package services.app.src.main.java.ru.artwell.contractor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDocumentPermissionRequest {

    @NotBlank(message = "role is required")
    private String role;

    @NotNull(message = "documentTypeId is required")
    private Long documentTypeId;

    private boolean canView = false;
    private boolean canCreate = false;
    private boolean canEdit = false;
    private boolean canDelete = false;
    private boolean canApprove = false;
}
