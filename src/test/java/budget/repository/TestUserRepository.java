package budget.repository;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.google.gson.JsonObject;

class UserRepositoryTest {
    private static Path tempdir;
    private static Path userFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private UserRepository repository;

    @BeforeAll
    static void setupAll() throws IOException {
        tempDir = Files.createTempDirectory("usersTest");
        usersFile = tempDir.resolve("users.json");
        System.setProperty("budget.data.dir", tempDir.toString());
    }

    @BeforeEach
    void setup() throws IOException {
        repository = new UserRepository();
        Files.deleteIfExists(usersFile);
        Files.createFile(usersFile);
        writeEmptyJson();
    }

    private static void writeEmptyJson() throws IOEception {
        try (Writer w = Files.newBufferedWriter(usersFile, StandardCharsets.UTF_8)) {
            JsonObject obj = new JsonObject();
            GSON.toJson(obj, w);
        }
    }
}