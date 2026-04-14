package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.UserEntity;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Репозиторий для работы с таблицей users.
 * Расширен методами поиска по роли, организации, признаку активности и текстовому поиску.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    /** Все активные пользователи с заданной ролью */
    List<UserEntity> findByRoleAndActiveTrue(String role);

    Page<UserEntity> findByRoleAndActiveTrue(String role, Pageable pageable);

    /** Все активные пользователи организации */
    Page<UserEntity> findByOrganization_IdAndActiveTrue(Long organizationId, Pageable pageable);

    /** Текстовый поиск по username, fullName или email */
    @Query("""
            SELECT u FROM UserEntity u
            WHERE u.active = true
              AND (:role IS NULL OR u.role = :role)
              AND (:orgId IS NULL OR u.organization.id = :orgId)
              AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<UserEntity> search(@Param("role") String role,
                            @Param("orgId") Long organizationId,
                            @Param("query") String query,
                            Pageable pageable);

    /** Проверка уникальности username */
    boolean existsByUsername(String username);
}
