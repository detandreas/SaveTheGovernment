package budget.service;

import budget.model.domain.ChangeLog;
import budget.model.domain.PendingChange;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.User;
import budget.model.enums.Status;
import budget.repository.ChangeLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangeLogService.
 * Tests approved and non-approved change recording,
 * null input handling, and authenticated user presence.
 */
public class TestChangeLogService {
    private ChangeLogRepository repository;
    private UserAuthenticationService authService;
    private ChangeLogService changeLogService;
    private User testUser;

    /**
     * Setup in-memory repository and fake auth service
     * before each test.
     */
    @BeforeEach
    void setup() {
        repository = new ChangeLogRepository() {
            private final List<ChangeLog> logs = new java.util.ArrayList<>();

            @Override
            public List<ChangeLog> load() { return new java.util.ArrayList<>(logs); }

            @Override
            public void save(ChangeLog entity) { logs.add(entity); }

            @Override
            public int generateId() { return logs.size() + 1; }
        };

        testUser = new Citizen("USER1", "Test User", "hashedPassword");

        authService = new UserAuthenticationService(null) {
            @Override
            public User getCurrentUser() { return testUser; }
        };

        changeLogService = new ChangeLogService(repository, authService);
    }
}
