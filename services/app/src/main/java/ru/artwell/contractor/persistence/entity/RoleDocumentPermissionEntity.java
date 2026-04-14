package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_document_permissions")
public class RoleDocumentPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role", nullable = false, length = 64)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentTypeEntity documentType;

    @Column(name = "can_upload", nullable = false)
    private boolean canUpload;

    @Column(name = "can_view", nullable = false)
    private boolean canView;

    @Column(name = "can_edit", nullable = false)
    private boolean canEdit;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete;

    @Column(name = "can_approve", nullable = false)
    private boolean canApprove;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected RoleDocumentPermissionEntity() {
    }

    public RoleDocumentPermissionEntity(String role, DocumentTypeEntity documentType,
                                        boolean canView, boolean canCreate, boolean canEdit,
                                        boolean canDelete, boolean canApprove,
                                        LocalDateTime createdAt) {
        this.role = role;
        this.documentType = documentType;
        this.canView = canView;
        this.canUpload = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canApprove = canApprove;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
    public DocumentTypeEntity getDocumentType() { return documentType; }
    public boolean isCanView() { return canView; }
    public boolean isCanCreate() { return canUpload; }
    public boolean isCanEdit() { return canEdit; }
    public boolean isCanDelete() { return canDelete; }
    public boolean isCanApprove() { return canApprove; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setRole(String role) { this.role = role; }
    public void setCanView(boolean canView) { this.canView = canView; }
    public void setCanCreate(boolean canCreate) { this.canUpload = canCreate; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
    public void setCanApprove(boolean canApprove) { this.canApprove = canApprove; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}