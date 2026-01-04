package budget.backend.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import budget.backend.model.domain.ChangeLog;

public class TestChangeLogRepository {
    
    private ChangeLogRepository repository;
    private String originalDataDir;
    
    private ChangeLog testLog1;
    private ChangeLog testLog2;
    private ChangeLog testLog3;
    
    private Gson gson;
    
    private UUID userId1;
    private UUID userId2;
    
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // Backup and set data dir so PathsUtil resolves files from tempDir
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());
        
        Path testFilePath = tempDir.resolve("budget-changes.json");
        Files.writeString(testFilePath, "[]");
        
        repository = new ChangeLogRepository();
        
        gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Create test data
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        
        String date1 = LocalDateTime.now().minusDays(2).format(DATE_FORMATTER);
        String date2 = LocalDateTime.now().minusDays(1).format(DATE_FORMATTER);
        String date3 = LocalDateTime.now().format(DATE_FORMATTER);
        
        testLog1 = new ChangeLog(
            1,
            100,
            0.0,
            1000.0,
            date1,
            "John Doe",
            userId1
        );
        
        testLog2 = new ChangeLog(
            2,
            100,
            1000.0,
            1500.0,
            date2,
            "Jane Smith",
            userId2
        );
        
        testLog3 = new ChangeLog(
            3,
            200,
            500.0,
            0.0,
            date3,
            "John Doe",
            userId1
        );
    }
    
    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }
    
    // load() Tests
    
    @Test
    void testLoadEmptyFile() {
        List<ChangeLog> result = repository.load();
        
        assertNotNull(result, "Failure - load should not return null");
        assertTrue(result.isEmpty(), "Failure - should return empty list");
    }
    
    @Test
    void testLoadValidJsonFile() {
        repository.save(testLog1);
        
        List<ChangeLog> result = repository.load();
        
        assertNotNull(result, "Failure - load should not return null");
        assertEquals(1, result.size(), "Failure - should return 1 log");
        assertEquals(1, result.get(0).id(), "Failure - id should match");
        assertEquals(100, result.get(0).budgetItemId(), "Failure - budgetItemId should match");
        assertEquals(0.0, result.get(0).oldValue(), "Failure - oldValue should match");
        assertEquals(1000.0, result.get(0).newValue(), "Failure - newValue should match");
    }
    
    @Test
    void testLoadMultipleRecords() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        List<ChangeLog> result = repository.load();
        
        assertNotNull(result, "Failure - load should not return null");
        assertEquals(3, result.size(), "Failure - should return 3 logs");
    }
    
    @Test
    void testLoadInvalidJson(@TempDir Path tempDir) throws IOException {
        System.setProperty("budget.data.dir", tempDir.toString());
        Path testFile = tempDir.resolve("budget-changes.json");
        Files.writeString(testFile, "{ invalid json }");
        
        assertDoesNotThrow(() -> repository.load(), "Failure - load shouldn't throw");
        List<ChangeLog> result = repository.load();
        
        assertNotNull(result, "Failure - load should not return null");
        assertTrue(result.isEmpty(), "Failure - should return empty list on invalid JSON");
    }
    
    @Test
    void testLoadNullJson(@TempDir Path tempDir) throws IOException {
        System.setProperty("budget.data.dir", tempDir.toString());
        Path testFile = tempDir.resolve("budget-changes.json");
        Files.writeString(testFile, "null");
        
        List<ChangeLog> result = repository.load();
        
        assertNotNull(result, "Failure - load should not return null");
        assertTrue(result.isEmpty(), "Failure - should return empty list for null JSON");
    }
    
    // save() Tests
    
    @Test
    void testSaveNewEntity() {
        repository.save(testLog1);
        
        List<ChangeLog> result = repository.load();
        assertEquals(1, result.size(), "Failure - should have 1 log");
        assertEquals(testLog1.id(), result.get(0).id(), "Failure - id should match");
        assertEquals(testLog1.actorName(), result.get(0).actorName(), "Failure - actorName should match");
        assertEquals(testLog1.oldValue(), result.get(0).oldValue(), "Failure - oldValue should match");
        assertEquals(testLog1.newValue(), result.get(0).newValue(), "Failure - newValue should match");
    }
    
    @Test
    void testSaveMultipleNewEntities() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        List<ChangeLog> result = repository.load();
        assertEquals(3, result.size(), "Failure - should have 3 logs");
    }
    
    @Test
    void testSaveUpdateExistingEntity() {
        repository.save(testLog1);
        
        ChangeLog updatedLog = new ChangeLog(
            1,
            100,
            0.0,
            2000.0,
            LocalDateTime.now().format(DATE_FORMATTER),
            "John Doe Updated",
            testLog1.actorId()
        );
        
        repository.save(updatedLog);
        
        List<ChangeLog> result = repository.load();
        assertEquals(1, result.size(), "Failure - should still have 1 log");
        assertEquals(2000.0, result.get(0).newValue(), "Failure - newValue should be updated");
        assertEquals("John Doe Updated", result.get(0).actorName(), "Failure - actorName should be updated");
    }
    
    @Test
    void testSaveNull() {
        assertDoesNotThrow(() -> repository.save(null), "Failure - saving null shouldn't throw");
        
        List<ChangeLog> result = repository.load();
        assertTrue(result.isEmpty(), "Failure - should have 0 logs after saving null");
    }
    
    // existsById() Tests
    
    @Test
    void testExistsByIdTrue() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        boolean exists = repository.existsById(1);
        
        assertTrue(exists, "Failure - should return true for existing id");
    }
    
    @Test
    void testExistsByIdFalse() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        boolean exists = repository.existsById(999);
        
        assertFalse(exists, "Failure - should return false for non-existing id");
    }
    
    @Test
    void testExistsByIdNull() {
        boolean exists = repository.existsById(null);
        assertFalse(exists, "Failure - should return false for null id");
    }
    
    @Test
    void testExistsByIdEmptyList() {
        boolean exists = repository.existsById(1);
        
        assertFalse(exists, "Failure - should return false for empty list");
    }
    
    // findById() Tests
    
    @Test
    void testFindByIdExisting() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        Optional<ChangeLog> result = repository.findById(2);
        
        assertTrue(result.isPresent(), "Failure - should find existing log");
        assertEquals(2, result.get().id(), "Failure - id should match");
        assertEquals(testLog2.actorName(), result.get().actorName(), "Failure - actorName should match");
        assertEquals(testLog2.budgetItemId(), result.get().budgetItemId(), "Failure - budgetItemId should match");
    }
    
    @Test
    void testFindByIdNonExisting() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        Optional<ChangeLog> result = repository.findById(999);
        
        assertTrue(result.isEmpty(), "Failure - should return empty for non-existing id");
    }
    
    @Test
    void testFindByIdNull() {
        Optional<ChangeLog> result = repository.findById(null);
        assertTrue(result.isEmpty(), "Failure - should return empty for null id");
    }
    
    @Test
    void testFindByIdEmptyList() {
        Optional<ChangeLog> result = repository.findById(1);
        
        assertTrue(result.isEmpty(), "Failure - should return empty for empty list");
    }
    
    // getLogsForItem() Tests
    
    @Test
    void testGetLogsForItemValid() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        List<ChangeLog> result = repository.getLogsForItem(100);
        
        assertEquals(2, result.size(), "Failure - should return 2 logs for item 100");
        assertTrue(result.stream().allMatch(log -> log.budgetItemId() == 100), 
            "Failure - all logs should have budgetItemId 100");
        assertTrue(result.contains(testLog1), "Failure - should contain testLog1");
        assertTrue(result.contains(testLog2), "Failure - should contain testLog2");
    }
    
    @Test
    void testGetLogsForItemNoMatching() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        List<ChangeLog> result = repository.getLogsForItem(999);
        
        assertTrue(result.isEmpty(), "Failure - should return empty list for non-matching item");
    }
    
    @Test
    void testGetLogsForItemNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.getLogsForItem(null),
            "Failure - should throw for null itemId"
        );
        assertEquals("Item ID cannot be null", exception.getMessage(), 
            "Failure - exception message should match");
    }
    
    // getLogsByUser() Tests
    
    @Test
    void testGetLogsByUserValid() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        List<ChangeLog> result = repository.getLogsByUser(userId1);
        
        assertEquals(2, result.size(), "Failure - should return 2 logs for userId1");
        assertTrue(result.stream().allMatch(log -> log.actorId().equals(userId1)), 
            "Failure - all logs should have actorId userId1");
        assertTrue(result.contains(testLog1), "Failure - should contain testLog1");
        assertTrue(result.contains(testLog3), "Failure - should contain testLog3");
    }
    
    @Test
    void testGetLogsByUserNoMatching() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        UUID randomUserId = UUID.randomUUID();
        List<ChangeLog> result = repository.getLogsByUser(randomUserId);
        
        assertTrue(result.isEmpty(), "Failure - should return empty list for non-matching user");
    }
    
    @Test
    void testGetLogsByUserNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.getLogsByUser(null),
            "Failure - should throw for null userId"
        );
        assertEquals("User ID cannot be null", exception.getMessage(), 
            "Failure - exception message should match");
    }
    
    // delete() Tests
    
    @Test
    void testDeleteExisting() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        repository.delete(testLog1);
        
        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size(), "Failure - should have 2 logs after delete");
        assertFalse(result.stream().anyMatch(log -> log.id() == testLog1.id()), 
            "Failure - should not contain deleted log");
        assertTrue(result.contains(testLog2), "Failure - should contain testLog2");
        assertTrue(result.contains(testLog3), "Failure - should contain testLog3");
    }
    
    @Test
    void testDeleteNonExisting() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        ChangeLog nonExistingLog = new ChangeLog(
            999,
            300,
            100.0,
            200.0,
            LocalDateTime.now().format(DATE_FORMATTER),
            "Non Existing",
            UUID.randomUUID()
        );
        
        assertDoesNotThrow(() -> repository.delete(nonExistingLog), 
            "Failure - deleting non-existing log shouldn't throw");
        
        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size(), "Failure - should still have 2 logs");
    }
    
    @Test
    void testDeleteNull() {
        repository.save(testLog1);
        repository.save(testLog2);
        
        assertDoesNotThrow(() -> repository.delete(null), 
            "Failure - deleting null shouldn't throw");
        
        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size(), "Failure - should still have 2 logs");
    }
    
    @Test
    void testDeleteLastEntity() {
        repository.save(testLog1);
        
        repository.delete(testLog1);
        
        List<ChangeLog> result = repository.load();
        assertTrue(result.isEmpty(), "Failure - should have empty list after deleting last entity");
    }
    
    // generateId() Tests
    
    @Test
    void testGenerateIdEmptyList() {
        int newId = repository.generateId();
        
        assertEquals(1, newId, "Failure - first id should be 1");
    }
    
    @Test
    void testGenerateIdWithExistingLogs() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        int newId = repository.generateId();
        
        assertEquals(4, newId, "Failure - next id should be max + 1");
    }
    
    @Test
    void testGenerateIdWithNonSequentialIds() {
        ChangeLog log1 = new ChangeLog(1, 100, 0.0, 100.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 1", UUID.randomUUID());
        ChangeLog log2 = new ChangeLog(5, 100, 100.0, 200.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 2", UUID.randomUUID());
        ChangeLog log3 = new ChangeLog(3, 100, 200.0, 300.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 3", UUID.randomUUID());
        
        repository.save(log1);
        repository.save(log2);
        repository.save(log3);
        
        int newId = repository.generateId();
        
        assertEquals(6, newId, "Failure - should return max id + 1");
    }
    
    // Other Tests
    
    @Test
    void testValueTracking() {
        ChangeLog changeLog = new ChangeLog(
            1,
            100,
            500.0,
            750.0,
            LocalDateTime.now().format(DATE_FORMATTER),
            "Budget Manager",
            UUID.randomUUID()
        );
        
        repository.save(changeLog);
        
        Optional<ChangeLog> result = repository.findById(1);
        assertTrue(result.isPresent(), "Failure - should find saved log");
        assertEquals(500.0, result.get().oldValue(), "Failure - oldValue should match");
        assertEquals(750.0, result.get().newValue(), "Failure - newValue should match");
    }
    
    @Test
    void testConcurrentOperations() throws InterruptedException {
        Thread thread1 = new Thread(() -> repository.save(testLog1));
        Thread thread2 = new Thread(() -> repository.save(testLog2));
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size(), "Failure - concurrent saves should both succeed");
    }
    
    @Test
    void testDateFormat() {
        String expectedDate = "2024-12-15 14:30:00";
        ChangeLog changeLog = new ChangeLog(
            1,
            100,
            100.0,
            200.0,
            expectedDate,
            "Test Actor",
            UUID.randomUUID()
        );
        
        repository.save(changeLog);
        
        Optional<ChangeLog> result = repository.findById(1);
        assertTrue(result.isPresent(), "Failure - should find saved log");
        assertEquals(expectedDate, result.get().submittedDate(), "Failure - date should match");
    }
    
    @Test
    void testFullCrudCycle() {
        // Create
        repository.save(testLog1);
        assertTrue(repository.existsById(testLog1.id()), "Failure - save() not working properly");
        
        // Read
        List<ChangeLog> logs = repository.load();
        assertEquals(1, logs.size(), "Failure - load() not working properly");
        Optional<ChangeLog> readLog = repository.findById(testLog1.id());
        assertTrue(readLog.isPresent(), "Failure - findById() not working properly");
        assertEquals(testLog1.actorName(), readLog.get().actorName(), 
            "Failure - actorName should match");
        
        // Update
        ChangeLog updatedLog = new ChangeLog(
            testLog1.id(),
            testLog1.budgetItemId(),
            testLog1.oldValue(),
            3000.0,
            LocalDateTime.now().format(DATE_FORMATTER),
            "Updated Actor",
            testLog1.actorId()
        );
        repository.save(updatedLog);
        Optional<ChangeLog> afterUpdate = repository.findById(testLog1.id());
        assertTrue(afterUpdate.isPresent(), "Failure - findById() after update not working");
        assertEquals(3000.0, afterUpdate.get().newValue(), 
            "Failure - update() not working properly");
        
        // Delete
        repository.delete(afterUpdate.get());
        assertFalse(repository.existsById(testLog1.id()), 
            "Failure - delete() not working properly");
    }
    
    @Test
    void testMultipleLogs() {
        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);
        
        List<ChangeLog> logs = repository.load();
        assertEquals(3, logs.size(), "Failure - should have 3 logs");
        
        repository.delete(testLog1);
        assertTrue(repository.existsById(testLog2.id()), "Failure - testLog2 should exist");
        assertTrue(repository.existsById(testLog3.id()), "Failure - testLog3 should exist");
        
        repository.delete(testLog2);
        repository.delete(testLog3);
        logs = repository.load();
        assertEquals(0, logs.size(), "Failure - all logs should be deleted");
    }

    @Test
    void testLoadWhenFileNotFound(@TempDir Path emptyTempDir) throws IOException{
        // Set a temp directory that doesn't have budget-changes.json
        // This will make the external file not exist
        // And since we're in test context, classpath resource won't exist either
        System.setProperty("budget.data.dir", emptyTempDir.toString());
        Path resourceFile = Paths.get("target/classes/budget-changes.json");
        Path renamedFile = Paths.get("target/classes/budget-changes-temp.json");
        try {
            
            if (Files.exists(resourceFile)) {
                Files.move(resourceFile, renamedFile, StandardCopyOption.REPLACE_EXISTING);
            }
            List<ChangeLog> result = repository.load();
            
            assertNotNull(result, "Failure - load should not return null even when file not found");
            assertTrue(result.isEmpty(), "Failure - should return empty list when file not found");
        } finally {
            // Always restore the file, even if test fails
            if (Files.exists(renamedFile)) {
                Files.move(renamedFile, resourceFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}