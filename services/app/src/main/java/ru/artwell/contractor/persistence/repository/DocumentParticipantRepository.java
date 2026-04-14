package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.DocumentParticipantEntity;

public interface DocumentParticipantRepository extends JpaRepository<DocumentParticipantEntity, Long> {
}