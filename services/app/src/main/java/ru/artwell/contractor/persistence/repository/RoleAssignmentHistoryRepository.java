package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.RoleAssignmentHistoryEntity;

import java.util.List;

//Репозиторий для работы с таблицей role_assignment_history.

public interface RoleAssignmentHistoryRepository extends JpaRepository<RoleAssignmentHistoryEntity, Long> {

    /** Полная история смены ролей конкретного пользователя, от новых к старым */
    List<RoleAssignmentHistoryEntity> findByUser_IdOrderByChangedAtDesc(Long userId);
}
