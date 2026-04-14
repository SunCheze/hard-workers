package services.app.src.main.java.ru.artwell.contractor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.RoleDocumentPermissionRequest;
import ru.artwell.contractor.dto.RoleDocumentPermissionResponse;
import ru.artwell.contractor.persistence.entity.DocumentTypeEntity;
import ru.artwell.contractor.persistence.entity.RoleDocumentPermissionEntity;
import ru.artwell.contractor.persistence.repository.DocumentTypeRepository;
import ru.artwell.contractor.persistence.repository.RoleDocumentPermissionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Сервис для управления правами доступа ролей к типам документов.
 *
 * Определяет матрицу прав: какая роль может делать с каким типом документов.
 * Правила: canView, canCreate, canEdit, canDelete, canApprove.
 * Каждая комбинация (role, documentType) уникальна.
 */
@Service
public class RoleDocumentPermissionService {

    private static final List<String> VALID_ROLES = List.of(
            "ADMIN", "CUSTOMER", "TECH_CUSTOMER", "CONTRACTOR",
            "SUB_CONTRACTOR", "DESIGNER", "SUPERVISOR"
    );

    private final ZoneId applicationZoneId;
    private final RoleDocumentPermissionRepository permissionRepository;
    private final DocumentTypeRepository documentTypeRepository;

    public RoleDocumentPermissionService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                                         RoleDocumentPermissionRepository permissionRepository,
                                         DocumentTypeRepository documentTypeRepository) {
        this.applicationZoneId = applicationZoneId;
        this.permissionRepository = permissionRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    // ─── Чтение ─────────────────────────────────────────────────

    /** Все разрешения для роли */
    @Transactional(readOnly = true)
    public List<RoleDocumentPermissionResponse> listByRole(String role) {
        return permissionRepository.findByRole(role).stream()
                .map(this::toResponse).toList();
    }

    /** Все разрешения для типа документа */
    @Transactional(readOnly = true)
    public List<RoleDocumentPermissionResponse> listByDocumentType(Long documentTypeId) {
        return permissionRepository.findByDocumentType_Id(documentTypeId).stream()
                .map(this::toResponse).toList();
    }

    /** Разрешение для конкретной роли и типа документа */
    @Transactional(readOnly = true)
    public RoleDocumentPermissionResponse getByRoleAndDocumentType(String role, Long documentTypeId) {
        return toResponse(permissionRepository.findByRoleAndDocumentType_Id(role, documentTypeId)
                .orElseThrow(() -> new NotFoundException(
                        "Permission not found for role=" + role + ", documentType=" + documentTypeId)));
    }

    // ─── Создание ───────────────────────────────────────────────

    @Transactional
    public RoleDocumentPermissionResponse create(RoleDocumentPermissionRequest request) {
        validateRole(request.getRole());
        DocumentTypeEntity docType = findDocTypeOrThrow(request.getDocumentTypeId());

        if (permissionRepository.existsByRoleAndDocumentType_Id(request.getRole(), request.getDocumentTypeId())) {
            throw new IllegalArgumentException(
                    "Permission already exists for role=" + request.getRole() + ", documentType=" + request.getDocumentTypeId());
        }

        RoleDocumentPermissionEntity entity = new RoleDocumentPermissionEntity(
                request.getRole(), docType,
                request.isCanView(), request.isCanCreate(), request.isCanEdit(),
                request.isCanDelete(), request.isCanApprove(),
                LocalDateTime.now(applicationZoneId)
        );
        return toResponse(permissionRepository.save(entity));
    }

    // ─── Обновление ─────────────────────────────────────────────

    @Transactional
    public RoleDocumentPermissionResponse update(Long id, RoleDocumentPermissionRequest request) {
        RoleDocumentPermissionEntity entity = findOrThrow(id);

        if (request.getRole() != null && !request.getRole().isBlank()) {
            validateRole(request.getRole());
            entity.setRole(null); // role is managed via unique constraint, don't change
        }

        entity.setCanView(request.isCanView());
        entity.setCanCreate(request.isCanCreate());
        entity.setCanEdit(request.isCanEdit());
        entity.setCanDelete(request.isCanDelete());
        entity.setCanApprove(request.isCanApprove());
        entity.setUpdatedAt(LocalDateTime.now(applicationZoneId));

        return toResponse(permissionRepository.save(entity));
    }

    // ─── Удаление ───────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        permissionRepository.delete(findOrThrow(id));
    }

    // ─── Вспомогательные ────────────────────────────────────────

    RoleDocumentPermissionEntity findOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
    }

    private DocumentTypeEntity findDocTypeOrThrow(Long id) {
        return documentTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document type not found: " + id));
    }

    private void validateRole(String role) {
        if (role == null || !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Valid: " + VALID_ROLES);
        }
    }

    private RoleDocumentPermissionResponse toResponse(RoleDocumentPermissionEntity e) {
        return new RoleDocumentPermissionResponse(
                e.getId(), e.getRole(),
                e.getDocumentType().getId(), e.getDocumentType().getTypeCode(), e.getDocumentType().getTypeName(),
                e.isCanView(), e.isCanCreate(), e.isCanEdit(), e.isCanDelete(), e.isCanApprove(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}

