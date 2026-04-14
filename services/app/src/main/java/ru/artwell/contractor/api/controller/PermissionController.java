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
import ru.artwell.contractor.dto.RoleDocumentPermissionRequest;
import ru.artwell.contractor.dto.RoleDocumentPermissionResponse;
import ru.artwell.contractor.service.RoleDocumentPermissionService;

import java.util.List;

@Tag(name = "Permissions", description = "Права доступа: матрица ролей x типы документов (view, create, edit, delete, approve)")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final RoleDocumentPermissionService permissionService;

    public PermissionController(RoleDocumentPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "Список разрешений", description = "Фильтр по роли ИЛИ типу документа")
    @GetMapping
    public ResponseEntity<List<RoleDocumentPermissionResponse>> list(
            @Parameter(description = "Фильтр по роли") @RequestParam(required = false) String role,
            @Parameter(description = "Фильтр по ID типа документа") @RequestParam(required = false) Long documentTypeId) {
        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(permissionService.listByRole(role));
        } else if (documentTypeId != null) {
            return ResponseEntity.ok(permissionService.listByDocumentType(documentTypeId));
        }
        throw new IllegalArgumentException("Specify either 'role' or 'documentTypeId' filter");
    }

    @Operation(summary = "Разрешение по роли и типу документа")
    @GetMapping("/by-role-type")
    public ResponseEntity<RoleDocumentPermissionResponse> getByRoleAndType(
            @RequestParam String role, @RequestParam Long documentTypeId) {
        return ResponseEntity.ok(permissionService.getByRoleAndDocumentType(role, documentTypeId));
    }

    @Operation(summary = "Создать разрешение")
    @PostMapping
    public ResponseEntity<RoleDocumentPermissionResponse> create(@Valid @RequestBody RoleDocumentPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @Operation(summary = "Обновить разрешение")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDocumentPermissionResponse> update(@PathVariable Long id, @Valid @RequestBody RoleDocumentPermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @Operation(summary = "Удалить разрешение")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok().build();
    }
}