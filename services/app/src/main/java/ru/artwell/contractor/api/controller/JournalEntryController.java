package services.app.src.main.java.ru.artwell.contractor.api.controller;

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
import ru.artwell.contractor.dto.JournalEntryRequest;
import ru.artwell.contractor.dto.JournalEntryResponse;
import ru.artwell.contractor.service.JournalEntryService;

/**
 * REST-контроллер для работы с журналом документов (journal_entries).
 *
 * Журнал — это append-only лог действий: stamp, утверждение, отклонение,
 * смена статуса. Записи нельзя редактировать или удалять — только создавать
 * и читать. Это обеспечивает целостность истории документа.
 *
 * Эндпоинты:
 *   GET  /api/documents/{documentId}/journal          → записи по документу
 *   GET  /api/users/{userId}/journal                   → записи по пользователю
 *   GET  /api/journal/{id}                             → детали записи
 *   POST /api/journal                                  → добавить запись
 */
@Tag(name = "Journal", description = "Журнал документов: append-only лог действий (stamp, approval, status change)")
@RestController
@RequestMapping("/api")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    /**
     * Получить записи журнала для документа (от новых к старым).
     *
     * Пример: GET /api/documents/1/journal?page=0&size=20
     */
    @Operation(summary = "Журнал документа", description = "Все записи по документу, пагинация")
    @GetMapping("/documents/{documentId}/journal")
    public ResponseEntity<Page<JournalEntryResponse>> listByDocument(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Номер страницы (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(journalEntryService.listByDocument(documentId, PageRequest.of(page, size)));
    }

    /**
     * Получить записи, выполненные конкретным пользователем.
     */
    @Operation(summary = "Журнал пользователя", description = "Записи журнала, выполненные пользователем")
    @GetMapping("/users/{userId}/journal")
    public ResponseEntity<Page<JournalEntryResponse>> listByUser(
            @Parameter(description = "ID пользователя")
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(journalEntryService.listByUser(userId, PageRequest.of(page, size)));
    }

    /**
     * Детали записи журнала.
     */
    @Operation(summary = "Детали записи журнала")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись найдена"),
            @ApiResponse(responseCode = "404", description = "Запись не найдена")
    })
    @GetMapping("/journal/{id}")
    public ResponseEntity<JournalEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(journalEntryService.getById(id));
    }

    /**
     * Добавить запись в журнал (append-only, нельзя редактировать/удалять).
     *
     * Пример: POST /api/journal
     * { "documentId": 1, "action": "APPROVED", "comment": "Согласовано", "performedByUserId": 5, "journalType": "APPROVAL" }
     */
    @Operation(summary = "Добавить запись в журнал")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Запись добавлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Документ или пользователь не найден")
    })
    @PostMapping("/journal")
    public ResponseEntity<JournalEntryResponse> create(@Valid @RequestBody JournalEntryRequest request) {
        JournalEntryResponse created = journalEntryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
