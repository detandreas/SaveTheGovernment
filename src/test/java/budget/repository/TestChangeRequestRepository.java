package budget.repository;

import budget.model.enums.Status;
import budget.model.domain.PendingChange;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;;


public class TestChangeRequestRepository {

    private ChangeRequestRepository repository;
    // backup της αρχικής τιμής
    private String originalDataDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException{
        //Το junit φτιαχνει temporary directory που διαγράφει 
        //μετα απο κάθε test
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        Path testFile = tempDir.resolve("pending-changes.json");
        Files.writeString(testFile, "[]");

        repository = new ChangeRequestRepository();
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);

        }
    }

    private PendingChange createTestChange(
        int budgetItemId,
        String requestByName,
        double oldValue,
        double newValue
    ) {
        return new PendingChange(
            budgetItemId,
            requestByName,
            UUID.randomUUID(),
            oldValue,
            newValue
        );
    }

    //Tests για load()

    @Test
    void testLoadEmptyFile() {
        List<PendingChange> changes = repository.load();
        assertNotNull(changes, "Failure - load should not return null");
        assertTrue(changes.isEmpty(), "Failure - should return empty list");
    }

    @Test
    void testLoadWithData() {
        PendingChange change1 = createTestChange(1, "TestName", 1000.0, 1100.0);
        PendingChange change2 = createTestChange(2, "TestName2", 1500.0, 1600.0);
        repository.save(change1);
        repository.save(change2);

        List<PendingChange> changes = repository.load();
        assertEquals(2, changes.size(), "Failure - should load 2 changes");
        assertTrue(changes
                        .stream()
                        .anyMatch(change -> change.getId() == change1.getId()),
                         "Failure - data not loaded correctly");
        assertTrue(changes
                        .stream()
                        .anyMatch(change -> change.getId() == change2.getId()),
                        "Failure - data not loaded correctly");
    }

    //Tests για save()

    @Test
    void testSaveNewChange() {
        PendingChange change = createTestChange(1, "User", 100.0, 200.0);
        repository.save(change);

        List<PendingChange> changes = repository.load();
        PendingChange readChange = changes.get(0);
        //Μελλοντικα θα μπορουσαμε να δημιουργήσουμε equals
        //και να αποφευχθει ολοι οι ελεγχοι.
        assertEquals(change.getId(), readChange.getId(), 
                 "Failure - IDs should match");
        assertEquals(change.getBudgetItemId(), readChange.getBudgetItemId(),
                     "Failure - budgetItemId should match");
        assertEquals(change.getRequestByName(), readChange.getRequestByName(),
                     "Failure - requestByName should match");
        assertEquals(change.getRequestById(), readChange.getRequestById(),
                     "Failure - requestById should match");
        assertEquals(change.getOldValue(), readChange.getOldValue(),
                     "Failure - oldValue should match");
        assertEquals(change.getNewValue(), readChange.getNewValue(),
                     "Failure - newValue should match");
        assertEquals(change.getStatus(), readChange.getStatus(),
                     "Failure - status should match");
        assertEquals(change.getSubmittedDate(), readChange.getSubmittedDate(),
                    "Failure - submittedDate should match");
    }

    @Test
    void testSaveUpdateChange() {
        PendingChange change = createTestChange(1, "User", 100, 200);
        repository.save(change);

        change.approve();
        repository.save(change);

        List<PendingChange> changes = repository.load();
        assertEquals(1, changes.size(),
        "Failure - save should update existing change");
        PendingChange read_change = changes.get(0);
        assertEquals(Status.APPROVED, read_change.getStatus(),
        "Failure - status not updated correctly");
    }

    @Test
    void testSaveNull() {
        assertDoesNotThrow(() -> repository.save(null),
        "Failure - saving null shouldn't throw Exception");
        List<PendingChange> changes = repository.load();
        assertEquals(0, changes.size(),
        "Failure - should not have null changes");
    }

    //Tests για existsById()

    @Test
    void testExistsByIdTrue() {
        PendingChange change = createTestChange(1, "User", 100.0, 150.0);
        repository.save(change);

        int id = change.getId();
        assertTrue(repository.existsById(id),
        "Failure - should return true for existing id");
    }

    @Test
    void testExistsByIdFalse() {
        PendingChange change = createTestChange(1, "User", 100.0, 150.0);
        repository.save(change);

        assertFalse(repository.existsById(0),
        "Failure - should return false for Non-existing id");
    }

    @Test
    void testExistsByIdNull() {
        assertFalse(repository.existsById(null),
        "Failure - should return false for null id");
    }

    //Tests για delete()

    @Test
    void testDeleteExisting() {
        PendingChange change = createTestChange(1, "User", 100.0, 150.0);
        repository.save(change);
        repository.delete(change);
        List<PendingChange> changes = repository.load();
        assertTrue(changes.isEmpty(),"Failure - Should be empty");
    }
    
    @Test
    void testDeleteNonExisting() {
        PendingChange change = createTestChange(1, "User", 100.0, 150.0);
        assertDoesNotThrow(() -> repository.delete(change),
        "Failure - should not throw for deleting non-existing change");
    }

    @Test
    void testDeleteNull() {
        assertDoesNotThrow(() -> repository.delete(null),
        "Failure - should not throw for deleting null");
    }
}
