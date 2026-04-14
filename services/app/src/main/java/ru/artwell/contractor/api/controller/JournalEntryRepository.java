package services.app.src.main.java.ru.artwell.contractor.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.JournalEntryEntity;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntryEntity, Long> {
    Page<JournalEntryEntity> findByDocument_IdOrderByPerformedAtDesc(Long documentId, Pageable pageable);
    Page<JournalEntryEntity> findByPerformedBy_IdOrderByPerformedAtDesc(Long userId, Pageable pageable);
    Page<JournalEntryEntity> findByJournalTypeAndDocument_IdOrderByPerformedAtDesc(String journalType, Long documentId, Pageable pageable);
    Optional<JournalEntryEntity> findByIdAndDocument_Id(Long id, Long documentId);
}