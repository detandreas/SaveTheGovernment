package budget.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public class TestBudgetRepository {
    private BudgetRepository repository;
    private String originalDataDir;

    private Path budgetJson;
    private Path ministryJson;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // backup and set data dir so PathsUtil resolves files from tempDir
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        budgetJson = tempDir.resolve("budget.json");
        ministryJson = tempDir.resolve("bill-ministry-map.json");

        // create minimal files (empty JSON structures) so repository.load doesn't
        // return early due to missing resources
        Files.writeString(budgetJson, "{}", StandardCharsets.UTF_8);
        Files.writeString(ministryJson, "{ \"byId\": {}, \"byName\": {} }", StandardCharsets.UTF_8);

        repository = new BudgetRepository();
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

}