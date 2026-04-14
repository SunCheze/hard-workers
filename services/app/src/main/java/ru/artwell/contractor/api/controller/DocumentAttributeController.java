package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.DocumentAttributeRequest;
import ru.artwell.contractor.dto.DocumentAttributeResponse;
import ru.artwell.contractor.service.DocumentAttributeService;

import java.util.List;

/**
 * REST-контроллер для работы с расширенными атрибутами документов (EAV-модель).
 *
 * Позволяет прикреплять произвольные метаданные к документам без изменения
 * схемы БД. Каждый атрибут — это пара "имя-значение" с указанием типа.
 *
 * Примеры использования:
 *   POST /api/documents/1/attributes
 *   { "attributeName": "priority", "attributeValue": "high", "attributeType": "STRING" }
 *
 *   POST /api/documents/1/attributes
 *   { "attributeName": "review_deadline", "attributeValue": "2024-06-30", "attributeType": "DATE" }
 *
 * Ограничения:
 * — attributeName уникален в рамках одного документа
 * — attributeName нельзя изменить после создания (нужно удалить и создать заново)
 * — Физическое удаление (атрибуты не являются критичными данными)
 *
 * Общая схема:
 *   GET    /api/documents/{documentId}/attributes                  → список атрибутов
 *   POST   /api/documents/{documentId}/attributes                  → создать атрибут
 *   PUT    /api/documents/{documentId}/attributes/{attributeId}    → обновить атрибут
 *   DELETE /api/documents/{documentId}/attributes/{attributeId}    → удалить атрибут
 */
@Tag(name = "Document Attributes", description = "Расширенные атрибуты документов (EAV-модель: произвольные метаданные)")
@RestController
@RequestMapping("/api/documents/{documentId}/attributes")
public class DocumentAttributeController {

    /**
     * Сервис расширенных атрибутов — содержит бизнес-логику: проверку уникальности
     * attributeName, валидацию принадлежности атрибута документу.
     */
    private final DocumentAttributeService documentAttributeService;

    public DocumentAttributeController(DocumentAttributeService documentAttributeService) {
        this.documentAttributeService = documentAttributeService;
    }

    /**
     * Получить список расширенных атрибутов документа.
     *
     * Как работает:
     * 1. Проверяет существование документа (404 если не найден)
     * 2. Загружает все атрибуты документа из БД, отсортированные по имени
     * 3. Маппит в DTO и возвращает список
     *
     * Пример: GET /api/documents/1/attributes
     * Ответ: [
     *   { id: 1, attributeName: "priority", attributeValue: "high", ... },
     *   { id: 2, attributeName: "contract_number", attributeValue: "Д-123", ... }
     * ]
     */
    @Operation(summary = "Список расширенных атрибутов документа",
            description = "Все EAV-атрибуты документа, отсортированные по имени")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список атрибутов"),
            @ApiResponse(responseCode = "404", description = "Документ не найден")
    })
    @GetMapping
    public ResponseEntity<List<DocumentAttributeResponse>> list(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(documentAttributeService.listByDocument(documentId));
    }

    /**
     * Создать новый расширенный атрибут для документа.
     *
     * Как работает:
     * 1. @Valid проверяет @NotBlank поле attributeName
     * 2. Проверяет существование документа (404)
     * 3. Проверяет уникальность attributeName в рамках документа (400 если дубликат)
     * 4. Если attributeType не указан — по умолчанию "STRING"
     * 5. Сохраняет и возвращает 201 Created
     */
    @Operation(summary = "Создать расширенный атрибут")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Атрибут создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или attributeName уже существует"),
            @ApiResponse(responseCode = "404", description = "Документ не найден")
    })
    @PostMapping
    public ResponseEntity<DocumentAttributeResponse> create(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Valid @RequestBody DocumentAttributeRequest request
    ) {
        DocumentAttributeResponse created = documentAttributeService.create(documentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить значение расширенного атрибута.
     *
     * Как работает:
     * 1. Ищет атрибут по {attributeId} с проверкой принадлежности к {documentId}
     *    (404 если не найден или принадлежит другому документу)
     * 2. Обновляет attributeValue и (опционально) attributeType
     * 3. Обновляет поле updatedAt
     * 4. attributeName изменить нельзя — для этого нужно удалить и создать заново
     */
    @Operation(summary = "Обновить расширенный атрибут")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Атрибут обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Атрибут или документ не найден")
    })
    @PutMapping("/{attributeId}")
    public ResponseEntity<DocumentAttributeResponse> update(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Parameter(description = "ID атрибута", example = "5")
            @PathVariable Long attributeId,

            @Valid @RequestBody DocumentAttributeRequest request
    ) {
        return ResponseEntity.ok(documentAttributeService.update(documentId, attributeId, request));
    }

    /**
     * Удалить расширенный атрибут (физическое удаление).
     *
     * Как работает:
     * Атрибут удаляется из БД полностью. В отличие от документов и организаций,
     * для атрибутов применяется физическое удаление, т.к. они не являются
     * критичными для бизнес-логики данными.
     */
    @Operation(summary = "Удалить расширенный атрибут")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Атрибут удалён"),
            @ApiResponse(responseCode = "404", description = "Атрибут или документ не найден")
    })
    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Parameter(description = "ID атрибута", example = "5")
            @PathVariable Long attributeId
    ) {
        documentAttributeService.delete(documentId, attributeId);
        return ResponseEntity.ok().build();
    }
}
