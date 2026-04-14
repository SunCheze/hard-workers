package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.DocumentTypeRequest;
import ru.artwell.contractor.dto.DocumentTypeResponse;
import ru.artwell.contractor.service.DocumentTypeService;

/**
 * REST-контроллер для работы с типами документов (справочник document_types).
 *
 * Типы документов определяют классификацию XML-документов по корневому элементу.
 * Всего предусмотрено 21 тип по классификации Минстроя: ПОДРЯД, АКТ_ПРИЁМКИ,
 * ПРОЕКТ, СМЕТА, РАЙДЕР and т.д.
 *
 * Каждый тип может иметь XSD-схему, которая используется для валидации
 * загружаемых XML-документов (проверка структуры и обязательных полей).
 *
 * Общая схема взаимодействия:
 *   HTTP-запрос → Controller → Service → Repository → БД
 *   Controller получает DTO, передаёт в Service, Service работает с Entity,
 *   маппит Entity → DTO и возвращает в Controller.
 *
 * Эндпоинты:
 *   GET    /api/document-types         → список (пагинация, фильтры)
 *   POST   /api/document-types         → создание нового типа
 *   GET    /api/document-types/{id}    → детали типа
 *   PUT    /api/document-types/{id}    → обновление типа
 *   DELETE /api/document-types/{id}    → мягкое удаление (active=false)
 */
@Tag(name = "Document Types", description = "CRUD для справочника типов документов (классификация Минстроя)")
@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    /**
     * Сервис типов документов — содержит бизнес-логику: проверку уникальности
     * typeCode, маппинг Entity ↔ DTO. Контроллер делегирует всю логику ему.
     */
    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    /**
     * Получить список типов документов с пагинацией и фильтрами.
     *
     * Как работает:
     * 1. Извлекает параметры query из URL (?category=...&search=...&page=...&size=...)
     * 2. Если передан search — выполняется поиск по typeCode и typeName (LIKE)
     * 3. Если передан category — фильтрация по категории
     * 4. Иначе — все активные типы с пагинацией
     * 5. Возвращает Page<DocumentTypeResponse> с метаданными пагинации
     *
     * Пример: GET /api/document-types?search=ПОДРЯД&page=0&size=20
     */
    @Operation(summary = "Список типов документов",
            description = "Пагинация, фильтр по категории и текстовый поиск по typeCode/typeName")
    @GetMapping
    public ResponseEntity<Page<DocumentTypeResponse>> list(
            @Parameter(description = "Фильтр по категории")
            @RequestParam(required = false) String category,

            @Parameter(description = "Текстовый поиск по typeCode или typeName")
            @RequestParam(required = false) String search,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DocumentTypeResponse> result = documentTypeService.list(
                category, search, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    /**
     * Получить один тип документа по ID.
     *
     * Как работает:
     * 1. Извлекает {id} из пути URL
     * 2. Передаёт в DocumentTypeService.getById()
     * 3. Если не найден — сервис выбросит NotFoundException → GlobalExceptionHandler → 404
     */
    @Operation(summary = "Детали типа документа по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тип документа найден",
                    content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Тип документа не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentTypeResponse> getById(
            @Parameter(description = "ID типа документа", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentTypeService.getById(id));
    }

    /**
     * Создать новый тип документа.
     *
     * Как работает:
     * 1. @Valid проверяет @NotBlank поля (typeCode, typeName)
     * 2. Сервис проверяет уникальность typeCode (409 если занят)
     * 3. Создаёт запись в БД и возвращает 201 Created
     */
    @Operation(summary = "Создать тип документа")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Тип документа создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или typeCode уже существует")
    })
    @PostMapping
    public ResponseEntity<DocumentTypeResponse> create(
            @Valid @RequestBody DocumentTypeRequest request
    ) {
        DocumentTypeResponse created = documentTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить данные типа документа.
     *
     * Как работает:
     * 1. Находит тип по ID (404 если не найден)
     * 2. Обновляет typeName, category, xsdSchemaPath
     * 3. typeCode является бизнес-ключом и не меняется
     */
    @Operation(summary = "Обновить тип документа")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тип документа обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Тип документа не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentTypeRequest request
    ) {
        return ResponseEntity.ok(documentTypeService.update(id, request));
    }

    /**
     * Мягкое удаление типа документа (active → false).
     *
     * Как работает:
     * Тип НЕ удаляется физически — это нарушило бы ссылочную целостность
     * (существующие документы ссылаются на него через document_type_id).
     * Вместо этого устанавливается active = false.
     */
    @Operation(summary = "Удалить тип документа (мягкое)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тип документа деактивирован"),
            @ApiResponse(responseCode = "404", description = "Тип документа не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentTypeService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}