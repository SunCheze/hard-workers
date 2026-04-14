package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_extended_attributes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doc_attr_doc_name",
                columnNames = {"document_version_id", "attribute_name"}
        ),
        indexes = {
                @Index(name = "idx_doc_attrs_document", columnList = "document_version_id"),
                @Index(name = "idx_doc_attrs_name", columnList = "attribute_name")
        }
)
public class DocumentExtendedAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersionEntity documentVersion;

    @Column(name = "attribute_name", nullable = false, length = 512)
    private String attributeName;

    @Column(name = "attribute_value", columnDefinition = "text")
    private String attributeValue;

    @Column(name = "attribute_type", length = 64)
    private String attributeType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected DocumentExtendedAttributeEntity() {
    }

    public DocumentExtendedAttributeEntity(DocumentVersionEntity documentVersion, String attributeName,
                                           String attributeValue, String attributeType,
                                           LocalDateTime createdAt) {
        this.documentVersion = documentVersion;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.attributeType = attributeType;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public DocumentVersionEntity getDocumentVersion() { return documentVersion; }
    public String getAttributeName() { return attributeName; }
    public String getAttributeValue() { return attributeValue; }
    public String getAttributeType() { return attributeType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}