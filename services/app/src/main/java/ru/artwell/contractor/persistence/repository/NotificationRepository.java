package ru.artwell.contractor.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.NotificationEntity;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<NotificationEntity> findByUser_IdAndReadByUserFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUser_IdAndReadByUserFalse(Long userId);

    Optional<NotificationEntity> findByIdAndUser_Id(Long id, Long userId);
}