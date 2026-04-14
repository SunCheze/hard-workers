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
import ru.artwell.contractor.dto.WorkVolumeRequest;
import ru.artwell.contractor.dto.WorkVolumeResponse;
import ru.artwell.contractor.service.WorkVolumeService;

/**
 * REST-контроллер для работы с объёмами работ (work_volumes).
 *
 * Управляет привязкой видов работ к документам: тип работы, объём,
 * единица измерения, статус утверждения.
 *
 * Эндпоинты:
 *   GET    /api/documents/{documentId}/work-volumes              → список по документу
 *   GET    /api/construction-objects/{objectId}/work-volumes    → список по объекту
 *   POST   /api/work-volumes                                     → создать
 *   GET    /api/work-volumes/{id}                                → детали
 *   PUT    /api/work-volumes/{id}                                → обновить
 *   POST   /api/work-volumes/{id}/approve                        → утвердить
 *   DELETE /api/work-volumes/{id}                                → удалить
 */
@Tag(name = "Work Volumes", description = "Объёмы работ: привязка видов работ к документам, утверждение")
@RestController
@RequestMapping("/api")
public class WorkVolumeController {

    private final WorkVolumeService workVolumeService;

    public WorkVolumeController(WorkVolumeService workVolumeService) {
        this.workVolumeService = workVolumeService;
    }

    @Operation(summary = "Объёмы работ по документу")
    @GetMapping("/documents/{documentId}/work-volumes")
    public ResponseEntity<Page<WorkVolumeResponse>> listByDocument(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(workVolumeService.listByDocument(documentId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Объёмы работ по объекту строительства")
    @GetMapping("/construction-objects/{objectId}/work-volumes")
    public ResponseEntity<Page<WorkVolumeResponse>> listByConstructionObject(
            @PathVariable Long objectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(workVolumeService.listByConstructionObject(objectId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Создать объём работ")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Объём создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PostMapping("/work-volumes")
    public ResponseEntity<WorkVolumeResponse> create(@Valid @RequestBody WorkVolumeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workVolumeService.create(request));
    }

    @Operation(summary = "Детали объёма работ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найден"),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    @GetMapping("/work-volumes/{id}")
    public ResponseEntity<WorkVolumeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workVolumeService.getById(id));
    }

    @Operation(summary = "Обновить объём работ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлён"),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    @PutMapping("/work-volumes/{id}")
    public ResponseEntity<WorkVolumeResponse> update(@PathVariable Long id, @Valid @RequestBody WorkVolumeRequest request) {
        return ResponseEntity.ok(workVolumeService.update(id, request));
    }

    @Operation(summary = "Утвердить объём работ", description = "Устанавливает approved=true с указанием кто утвердил")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Утверждён"),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    @PostMapping("/work-volumes/{id}/approve")
    public ResponseEntity<WorkVolumeResponse> approve(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя, утверждающего объём") @RequestParam Long approvedByUserId) {
        return ResponseEntity.ok(workVolumeService.approve(id, approvedByUserId));
    }

    @Operation(summary = "Удалить объём работ")
    @DeleteMapping("/work-volumes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workVolumeService.delete(id);
        return ResponseEntity.ok().build();
    }
}

