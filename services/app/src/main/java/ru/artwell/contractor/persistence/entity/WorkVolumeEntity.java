package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_volumes")
public class WorkVolumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "construction_object_id")
    private Long constructionObjectId;

    @Column(name = "construction_object_name", length = 512)
    private String constructionObjectName;

    @Column(name = "work_type", length = 512)
    private String workType;

    @Column(name = "volume", precision = 19, scale = 4)
    private BigDecimal volume;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserEntity approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected WorkVolumeEntity() {
    }

    public WorkVolumeEntity(Long constructionObjectId, String constructionObjectName,
                            DocumentEntity document, String workType, BigDecimal volume,
                            String unit, boolean approved, UserEntity approvedBy,
                            LocalDateTime approvedAt, LocalDateTime createdAt) {
        this.constructionObjectId = constructionObjectId;
        this.constructionObjectName = constructionObjectName;
        this.document = document;
        this.workType = workType;
        this.volume = volume;
        this.unit = unit;
        this.approved = approved;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public DocumentEntity getDocument() { return document; }
    public Long getConstructionObjectId() { return constructionObjectId; }
    public String getConstructionObjectName() { return constructionObjectName; }
    public String getWorkType() { return workType; }
    public BigDecimal getVolume() { return volume; }
    public String getUnit() { return unit; }
    public boolean isApproved() { return approved; }
    public UserEntity getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setWorkType(String workType) { this.workType = workType; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setConstructionObjectId(Long constructionObjectId) { this.constructionObjectId = constructionObjectId; }
    public void setConstructionObjectName(String constructionObjectName) { this.constructionObjectName = constructionObjectName; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setApprovedBy(UserEntity approvedBy) { this.approvedBy = approvedBy; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}