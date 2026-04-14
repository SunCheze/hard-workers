package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.ConstructionObjectRequest;
import ru.artwell.contractor.dto.ConstructionObjectResponse;
import ru.artwell.contractor.service.ConstructionObjectService;

/**
 * REST-контроллер для работы с объектами строительства (construction_objects).
 *
 * CRUD-операции: список, поиск по названию, создание, обновление,
 * деактивация (soft delete).
 */
@Tag(name = "Construction Objects", description = "Объекты строительства: CRUD и поиск")
@RestController
@RequestMapping("/api/construction-objects")
public class ConstructionObjectController {

    private final ConstructionObjectService constructionObjectService;

    public ConstructionObjectController(ConstructionObjectService constructionObjectService) {
        this.constructionObjectService = constructionObjectService;
    }

    @Operation(summary = "Список объектов строительства", description = "Пагинация, опциональный поиск по названию")
    @GetMapping
    public ResponseEntity<Page<ConstructionObjectResponse>> list(
            @Parameter(description = "Поиск по названию (частичное совпадение)")
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(constructionObjectService.list(null, name, null, PageRequest.of(page, size)));
    }

    @Operation(summary = "Объект строительства по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Объект не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionObjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(constructionObjectService.getById(id));
    }

    @Operation(summary = "Создать объект строительства")
    @ApiResponses({
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или дубликат кода")
    })
    @PostMapping
    public ResponseEntity<ConstructionObjectResponse> create(@Valid @RequestBody ConstructionObjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(constructionObjectService.create(request));
    }

    @Operation(summary = "Обновить объект строительства")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Объект не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionObjectResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody ConstructionObjectRequest request) {
        return ResponseEntity.ok(constructionObjectService.update(id, request));
    }

    @Operation(summary = "Деактивировать объект", description = "Soft delete: active=false")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Объект не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        constructionObjectService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
