package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "xsd_definitions",
        uniqueConstraints = @UniqueConstraint(name = "uk_xsd_resource_path", columnNames = {"xsd_resource_path"})
)
public class XsdDefinitionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "namespace_uri", nullable = false, length = 512)
    private String namespaceUri;

    @Column(name = "root_element_local_name", length = 256)
    private String rootElementLocalName;

    @Column(name = "document_type", length = 256)
    private String documentType;

    @Column(name = "xsd_resource_path", nullable = false, length = 512)
    private String xsdResourcePath;

    @Lob
    @Column(name = "xsd_content", nullable = false)
    private String xsdContent;

    @Column(name = "loaded_at", nullable = false)
    private LocalDateTime loadedAt;

    protected XsdDefinitionEntity() {
    }

    public XsdDefinitionEntity(String namespaceUri,
                                String rootElementLocalName,
                                String documentType,
                                String xsdResourcePath,
                                String xsdContent,
                                LocalDateTime loadedAt) {
        this.namespaceUri = namespaceUri;
        this.rootElementLocalName = rootElementLocalName;
        this.documentType = documentType;
        this.xsdResourcePath = xsdResourcePath;
        this.xsdContent = xsdContent;
        this.loadedAt = loadedAt;
    }

    /**
     * Обновление после повторного сканирования classpath (новые XSD или правка схемы).
     */
    public void syncFromClasspath(String namespaceUri,
                                  String rootElementLocalName,
                                  String documentType,
                                  String xsdContent,
                                  LocalDateTime loadedAt) {
        this.namespaceUri = namespaceUri;
        this.rootElementLocalName = rootElementLocalName;
        this.documentType = documentType;
        this.xsdContent = xsdContent;
        this.loadedAt = loadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public String getRootElementLocalName() {
        return rootElementLocalName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getXsdResourcePath() {
        return xsdResourcePath;
    }

    public String getXsdContent() {
        return xsdContent;
    }

    /**
     * Сущность расширенного атрибута документа (EAV-модель).
     *
     * Таблица: document_extended_attributes
     *
     * EAV (Entity-Attribute-Value) позволяет хранить произвольные атрибуты документов
     * без изменения схемы БД. Каждый документ может иметь любое количество атрибутов
     * с разными именами и значениями.
     *
     * Примеры атрибутов:
     *   - "priority" = "high"
     *   - "contract_number" = "Д-123/2024"
     *   - "review_deadline" = "2024-06-30"
     *   - "approval_status" = "approved"
     *
     * Связи:
     *   - document → DocumentEntity (к какому документу привязан атрибут)
     */
    @Entity
    @Table(
            name = "document_extended_attributes",
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_doc_attr_doc_name",
                    columnNames = {"document_id", "attribute_name"}
            ),
            indexes = {
                    @Index(name = "idx_doc_attrs_document", columnList = "document_id"),
                    @Index(name = "idx_doc_attrs_name", columnList = "attribute_name")
            }
    )
    public static class DocumentExtendedAttributeEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "document_id", nullable = false)
        private DocumentEntity document;

        @Column(name = "attribute_name", nullable = false, length = 256)
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

        public DocumentExtendedAttributeEntity(DocumentEntity document, String attributeName,
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
}

