package budget.backend.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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



