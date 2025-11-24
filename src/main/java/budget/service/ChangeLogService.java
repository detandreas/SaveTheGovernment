package budget.service;

import budget.model.domain.BudgetItem;
import budget.model.domain.ChangeLog;
import budget.repository.ChangeLogRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service responsible for storing and retrieving {@link ChangeLog}
 * entries. This service is stateless and delegates persistence to
 * {@link ChangeLogRepository}.
 */
public class ChangeLogService {
    /** Repository for log persistence. */
    private final ChangeLogRepository changeLogRepository;

    /** Authentication service used to obtain the current user. */
    private final UserAuthenticationService authService;

    /** Date-time format used for submitted changes. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a ChangeLogService.
     *
     * @param repository repository handling ChangeLog persistence
     * @param auth       authentication service
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
        "This allows testability and shared state across service instances."
    )
    public ChangeLogService(ChangeLogRepository repository,
            UserAuthenticationService auth) {
        this.changeLogRepository = repository;
        this.authService = auth;
    }

    /**
     * Records a change for the given budget item.
     *
     * @param item     the budget item modified
     * @param oldValue the previous value
     * @param newValue the new value
     */
    public void recordChange(BudgetItem item,
            double oldValue, double newValue) {
        validateRecordChangeInputs(item);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        int logId = changeLogRepository.generateId();
        var currentUser = authService.getCurrentUser();
        ChangeLog log = new ChangeLog(
                logId,
                item.getId(),
                oldValue,
                newValue,
                timestamp,
                currentUser.getUserName(),
                currentUser.getId()
        );

        changeLogRepository.save(log);
    }

    /**
     * Validates inputs for recordChange method.
     *
     * @param item the budget item to validate
     * @throws IllegalArgumentException if item is null
     * @throws IllegalStateException if no authenticated user exists
     * or user has invalid data
     */
    private void validateRecordChangeInputs(BudgetItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "BudgetItem cannot be null");
        }

        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException(
                    "No authenticated user present");
        }

        if (currentUser.getUserName() == null) {
            throw new IllegalStateException(
                    "Authenticated user has null username");
        }
    }

    /**
     * Retrieves all ChangeLog entries.
     *
     * @return list of logs
     */
    public List<ChangeLog> getAllLogs() {
        List<ChangeLog> logs = changeLogRepository.load();
        return logs != null ? logs : List.of();
    }

    /**
     * Retrieves logs related to a specific budget item.
     *
     * @param itemId ID of the budget item
     * @return list of logs for the item
     * @throws IllegalArgumentException if itemId is null
     */
    public List<ChangeLog> getLogsForItem(Integer itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException(
                    "Item ID cannot be null");
        }

        return changeLogRepository
                .load()
                .stream()
                .filter(log -> log.budgetItemId() == itemId)
                .toList();
    }

    /**
     * Retrieves logs created by a specific user.
     *
     * @param userId ID of the user
     * @return list of logs created by the user
     * @throws IllegalArgumentException if userId is null
     */
    public List<ChangeLog> getLogsByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null");
        }

        return changeLogRepository
                .load()
                .stream()
                .filter(log -> log.actorId() == userId)
                .toList();
    }
}
