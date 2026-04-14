package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.RoleDocumentPermissionEntity;

import java.util.List;
import java.util.Optional;

public interface RoleDocumentPermissionRepository extends JpaRepository<RoleDocumentPermissionEntity, Long> {

    List<RoleDocumentPermissionEntity> findByRole(String role);

    List<RoleDocumentPermissionEntity> findByDocumentType_Id(Long documentTypeId);

    Optional<RoleDocumentPermissionEntity> findByRoleAndDocumentType_Id(String role, Long documentTypeId);

    boolean existsByRoleAndDocumentType_Id(String role, Long documentTypeId);
}

