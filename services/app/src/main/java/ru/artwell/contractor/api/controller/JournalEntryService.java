package services.app.src.main.java.ru.artwell.contractor.api.controller;

@Service
public class JournalEntryService {
    // append-only — записи нельзя редактировать или удалять
    // 4 метода: listByDocument(), listByUser(), getById(), create()
    // Внутренний NotFoundException extends RuntimeException
    // findOrThrow(), findDocumentOrThrow(), findUserOrThrow(), toResponse()
}