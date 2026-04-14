package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.DocumentVersionDetailResponse;
import ru.artwell.contractor.service.DocumentVersionService;

import java.util.List;

/**
 * REST-контроллер для работы с версиями документов.
 *
 * Каждый документ имеет историю версий: при первом upload создаётся версия 1,
 * при каждой замене (replace) — следующая версия. Версии образуют связный
 * список через previous_version_id.
 *
 * Контроллер предоставляет:
 * — просмотр списка всех версий документа;
 * — просмотр деталей конкретной версии;
 * — откат к указанной версии (создаёт новую версию с содержимым старой).
 *
 * Общая схема:
 *   GET  /api/documents/{documentId}/versions                     → список версий
 *   GET  /api/documents/{documentId}/versions/{versionId}        → детали версии
 *   POST /api/documents/{documentId}/versions/{versionId}/revert → откат к версии
 *
 * Скачивание XML находится в DocumentController:
 *   GET  /api/documents/{versionId}/xml → download XML file
 */
@Tag(name = "Document Versions", description = "Версии документов: просмотр истории, детали, откат")
@RestController
@RequestMapping("/api/documents/{documentId}/versions")
public class DocumentVersionController {

    /**
     * Сервис версий документов — содержит логику получения списка версий,
     * деталей конкретной версии и отката к указанной версии.
     */
    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    /**
     * Получить список всех версий документа.
     *
     * Как работает:
     * 1. Извлекает {documentId} из пути URL
     * 2. Проверяет существование документа (404 если не найден)
     * 3. Запрашивает все версии из БД, отсортированные от новых к старым
     * 4. Маппит каждую версию в DocumentVersionDetailResponse
     *
     * Пример ответа (2 версии):
     * [
     *   { id: 5, versionNumber: 2, uploadedAt: "2024-03-15T10:30:00", ... },
     *   { id: 1, versionNumber: 1, uploadedAt: "2024-03-10T09:00:00", ... }
     * ]
     */
    @Operation(summary = "Список версий документа",
            description = "Все версии документа от новых к старым, включая метаданные и статус валидации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список версий"),
            @ApiResponse(responseCode = "404", description = "Документ не найден")
    })
    @GetMapping
    public ResponseEntity<List<DocumentVersionDetailResponse>> listVersions(
            @Parameter(description = "ID документа (не версии!)", example = "1")
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(documentVersionService.listByDocument(documentId));
    }

    /**
     * Получить детали конкретной версии документа.
     *
     * Как работает:
     * 1. Извлекает {documentId} и {versionId} из пути URL
     * 2. Загружает версию с join fetch документа и типа документа
     * 3. Возвращает полную информацию: метаданные, статус валидации,
     *    кто загрузил, размер файла, ссылка на предыдущую версию
     */
    @Operation(summary = "Детали версии документа")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Детали версии",
                    content = @Content(schema = @Schema(implementation = DocumentVersionDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Версия не найдена")
    })
    @GetMapping("/{versionId}")
    public ResponseEntity<DocumentVersionDetailResponse> getVersion(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Parameter(description = "ID версии", example = "5")
            @PathVariable Long versionId
    ) {
        return ResponseEntity.ok(documentVersionService.getById(versionId));
    }

    /**
     * Откатить документ к указанной версии.
     *
     * Как работает:
     * 1. Находит целевую версию по {versionId}
     * 2. Находит текущую (последнюю) версию документа
     * 3. Создаёт НОВУЮ версию с тем же XML-файлом, что и целевая
     * 4. Номер новой версии = текущий + 1
     * 5. Обновляет currentVersion в документе
     *
     * Важно: откат НЕ удаляет промежуточные версии! История сохраняется
     * для audit-целей. Откат — это создание «восстановительной» версии.
     *
     * Пример: документ имеет версии 1→2→3. Откат к версии 1 создаёт версию 4
     * с содержимым версии 1. Цепочка: 1→2→3→4.
     */
    @Operation(summary = "Откатить документ к указанной версии",
            description = "Создаёт новую версию с содержимым указанной. История сохраняется.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Откат выполнен, возвращена новая версия",
                    content = @Content(schema = @Schema(implementation = DocumentVersionDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Версия или документ не найдены")
    })
    @PostMapping("/{versionId}/revert")
    public ResponseEntity<DocumentVersionDetailResponse> revertToVersion(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Parameter(description = "ID версии, к которой нужно откатиться", example = "1")
            @PathVariable Long versionId
    ) {
        return ResponseEntity.ok(documentVersionService.revertTo(versionId));
    }
}
