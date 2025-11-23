package budget.service;

import budget.model.domain.BudgetItem;
import budget.model.domain.ChangeLog;
import budget.repository.ChangeLogRepository;

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
     * @throws IllegalArgumentException if any argument is null
     */
    public ChangeLogService(ChangeLogRepository repository,
            UserAuthenticationService auth) {

        if (repository == null) {
            throw new IllegalArgumentException(
                    "ChangeLogRepository cannot be null");
        }
        if (auth == null) {
            throw new IllegalArgumentException(
                    "UserAuthenticationService cannot be null");
        }

        this.changeLogRepository = repository;
        this.authService = auth;
    }
}
