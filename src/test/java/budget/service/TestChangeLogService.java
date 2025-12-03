package budget.service;

import budget.model.domain.ChangeLog;
import budget.model.domain.PendingChange;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.User;
import budget.repository.ChangeLogRepository;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void testRecordChangeApprovedDoesNotThrow() {
        PendingChange change = new PendingChange(
                1, 2025, "Budget Item 1",
                testUser.getUserName(), testUser.getId(),
                1000.0, 1200.0
        );
        change.approve(); // approved

        assertDoesNotThrow(() -> changeLogService.recordChange(change));

        List<ChangeLog> logs = repository.load();
        assertEquals(1, logs.size());
        ChangeLog log = logs.get(0);
        assertEquals(change.getBudgetItemId(), log.budgetItemId());
        assertEquals(change.getOldValue(), log.oldValue());
        assertEquals(change.getNewValue(), log.newValue());
        assertEquals(testUser.getUserName(), log.actorUserName());
        assertEquals(testUser.getId(), log.actorId());
    }

    @Test
    void testRecordChangeNotApprovedThrows() {
        PendingChange change = new PendingChange(
                2, 2025, "Budget Item 2",
                testUser.getUserName(), testUser.getId(),
                2000.0, 2500.0
        );
        // Not approved

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change));
        assertEquals("Change is not approved yet", ex.getMessage());
    }

    @Test
    void testRecordChangeNullPendingChangeThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> changeLogService.recordChange(null));
        assertEquals("PendingChange cannot be null", ex.getMessage());
    }

    @Test
    void testRecordChangeNoAuthenticatedUserThrows() {
        PendingChange change = new PendingChange(
                3, 2025, "Budget Item 3",
                testUser.getUserName(), testUser.getId(),
                3000.0, 3300.0
        );
        change.approve();

        authService = new UserAuthenticationService(null);
        changeLogService = new ChangeLogService(repository, authService);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change));
        assertEquals("No authenticated user present", ex.getMessage());
    }

    @Test
    void testRecordChangeAuthenticatedUserNullUsernameThrows() {
        PendingChange change = new PendingChange(
                4, 2025, "Budget Item 4",
                "dummy", testUser.getId(),
                4000.0, 4500.0
        );
        change.approve();

        authService = new UserAuthenticationService(null) {
            @Override
            public User getCurrentUser() {
                return new Citizen(null, "NoName", "hashed"); // null username
            }
        };
        changeLogService = new ChangeLogService(repository, authService);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change));
        assertEquals("Authenticated user has null username", ex.getMessage());
    }

    @Test
    void testRecordMultipleApprovedChanges() {
        for (int i = 1; i <= 3; i++) {
            PendingChange change = new PendingChange(
                    i, 2025, "Budget Item " + i,
                    testUser.getUserName(), testUser.getId(),
                    1000.0 * i, 1200.0 * i
            );
            change.approve();
            changeLogService.recordChange(change);
        }

        List<ChangeLog> logs = repository.load();
        assertEquals(3, logs.size());
    }
}
