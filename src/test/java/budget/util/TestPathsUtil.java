package budget.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathsUtil {

    private static final String DATA_DIR_PROPERTY = "budget.data.dir";
    private String originalDataDir;

    private static final String[] FILE_NAMES = {
        "budget.json",
        "bill-ministry-map.json",
        "users.json",
        "pending-changes.json",
        "budget-changes.json"
    };

    private static final List<ResourceEntry> RESOURCES = List.of(
        new ResourceEntry(FILE_NAMES[0], () -> PathsUtil.getBudgetInputStream()),
        new ResourceEntry(FILE_NAMES[1], () -> PathsUtil.getBillMinistryMapInputStream()),
        new ResourceEntry(FILE_NAMES[2], () -> PathsUtil.getUsersInputStream()),
        new ResourceEntry(FILE_NAMES[3], () -> PathsUtil.getPendingChangesInputStream()),
        new ResourceEntry(FILE_NAMES[4], () -> PathsUtil.getBudgetChangesInputStream())
    );

    @BeforeEach
    void setUp() {
        originalDataDir = System.getProperty(DATA_DIR_PROPERTY);
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty(DATA_DIR_PROPERTY);
        } else {
            System.setProperty(DATA_DIR_PROPERTY, originalDataDir);
        }
    }

    // resolveDataFile() Tests

    @Test
    void resolveDataFile_WhenPropertyNotSet() {
        // ShouldReturnDefaultPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = Paths.get("src", "main", "resources", "test.json");
        assertEquals(expected, result, "Failure - wrong path when property not set");
    }

    @Test
    void resolveDataFile_WhenPropertyIsBlank() {
        // ShouldReturnDefaultPath
        System.setProperty(DATA_DIR_PROPERTY, "   ");
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = Paths.get("src", "main", "resources", "test.json");
        assertEquals(expected, result, "Failure - wrong path when property is blank");
    }

    @Test
    void resolveDataFile_WhenPropertyIsDirectory(@TempDir Path tempDir) {
        // ShouldResolveFileName
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = tempDir.resolve("test.json");
        assertEquals(expected, result, "Failure - wrong path when property is directory");
    }

    @Test
    void resolveDataFile_WhenPropertyIsFile(@TempDir Path tempDir)
    throws IOException {
        // ShouldReturnBasePath
        Path testFile = tempDir.resolve("datafile");
        Files.createFile(testFile);
        System.setProperty(DATA_DIR_PROPERTY, testFile.toString());
        Path result = PathsUtil.resolveDataFile("test.json");
        // test.json is not resolved
        assertEquals(testFile, result, "Failure - wrong path when property is file");
    }

    // openDataStream() Tests

    @Test
    void openDataStream_WhenExternalFileExists(@TempDir Path tempDir)
    throws IOException {
        // ShouldReturnExternalStream
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path testFile = tempDir.resolve("test.json");
        String testContent = "{\"test\": \"data\"}";
        Files.writeString(testFile, testContent);

        try (InputStream stream = PathsUtil.openDataStream("test.json", "/nonexistent.json")) {
            assertNotNull(stream, "Failure - stream should not be null when external file exists");
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(testContent, content, "Failure - wrong content when external file exists");
        }
    }

    @Test
    void openDataStream_WhenExternalFileNotExists()
    throws IOException {
        // ShouldFallbackToClasspath
        System.clearProperty(DATA_DIR_PROPERTY);
        try (InputStream stream = PathsUtil.openDataStream("nonexistent.json", PathsUtil.BUDGET_RESOURCE)) {
            assertNotNull(stream, "Failure - stream should not be null when falling back to classpath");
        }
    }

    @Test
    void openDataStream_WhenExternalFileIsDirectory(@TempDir Path tempDir)
    throws IOException {
        // ShouldFallbackToClasspath
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path testFile = tempDir.resolve("test.json");
        // Create a directory with the same name - this will make !Files.isDirectory() return false
        Files.createDirectory(testFile);

        try (InputStream stream = PathsUtil.openDataStream("test.json", PathsUtil.BUDGET_RESOURCE)) {
            assertNotNull(stream, "Failure - stream should not be null when external file is directory");
            // Should load from classpath, not external file
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.length() > 0, "Failure - content should not be empty when external file is directory");
        } finally {
            if (Files.exists(testFile) && Files.isDirectory(testFile)) {
                Files.delete(testFile);
            }
        }
    }
    
    @Test
    @DisabledOnOs(OS.WINDOWS)
    void openDataStream_WhenExternalFileNotReadable(@TempDir Path tempDir)
    throws IOException {
        // ShouldFallbackToClasspath
        // Note: This test is disabled on Windows because setReadable() doesn't work
        // reliably on Windows due to different file permission models
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path testFile = tempDir.resolve("test.json");
        Files.createFile(testFile);
        testFile.toFile().setReadable(false);

        try (InputStream stream = PathsUtil.openDataStream("test.json", PathsUtil.BUDGET_RESOURCE)) {
            assertNotNull(stream, "Failure - stream should not be null when external file is not readable");
            // Should load from classpath, not external file
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.length() > 0, "Failure - content should not be empty when external file is not readable");
        } finally {
            testFile.toFile().setReadable(true);
        }
    }

    // get*WritablePath() Tests

    @Test
    void getBudgetWritablePath() {
        // ShouldReturnCorrectPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBudgetWritablePath();
        Path expected = Paths.get("src", "main", "resources", "budget.json");
        assertEquals(expected, result, "Failure - wrong budget writable path");
    }

    @Test
    void getBillMinistryWritablePath() {
        // ShouldReturnCorrectPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBillMinistryWritablePath();
        Path expected = Paths.get("src", "main", "resources", "bill-ministry-map.json");
        assertEquals(expected, result, "Failure - wrong bill ministry writable path");
    }

    @Test
    void getUsersWritablePath() {
        // ShouldReturnCorrectPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getUsersWritablePath();
        Path expected = Paths.get("src", "main", "resources", "users.json");
        assertEquals(expected, result, "Failure - wrong users writable path");
    }

    @Test
    void getPendingChangesWritablePath() {
        // ShouldReturnCorrectPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getPendingChangesWritablePath();
        Path expected = Paths.get("src", "main", "resources", "pending-changes.json");
        assertEquals(expected, result, "Failure - wrong pending changes writable path");
    }

    @Test
    void getBudgetChangesWritablePath() {
        // ShouldReturnCorrectPath
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBudgetChangesWritablePath();
        Path expected = Paths.get("src", "main", "resources", "budget-changes.json");
        assertEquals(expected, result, "Failure - wrong budget changes writable path");
    }

    // get*InputStream() Tests 

    @Test
    void shouldLoadAllResourcesFromClasspath() throws IOException {
        System.clearProperty(DATA_DIR_PROPERTY);
        for (ResourceEntry entry : RESOURCES) {
            try (InputStream inputStream = entry.loader().get()) {
                assertNotNull(inputStream, () -> "Δεν βρέθηκε resource: " + entry.name());
                final String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(content.length() > 0, () -> "Κενό resource: " + entry.name());
            }
        }
    }

    @Test
    void shouldLoadAllResourcesFromDataDir(@TempDir Path tempDir)
    throws IOException {
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        for (String fileName : FILE_NAMES) {
            Path testFile = tempDir.resolve(fileName);
            Files.writeString(testFile, "{\"test\": \"data\"}");
        }

        for (ResourceEntry entry : RESOURCES) {
            try (InputStream inputStream = entry.loader().get()) {
                assertNotNull(inputStream, () -> "Δεν βρέθηκε resource: " + entry.name());
                final String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(content.length() > 0, () -> "Κενό resource: " + entry.name());
            }
        }
    }

    @Test
    void shouldFallbackToClasspathWhenExternalFileMissing(
                                    @TempDir Path tempDir) throws IOException {
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        // Don't create files - should fallback to classpath

        for (ResourceEntry entry : RESOURCES) {
            try (InputStream inputStream = entry.loader().get()) {
                assertNotNull(inputStream, () -> "Δεν βρέθηκε resource: " + entry.name());
            }
        }
    }

    // Constants Tests

    @Test
    void constants_ShouldHaveCorrectValues() {
        assertEquals("/budget.json", PathsUtil.BUDGET_RESOURCE, "Failure - wrong BUDGET_RESOURCE constant");
        assertEquals("/bill-ministry-map.json", PathsUtil.BILL_MINISTRY_MAP_RESOURCE, "Failure - wrong BILL_MINISTRY_MAP_RESOURCE constant");
        assertEquals("/users.json", PathsUtil.USERS_RESOURCE, "Failure - wrong USERS_RESOURCE constant");
        assertEquals("/pending-changes.json", PathsUtil.PENDING_CHANGES_RESOURCE, "Failure - wrong PENDING_CHANGES_RESOURCE constant");
        assertEquals("/budget-changes.json", PathsUtil.BUDGET_CHANGES_RESOURCE, "Failure - wrong BUDGET_CHANGES_RESOURCE constant");
    }

    private record ResourceEntry(String name, ResourceSupplier loader) { }

    @FunctionalInterface
    private interface ResourceSupplier {
        InputStream get();
    }
}
