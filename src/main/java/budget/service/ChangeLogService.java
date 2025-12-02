package budget.service;

import budget.model.domain.ChangeLog;
import budget.model.domain.PendingChange;
import budget.model.enums.Status;
import budget.repository.ChangeLogRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
     * @param change  the proposed change
     */
    public void recordChange(PendingChange change) {
        if (change.getStatus() != Status.APPROVED) {
            throw new IllegalStateException("Change is not approved yet");
        }
        validateRecordChangeInputs(change);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        int logId = changeLogRepository.generateId();
        var currentUser = authService.getCurrentUser();
        ChangeLog log = new ChangeLog(
                logId,
                change.getBudgetItemId(),
                change.getOldValue(),
                change.getNewValue(),
                timestamp,
                currentUser.getUserName(),
                currentUser.getId()
        );

        changeLogRepository.save(log);
    }

    /**
     * Validates inputs for recordChange method.
     *
     * @param change the Pending change to log
     * @throws IllegalArgumentException if change is null
     * @throws IllegalStateException if no authenticated user exists
     * or user has invalid data
     */
    private void validateRecordChangeInputs(PendingChange change) {
        if (change == null) {
            throw new IllegalArgumentException(
                    "PendingChange cannot be null");
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
}
