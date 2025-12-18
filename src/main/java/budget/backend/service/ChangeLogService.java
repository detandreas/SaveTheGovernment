package budget.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;

import budget.backend.model.domain.ChangeLog;
import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Status;
import budget.backend.repository.ChangeLogRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.ObservableList;

/**
 * Service responsible for storing and retrieving {@link ChangeLog}
 * entries. This service is stateless and delegates persistence to
 * {@link ChangeLogRepository}.
 */
public class ChangeLogService {
    /** Repository for log persistence. */
    private final ChangeLogRepository changeLogRepository;

    /** Date-time format used for submitted changes. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a ChangeLogService.
     *
     * @param repository repository handling ChangeLog persistence
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
        "This allows testability and shared state across service instances."
    )
    public ChangeLogService(ChangeLogRepository repository) {
        this.changeLogRepository = repository;
    }

    /**
     * Records a change for the given budget item.
     *
     * @param change  the proposed change
     * @param user the user who proposed the change
     */
    public void recordChange(PendingChange change, User user) {
        if (change == null) {
            throw new IllegalArgumentException(
                    "PendingChange cannot be null");
        }

        if (change.getStatus() != Status.APPROVED) {
            throw new IllegalStateException("Change is not approved yet");
        }
        validateRecordChangeInputs(change, user);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        int logId = changeLogRepository.generateId();
        ChangeLog log = new ChangeLog(
                logId,
                change.getBudgetItemId(),
                change.getOldValue(),
                change.getNewValue(),
                timestamp,
                user.getFullName(),
                user.getId()
        );

        changeLogRepository.save(log);
    }

    /**
     * Validates inputs for recordChange method.
     *
     * @param change the Pending change to log
     * @param user the user who proposed the change
     * @throws IllegalArgumentException if change is null
     * @throws IllegalStateException if no authenticated user exists
     * or user has invalid data
     */
    private void validateRecordChangeInputs(PendingChange change, User user) {
        if (user == null) {
            throw new IllegalStateException(
                    "No authenticated user present");
        }

        if (user.getUserName() == null) {
            throw new IllegalStateException(
                    "Authenticated user has null username");
        }

        if (user.getFullName() == null) {
            throw new IllegalStateException(
                    "Authenticated user has null full name");
        }

        if (!user.getId().equals(change.getRequestById())) {
            throw new IllegalArgumentException(
                    "User ID mismatch: change was requested by different user");
        }

        String changeRequesterName = change.getRequestByName();
        if (changeRequesterName == null) {
            throw new IllegalArgumentException(
                    "Change request name cannot be null");
        }

        boolean nameMatches = changeRequesterName.equals(user.getFullName())
                || changeRequesterName.equals(user.getUserName());
                //αμα ταιριάζει με το username το δεχόμαστε

        if (!nameMatches) {
            throw new IllegalArgumentException(
                    String.format(
                            "User name mismatch: change was requested by '%s' "
                            + "but authenticated user is '%s' (fullName: '%s')",
                            changeRequesterName,
                            user.getUserName(),
                            user.getFullName()
                    ));
        }
    }

    public ObservableList<ChangeLog> getAllChangeLogsSortedByDate() {
        return changeLogRepository.load().stream()
                .sorted(Comparator.comparing((ChangeLog log) -> 
                    LocalDateTime.parse(
                        log.submittedDate(),
                        FORMATTER)).reversed())
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(), 
                    FXCollections::observableArrayList
                ));
    }
}
