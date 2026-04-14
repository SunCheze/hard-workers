package ru.artwell.contractor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.*;
import ru.artwell.contractor.persistence.entity.OrganizationEntity;
import ru.artwell.contractor.persistence.entity.RoleAssignmentHistoryEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.OrganizationRepository;
import ru.artwell.contractor.persistence.repository.RoleAssignmentHistoryRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Сервис для работы с пользователями и ролями.
 *
 * Обеспечивает:
 * — CRUD-операции над пользователями;
 * — смену роли с записью в role_assignment_history;
 * — деактивацию пользователя;
 * — получение истории смены ролей.
 *
 * Пароль хранится в виде BCrypt-хэша, никогда не возвращается в API.
 */
@Service
public class UserService {

    /** Допустимые роли в системе */
    private static final List<String> VALID_ROLES = List.of(
            "ADMIN", "CUSTOMER", "TECH_CUSTOMER", "CONTRACTOR",
            "SUB_CONTRACTOR", "DESIGNER", "SUPERVISOR"
    );

    private final ZoneId applicationZoneId;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleAssignmentHistoryRepository roleHistoryRepository;

    public UserService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                       UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       RoleAssignmentHistoryRepository roleHistoryRepository) {
        this.applicationZoneId = applicationZoneId;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.roleHistoryRepository = roleHistoryRepository;
    }

    // ─── Создание ───────────────────────────────────────────────

    /**
     * Создаёт нового пользователя.
     *
     * Проверяет уникальность username, валидирует роль, хэширует пароль BCrypt, привязывает к организации.
     */
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        // Проверка уникальности username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("User with username '" + request.getUsername() + "' already exists");
        }

        // Валидация роли
        validateRole(request.getRole());

        // Привязка к организации (опционально)
        OrganizationEntity org = null;
        if (request.getOrganizationId() != null) {
            org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Organization not found: " + request.getOrganizationId()));
        }

        String passwordHash = new BCryptPasswordEncoder().encode(request.getPassword());

        UserEntity entity = new UserEntity(
                request.getUsername(),
                passwordHash,
                request.getFullName(),
                request.getRole() != null ? request.getRole() : "CONTRACTOR",
                org,
                request.getEmail(),
                true
        );
        UserEntity saved = userRepository.save(entity);
        return toResponse(saved);
    }

    // ─── Чтение ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Поиск пользователей с фильтрами и пагинацией.
     * Все параметры фильтров опциональны — пустые/null пропускаются.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> list(String role, Long organizationId, String search, Pageable pageable) {
        Page<UserEntity> page;
        if (search != null && !search.isBlank()) {
            page = userRepository.search(role, organizationId, search.trim(), pageable);
        } else if (role != null && !role.isBlank()) {
            page = userRepository.findByRoleAndActiveTrue(role, pageable);
        } else if (organizationId != null) {
            page = userRepository.findByOrganization_IdAndActiveTrue(organizationId, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    // ─── Обновление ─────────────────────────────────────────────

    /**
     * Обновление профиля (fullName, email, organization).
     * Пароль и роль не меняются через этот метод — для этого есть отдельные эндпоинты.
     */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity entity = findOrThrow(id);

        if (request.getFullName() != null) {
            entity.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getOrganizationId() != null) {
            OrganizationEntity org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Organization not found: " + request.getOrganizationId()));
            entity.setOrganization(org);
        }

        return toResponse(userRepository.save(entity));
    }

    // ─── Смена роли ─────────────────────────────────────────────

    /**
     * Смена роли пользователя.
     *
     * Записывает предыдущую роль в role_assignment_history вместе с
     * инициатором изменения и причиной. Это позволяет aud
     * отследить, кто, когда и почему поменял роль.
     *
     * @param userId ID пользователя
     * @param request новая роль + причина
     * @param changedByUsername username инициатора (для записи в историю)
     */
    @Transactional
    public UserResponse changeRole(Long userId, RoleChangeRequest request, String changedByUsername) {
        UserEntity entity = findOrThrow(userId);
        validateRole(request.getRole());

        String oldRole = entity.getRole();
        String newRole = request.getRole();

        // Запись в историю
        UserEntity changedBy = userRepository.findByUsername(changedByUsername)
                .orElse(null);

        roleHistoryRepository.save(new RoleAssignmentHistoryEntity(
                entity,
                oldRole,
                newRole,
                changedBy,
                LocalDateTime.now(applicationZoneId)
        ));

        entity.setRole(newRole);
        return toResponse(userRepository.save(entity));
    }

    // ─── Деактивация ────────────────────────────────────────────

    /** Мягкое удаление: isActive = false. Пользователь не может авторизоваться. */
    @Transactional
    public void deactivate(Long id) {
        UserEntity entity = findOrThrow(id);
        entity.setActive(false);
        userRepository.save(entity);
    }

    // ─── История ролей ──────────────────────────────────────────

    /** Получить полную историю смены ролей пользователя (от новых к старым) */
    @Transactional(readOnly = true)
    public List<RoleAssignmentRecordResponse> getRoleHistory(Long userId) {
        findOrThrow(userId); // проверяем, что пользователь существует
        return roleHistoryRepository.findByUser_IdOrderByChangedAtDesc(userId).stream()
                .map(h -> new RoleAssignmentRecordResponse(
                        h.getOldRole(),
                        h.getNewRole(),
                        h.getChangedBy() != null ? h.getChangedBy().getUsername() : null,
                        h.getChangedAt(),
                        null // reason пока не хранится в entity
                ))
                .toList();
    }

    // ─── Вспомогательные методы ──────────────────────────────────

    UserEntity findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /** Проверяет, что роль входит в список допустимых */
    private void validateRole(String role) {
        if (role != null && !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Valid roles: " + VALID_ROLES);
        }
    }

    /** Маппинг Entity → DTO (с вложенной организацией) */
    private UserResponse toResponse(UserEntity entity) {
        OrganizationResponse orgDto = null;
        if (entity.getOrganization() != null) {
            OrganizationEntity o = entity.getOrganization();
            orgDto = new OrganizationResponse(
                    o.getId(), o.getOrgName(), o.getOrgShortName(),
                    o.getOrgType(), o.getInn(), o.getKpp(),
                    o.getLegalAddress(), o.isActive()
            );
        }
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getFullName(),
                entity.getRole(),
                orgDto,
                entity.getEmail(),
                entity.isActive(),
                entity.getLastLogin()
        );
    }

    // ─── Исключения ─────────────────────────────────────────────

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}