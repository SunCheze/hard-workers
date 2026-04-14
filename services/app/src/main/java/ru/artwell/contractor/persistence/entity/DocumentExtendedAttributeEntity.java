package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_extended_attributes")
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

    @Column(name = "attribute_type", length = 32)
    private String attributeType;

    @Column(name = "group_name", length = 256)
    private String groupName;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected DocumentExtendedAttributeEntity(DocumentEntity document, String attributeName,
                                              String attributeValue, String attributeType,
                                              LocalDateTime createdAt) {
        this.document = document;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.attributeType = attributeType;
        this.createdAt = createdAt;
    }
    // ─── Геттеры и сеттеры ───

    public Long getId() { return id; }
    public DocumentEntity getDocument() { return document; }
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
