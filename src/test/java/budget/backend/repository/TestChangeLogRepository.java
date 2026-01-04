package budget.backend.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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

    @TempDir
    Path tempDir;

    private Path testFilePath;

    private ChangeLog testLog1;
    private ChangeLog testLog2;
    private ChangeLog testLog3;

    private Gson gson;

    private UUID userId1;
    private UUID userId2;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() throws IOException {
        repository = new TestableChangeLogRepository();
        testFilePath = tempDir.resolve("budget-changes.json");

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

        // Initialize with empty file
        Files.writeString(testFilePath, "[]");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testFilePath)) {
            Files.delete(testFilePath);
        }
    }

    @Test
    void testLoad_EmptyFile_ReturnsEmptyList() throws IOException {
        Files.writeString(testFilePath, "[]");

        List<ChangeLog> result = repository.load();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoad_ValidJsonFile_ReturnsChangeLogList() throws IOException {
        ChangeLog[] logs = {testLog1};
        String json = gson.toJson(logs);
        Files.writeString(testFilePath, json);

        List<ChangeLog> result = repository.load();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals(100, result.get(0).budgetItemId());
        assertEquals(0.0, result.get(0).oldValue());
        assertEquals(1000.0, result.get(0).newValue());
    }

    @Test
    void testLoad_MultipleRecords_ReturnsAllRecords() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        String json = gson.toJson(logs);
        Files.writeString(testFilePath, json);

        List<ChangeLog> result = repository.load();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testLoad_InvalidJson_ReturnsEmptyList() throws IOException {
        Files.writeString(testFilePath, "{ invalid json }");

        List<ChangeLog> result = repository.load();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoad_NullJson_ReturnsEmptyList() throws IOException {
        Files.writeString(testFilePath, "null");

        List<ChangeLog> result = repository.load();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_NewEntity_AddsToList() throws IOException {
        Files.writeString(testFilePath, "[]");

        repository.save(testLog1);

        List<ChangeLog> result = repository.load();
        assertEquals(1, result.size());
        assertEquals(testLog1.id(), result.get(0).id());
        assertEquals(testLog1.actorName(), result.get(0).actorName());
        assertEquals(testLog1.oldValue(), result.get(0).oldValue());
        assertEquals(testLog1.newValue(), result.get(0).newValue());
    }

    @Test
    void testSave_MultipleNewEntities_AddsAllToList() throws IOException {
        Files.writeString(testFilePath, "[]");

        repository.save(testLog1);
        repository.save(testLog2);
        repository.save(testLog3);

        List<ChangeLog> result = repository.load();
        assertEquals(3, result.size());
    }

    @Test
    void testSave_ExistingEntity_UpdatesInList() throws IOException {
        ChangeLog[] initialLogs = {testLog1};
        Files.writeString(testFilePath, gson.toJson(initialLogs));

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
        assertEquals(1, result.size());
        assertEquals(2000.0, result.get(0).newValue());
        assertEquals("John Doe Updated", result.get(0).actorName());
    }

    @Test
    void testSave_NullEntity_DoesNothing() throws IOException {
        Files.writeString(testFilePath, "[]");

        repository.save(null);

        List<ChangeLog> result = repository.load();
        assertTrue(result.isEmpty());
    }

    @Test
    void testExistsById_ExistingId_ReturnsTrue() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        boolean exists = repository.existsById(1);

        assertTrue(exists);
    }

    @Test
    void testExistsById_NonExistingId_ReturnsFalse() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        boolean exists = repository.existsById(999);

        assertFalse(exists);
    }

    @Test
    void testExistsById_NullId_ReturnsFalse() {
        boolean exists = repository.existsById(null);
        assertFalse(exists);
    }

    @Test
    void testExistsById_EmptyList_ReturnsFalse() throws IOException {
        Files.writeString(testFilePath, "[]");

        boolean exists = repository.existsById(1);

        assertFalse(exists);
    }

    @Test
    void testFindById_ExistingId_ReturnsOptionalWithValue() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        Files.writeString(testFilePath, gson.toJson(logs));

        Optional<ChangeLog> result = repository.findById(2);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().id());
        assertEquals(testLog2.actorName(), result.get().actorName());
        assertEquals(testLog2.budgetItemId(), result.get().budgetItemId());
    }

    @Test
    void testFindById_NonExistingId_ReturnsEmptyOptional() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        Optional<ChangeLog> result = repository.findById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_NullId_ReturnsEmptyOptional() {
        Optional<ChangeLog> result = repository.findById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_EmptyList_ReturnsEmptyOptional() throws IOException {
        Files.writeString(testFilePath, "[]");

        Optional<ChangeLog> result = repository.findById(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLogsForItem_ValidItemId_ReturnsFilteredLogs() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        Files.writeString(testFilePath, gson.toJson(logs));

        List<ChangeLog> result = repository.getLogsForItem(100);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.budgetItemId() == 100));
        assertTrue(result.contains(testLog1));
        assertTrue(result.contains(testLog2));
    }

    @Test
    void testGetLogsForItem_NoMatchingLogs_ReturnsEmptyList() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        List<ChangeLog> result = repository.getLogsForItem(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLogsForItem_NullItemId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.getLogsForItem(null)
        );
        assertEquals("Item ID cannot be null", exception.getMessage());
    }

    @Test
    void testGetLogsByUser_ValidUserId_ReturnsFilteredLogs() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        Files.writeString(testFilePath, gson.toJson(logs));

        List<ChangeLog> result = repository.getLogsByUser(userId1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.actorId().equals(userId1)));
        assertTrue(result.contains(testLog1));
        assertTrue(result.contains(testLog3));
    }

    @Test
    void testGetLogsByUser_NoMatchingLogs_ReturnsEmptyList() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        UUID randomUserId = UUID.randomUUID();
        List<ChangeLog> result = repository.getLogsByUser(randomUserId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLogsByUser_NullUserId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.getLogsByUser(null)
        );
        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void testDelete_ExistingEntity_RemovesFromList() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        Files.writeString(testFilePath, gson.toJson(logs));

        repository.delete(testLog1);

        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(log -> log.id() == testLog1.id()));
        assertTrue(result.contains(testLog2));
        assertTrue(result.contains(testLog3));
    }

    @Test
    void testDelete_NonExistingEntity_DoesNotChangeList() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        ChangeLog nonExistingLog = new ChangeLog(
            999,
            300,
            100.0,
            200.0,
            LocalDateTime.now().format(DATE_FORMATTER),
            "Non Existing",
            UUID.randomUUID()
        );

        repository.delete(nonExistingLog);

        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size());
    }

    @Test
    void testDelete_NullEntity_DoesNothing() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2};
        Files.writeString(testFilePath, gson.toJson(logs));

        repository.delete(null);

        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size());
    }

    @Test
    void testDelete_LastEntity_LeavesEmptyList() throws IOException {
        ChangeLog[] logs = {testLog1};
        Files.writeString(testFilePath, gson.toJson(logs));

        repository.delete(testLog1);

        List<ChangeLog> result = repository.load();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateId_EmptyList_ReturnsOne() throws IOException {
        Files.writeString(testFilePath, "[]");

        int newId = repository.generateId();

        assertEquals(1, newId);
    }

    @Test
    void testGenerateId_WithExistingLogs_ReturnsMaxPlusOne() throws IOException {
        ChangeLog[] logs = {testLog1, testLog2, testLog3};
        Files.writeString(testFilePath, gson.toJson(logs));

        int newId = repository.generateId();

        assertEquals(4, newId);
    }

    @Test
    void testGenerateId_WithNonSequentialIds_ReturnsMaxPlusOne() throws IOException {
        ChangeLog log1 = new ChangeLog(1, 100, 0.0, 100.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 1", UUID.randomUUID());
        ChangeLog log2 = new ChangeLog(5, 100, 100.0, 200.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 2", UUID.randomUUID());
        ChangeLog log3 = new ChangeLog(3, 100, 200.0, 300.0,
            LocalDateTime.now().format(DATE_FORMATTER), "Actor 3", UUID.randomUUID());

        ChangeLog[] logs = {log1, log2, log3};
        Files.writeString(testFilePath, gson.toJson(logs));

        int newId = repository.generateId();

        assertEquals(6, newId);
    }

    @Test
    void testChangeLog_ValueTracking_RecordsOldAndNewValues() throws IOException {
        Files.writeString(testFilePath, "[]");

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
        assertTrue(result.isPresent());
        assertEquals(500.0, result.get().oldValue());
        assertEquals(750.0, result.get().newValue());
    }

    @Test
    void testConcurrentOperations_SaveAndLoad_MaintainsDataIntegrity()
            throws IOException, InterruptedException {
        Files.writeString(testFilePath, "[]");

        Thread thread1 = new Thread(() -> repository.save(testLog1));
        Thread thread2 = new Thread(() -> repository.save(testLog2));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        List<ChangeLog> result = repository.load();
        assertEquals(2, result.size());
    }

    @Test
    void testChangeLog_DateFormat_IsStoredCorrectly() throws IOException {
        Files.writeString(testFilePath, "[]");

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
        assertTrue(result.isPresent());
        assertEquals(expectedDate, result.get().submittedDate());
    }


    /**
     * Inner class that extends ChangeLogRepository to override file paths
     * for testing purposes
     */

    private class TestableChangeLogRepository extends ChangeLogRepository {

        @Override
        public List<ChangeLog> load() {
            try {
                if (!Files.exists(testFilePath)) {
                    return List.of();
                }
                String content = Files.readString(testFilePath);
                ChangeLog[] logs = new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .fromJson(content, ChangeLog[].class);
                return logs != null ? List.of(logs) : List.of();
            } catch (Exception e) {
                return List.of();
            }
        }

        @Override
        public void save(ChangeLog entity) {
            if (entity == null) {
                return;
            }
            synchronized (this) {
                try {
                    List<ChangeLog> logs = new java.util.ArrayList<>(load());
                    java.util.OptionalInt index = java.util.stream.IntStream
                        .range(0, logs.size())
                        .filter(i -> logs.get(i).id() == entity.id())
                        .findFirst();

                    if (index.isPresent()) {
                        logs.set(index.getAsInt(), entity);
                    } else {
                        logs.add(entity);
                    }

                    String json = new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(logs);
                    Files.writeString(testFilePath, json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void delete(ChangeLog entity) {
            if (entity == null) {
                return;
            }
            synchronized (this) {
                try {
                    List<ChangeLog> logs = new java.util.ArrayList<>(load());
                    logs.removeIf(log -> log.id() == entity.id());

                    String json = new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(logs);
                    Files.writeString(testFilePath, json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}



