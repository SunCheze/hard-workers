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
import ru.artwell.contractor.dto.OrganizationRequest;
import ru.artwell.contractor.dto.OrganizationResponse;
import ru.artwell.contractor.service.OrganizationService;

/**
 * REST-контроллер для работы с организациями (organizations).
 *
 * CRUD-операции: список, поиск по названию, создание, обновление,
 * деактивация (soft delete).
 */
@Tag(name = "Organizations", description = "Организации: CRUD и поиск")
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Operation(summary = "Список организаций", description = "Получить все организации с пагинацией, опциональный поиск по названию")
    @GetMapping
    public ResponseEntity<Page<OrganizationResponse>> list(
            @Parameter(description = "Поиск по названию (частичное совпадение)")
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationService.list(name, PageRequest.of(page, size)));
    }

    @Operation(summary = "Организация по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @Operation(summary = "Создать организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или дубликат ИНН")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request));
    }

    @Operation(summary = "Обновить организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    @Operation(summary = "Деактивировать организацию", description = "Soft delete: active=false")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        organizationService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
