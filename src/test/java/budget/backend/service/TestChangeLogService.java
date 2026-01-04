package budget.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.model.domain.ChangeLog;
import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.Citizen;
import budget.backend.model.domain.user.User;
import budget.backend.repository.ChangeLogRepository;

/**
 * Unit tests for ChangeLogService.
 * Tests approved and non-approved change recording,
 * null input handling, authenticated user presence,
 * and user data consistency validation.
 */
public class TestChangeLogService {
    private ChangeLogRepository repository;
    private ChangeLogService changeLogService;
    private User testUser;
    private String originalDataDir;

    /**
     * Setup temporary directory for file-based repository testing
     * before each test.
     */
    @BeforeEach
    void setup(@TempDir Path tempDir) throws Exception {
        // Backup original system property
        originalDataDir = System.getProperty("budget.data.dir");
        
        // Set temporary directory for file operations
        System.setProperty("budget.data.dir", tempDir.toString());
        
        // Create empty budget-changes.json file
        Path testFile = tempDir.resolve("budget-changes.json");
        Files.writeString(testFile, "[]");

        // Use real repository instead of mock
        repository = new ChangeLogRepository();

        testUser = new Citizen("USER1", "Test User", "hashedPassword");

        // ChangeLogService no longer needs UserAuthenticationService
        changeLogService = new ChangeLogService(repository);
    }

    @AfterEach
    void tearDown() {
        // Restore original system property
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

    @Test
    void testRecordChangeApprovedDoesNotThrow() {
        // Use fullName for requestByName (as done in ChangeRequestService)
        PendingChange change = new PendingChange(
                3, 1, 2025, "Budget Item 1",
                testUser.getFullName(), testUser.getId(),
                1000.0, 1200.0
        );
        change.approve(); // approved

        assertDoesNotThrow(() -> changeLogService.recordChange(change, testUser));

        List<ChangeLog> logs = repository.load();
        assertEquals(1, logs.size());
        ChangeLog log = logs.get(0);
        assertEquals(change.getBudgetItemId(), log.budgetItemId());
        assertEquals(change.getOldValue(), log.oldValue());
        assertEquals(change.getNewValue(), log.newValue());
        assertEquals(testUser.getFullName(), log.actorName());
        assertEquals(testUser.getId(), log.actorId());
    }

    @Test
    void testRecordChangeApprovedWithUserNameMatches() {
        // Test that userName also matches (fallback check)
        PendingChange change = new PendingChange(
                1, 1, 2025, "Budget Item 1",
                testUser.getUserName(), testUser.getId(),
                1000.0, 1200.0
        );
        change.approve();

        assertDoesNotThrow(() -> changeLogService.recordChange(change, testUser));
    }

    @Test
    void testRecordChangeNotApprovedThrows() {
        PendingChange change = new PendingChange(
                2, 2, 2025, "Budget Item 2",
                testUser.getFullName(), testUser.getId(),
                2000.0, 2500.0
        );
        // Not approved

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change, testUser));
        assertEquals("Change is not approved yet", ex.getMessage());
    }

    @Test
    void testRecordChangeNullPendingChangeThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> changeLogService.recordChange(null, testUser));
        assertEquals("PendingChange cannot be null", ex.getMessage());
    }

    @Test
    void testRecordChangeNoAuthenticatedUserThrows() {
        PendingChange change = new PendingChange(
                4, 3, 2025, "Budget Item 3",
                testUser.getFullName(), testUser.getId(),
                3000.0, 3300.0
        );
        change.approve();

        // Pass null user instead of using authService
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change, null));
        assertEquals("No authenticated user present", ex.getMessage());
    }

    @Test
    void testRecordChangeAuthenticatedUserNullUsernameThrows() {
        PendingChange change = new PendingChange(
                5, 4, 2025, "Budget Item 4",
                "dummy", testUser.getId(),
                4000.0, 4500.0
        );
        change.approve();

        // Create user with null username and pass it directly
        User nullUsernameUser = new Citizen(null, "NoName", "hashed");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change, nullUsernameUser));
        assertEquals("Authenticated user has null username", ex.getMessage());
    }

    @Test
    void testRecordChangeAuthenticatedUserNullFullNameThrows() {
        PendingChange change = new PendingChange(
                6, 5, 2025, "Budget Item 5",
                testUser.getFullName(), testUser.getId(),
                5000.0, 5500.0
        );
        change.approve();

        // Create user with null fullName
        User nullFullNameUser = new Citizen("USER2", null, "hashed");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> changeLogService.recordChange(change, nullFullNameUser));
        assertEquals("Authenticated user has null full name", ex.getMessage());
    }

    @Test
    void testRecordChangeUserIdMismatchThrows() {
        PendingChange change = new PendingChange(
                7, 6, 2025, "Budget Item 6",
                testUser.getFullName(), UUID.randomUUID(), // Different user ID
                6000.0, 6500.0
        );
        change.approve();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> changeLogService.recordChange(change, testUser));
        assertEquals("User ID mismatch: change was requested by different user",
                ex.getMessage());
    }

    @Test
    void testRecordChangeRequestNameNullThrows() {
        // Create a change with null requestByName
        // Note: This is tricky since PendingChange constructor doesn't allow null
        // But we test the validation logic
        PendingChange change = new PendingChange(
                8, 7, 2025, "Budget Item 7",
                null, testUser.getId(),
                7000.0, 7500.0
        );
        change.approve();

        // This test verifies that if requestByName were null, it would be caught
        // In practice, PendingChange constructor doesn't allow null, but validation checks for it
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> changeLogService.recordChange(change, testUser));
        assertEquals("Change request name cannot be null", ex.getMessage());
    }

    @Test
    void testRecordChangeUserNameMismatchThrows() {
        PendingChange change = new PendingChange(
                9, 8, 2025, "Budget Item 8",
                "Different Name", testUser.getId(), // Name doesn't match
                8000.0, 8500.0
        );
        change.approve();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> changeLogService.recordChange(change, testUser));
        assertEquals(
                String.format(
                        "User name mismatch: change was requested by '%s' "
                        + "but authenticated user is '%s' (fullName: '%s')",
                        "Different Name",
                        testUser.getUserName(),
                        testUser.getFullName()
                ),
                ex.getMessage()
        );
    }

    @Test
    void testRecordMultipleApprovedChanges() {
        for (int i = 1; i <= 3; i++) {
            PendingChange change = new PendingChange(
                    i, i, 2025, "Budget Item " + i,
                    testUser.getFullName(), testUser.getId(),
                    1000.0 * i, 1200.0 * i
            );
            change.approve();
            changeLogService.recordChange(change, testUser);
        }

        List<ChangeLog> logs = repository.load();
        assertEquals(3, logs.size());
    }
}
