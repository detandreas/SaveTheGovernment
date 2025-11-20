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
    void resolveDataFile_WhenPropertyNotSet_ShouldReturnDefaultPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = Paths.get("src", "main", "resources", "test.json");
        assertEquals(expected, result);
    }

    @Test
    void resolveDataFile_WhenPropertyIsBlank_ShouldReturnDefaultPath() {
        System.setProperty(DATA_DIR_PROPERTY, "   ");
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = Paths.get("src", "main", "resources", "test.json");
        assertEquals(expected, result);
    }

    @Test
    void resolveDataFile_WhenPropertyIsDirectory_ShouldResolveFileName(@TempDir Path tempDir) {
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path result = PathsUtil.resolveDataFile("test.json");
        Path expected = tempDir.resolve("test.json");
        assertEquals(expected, result);
    }

    @Test
    void resolveDataFile_WhenPropertyIsFile_ShouldReturnBasePath(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("datafile");
        Files.createFile(testFile);
        System.setProperty(DATA_DIR_PROPERTY, testFile.toString());
        Path result = PathsUtil.resolveDataFile("test.json");
        assertEquals(testFile, result);
    }

    // openDataStream() Tests

    @Test
    void openDataStream_WhenExternalFileExists_ShouldReturnExternalStream(@TempDir Path tempDir) throws IOException {
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path testFile = tempDir.resolve("test.json");
        String testContent = "{\"test\": \"data\"}";
        Files.writeString(testFile, testContent);

        try (InputStream stream = PathsUtil.openDataStream("test.json", "/nonexistent.json")) {
            assertNotNull(stream);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(testContent, content);
        }
    }

    @Test
    void openDataStream_WhenExternalFileNotExists_ShouldFallbackToClasspath()
    throws IOException {
        System.clearProperty(DATA_DIR_PROPERTY);
        try (InputStream stream = PathsUtil.openDataStream("nonexistent.json", PathsUtil.BUDGET_RESOURCE)) {
            assertNotNull(stream);
        }
    }

    @Test
    void openDataStream_WhenExternalFileNotReadable_ShouldFallbackToClasspath(@TempDir Path tempDir) throws IOException {
        System.setProperty(DATA_DIR_PROPERTY, tempDir.toString());
        Path testFile = tempDir.resolve("test.json");
        Files.createFile(testFile);
        testFile.toFile().setReadable(false);

        try (InputStream stream = PathsUtil.openDataStream("test.json", PathsUtil.BUDGET_RESOURCE)) {
            assertNotNull(stream);
            // Should load from classpath, not external file
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.length() > 0);
        } finally {
            testFile.toFile().setReadable(true);
        }
    }

    // get*WritablePath() Tests

    @Test
    void getBudgetWritablePath_ShouldReturnCorrectPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBudgetWritablePath();
        Path expected = Paths.get("src", "main", "resources", "budget.json");
        assertEquals(expected, result);
    }

    @Test
    void getBillMinistryWritablePath_ShouldReturnCorrectPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBillMinistryWritablePath();
        Path expected = Paths.get("src", "main", "resources", "bill-ministry-map.json");
        assertEquals(expected, result);
    }

    @Test
    void getUsersWritablePath_ShouldReturnCorrectPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getUsersWritablePath();
        Path expected = Paths.get("src", "main", "resources", "users.json");
        assertEquals(expected, result);
    }

    @Test
    void getPendingChangesWritablePath_ShouldReturnCorrectPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getPendingChangesWritablePath();
        Path expected = Paths.get("src", "main", "resources", "pending-changes.json");
        assertEquals(expected, result);
    }

    @Test
    void getBudgetChangesWritablePath_ShouldReturnCorrectPath() {
        System.clearProperty(DATA_DIR_PROPERTY);
        Path result = PathsUtil.getBudgetChangesWritablePath();
        Path expected = Paths.get("src", "main", "resources", "budget-changes.json");
        assertEquals(expected, result);
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
    void shouldLoadAllResourcesFromDataDir(@TempDir Path tempDir) throws IOException {
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
    void shouldFallbackToClasspathWhenExternalFileMissing(@TempDir Path tempDir) throws IOException {
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
        assertEquals("/budget.json", PathsUtil.BUDGET_RESOURCE);
        assertEquals("/bill-ministry-map.json", PathsUtil.BILL_MINISTRY_MAP_RESOURCE);
        assertEquals("/users.json", PathsUtil.USERS_RESOURCE);
        assertEquals("/pending-changes.json", PathsUtil.PENDING_CHANGES_RESOURCE);
        assertEquals("/budget-changes.json", PathsUtil.BUDGET_CHANGES_RESOURCE);
    }

    private record ResourceEntry(String name, ResourceSupplier loader) { }

    @FunctionalInterface
    private interface ResourceSupplier {
        InputStream get();
    }
}
