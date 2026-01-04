package budget.backend.repository;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import budget.backend.model.domain.user.Citizen;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;

class TestUserRepository {

    private String originalDataDir;
    private UserRepository repository;
    private Path usersFile;

    private Citizen c1;
    private GovernmentMember gm;
    private PrimeMinister pm;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        usersFile = tempDir.resolve("users.json");
        Files.writeString(usersFile, "{}", StandardCharsets.UTF_8);

        repository = new UserRepository();
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

    @BeforeEach
    void getSampleData() {
        c1 = new Citizen("User1", "DET USER1", "123");
        gm = new GovernmentMember("User2", "DET USER2", "1234", Ministry.DEFENSE);
        pm = PrimeMinister.getInstance("User3", "DET USER3", "12345");
    }

    @Test
    void testLoadEmptyJsonReturnsEmptyUserList() {
        List<User> users = repository.load();
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Failure - should be empty");
    }

    @Test
    void testLoadCitizens() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();

        arr.add(GSON.toJsonTree(c1));
        root.add("citizens", arr);

        writeJson(root);

        List<User> users = repository.load();

        assertEquals(1, users.size(), "Failure - should be equal");
        assertTrue(users.get(0) instanceof Citizen,
                                "Failure - should be instance of Citizen");
        assertEquals(c1.getUserName(), users.get(0).getUserName(),
                                "Failure - UserName should match");
    }

    @Test
    void testLoadMalformedCitizenJson() throws IOException {
        // Use valid JSON structure but with invalid citizen data
        // This way the exception is caught in loadCitizens() and ignored
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        
        // Add a valid citizen
        arr.add(GSON.toJsonTree(c1));
        
        // Add a string instead of an object - this will cause JsonSyntaxException
        // when trying to deserialize to Citizen
        arr.add("not a citizen object");
        
        root.add("citizens", arr);
        writeJson(root);
        
        // The repository should ignore the malformed citizen and only load the valid one
        List<User> users = repository.load();
        assertEquals(1, users.size(),
                            "Should only load valid citizens, ignoring malformed ones");
        assertTrue(users.get(0) instanceof Citizen,
                            "Failure - should be instance of Citizen");
        assertEquals(c1.getUserName(), users.get(0).getUserName(),
                            "Failure - UserName should match");
    }

    @Test
    void testLoadMalformedGovernmentMemberJson() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray gmArray = new JsonArray();

        gmArray.add("Not a GM");

        root.add("governmentMembers", gmArray);
        writeJson(root);
        assertDoesNotThrow(() -> repository.load(), "Failure - load shouldn't throw");
        List<User> users = repository.load();
        assertEquals(0, users.size(), "Failure - size should be 0");
    }

    @Test
    void testLoadMalformedPrimeMinisterJson() throws IOException {
        JsonObject root = new JsonObject();
        
        JsonObject invalidPm = new JsonObject();
        invalidPm.addProperty("userName", "test");
        invalidPm.addProperty("fullName", "Test");
        invalidPm.addProperty("password", "pass");
        invalidPm.add("userRole", new JsonArray()); // Wrong type - should be string
        
        root.add("primeMinister", invalidPm);
        writeJson(root);

        assertDoesNotThrow(() -> repository.load(), "Failure - load shouldn't throw");
        List<User> users = repository.load();
        assertEquals(0, users.size(), "Failure - size should be 0");
    }

    @Test
    void testLoadGovernmentMembers() throws IOException {
        repository.save(gm);

        List<User> users = repository.load();

        assertEquals(1, users.size(), "Failure - size should be one");
        assertTrue(users.get(0) instanceof GovernmentMember, "Failure should be instace of GovernmentMember");
    }

    @Test
    void testLoadPrimeMinister() throws IOException {
        repository.save(pm);

        List<User> users = repository.load();

        assertEquals(1, users.size(), "Failure - size should be one");
        assertTrue(users.get(0) instanceof PrimeMinister, "Failure should be instace of PrimeMinister");
    }

    @Test
    void testFindByIdFound() {
        repository.save(c1);

        Optional<User> found = repository.findById(c1.getId());

        assertTrue(found.isPresent(), "Failure - should found");
        assertEquals(c1.getUserName(), found.get().getUserName(), "Failure - usernames should be equal");
    }

    @Test
    void testFindByIdNullReturnsEmpty() {
        assertTrue(repository.findById(null).isEmpty(), "Failure - null id -> empty");
    }

    @Test
    void testFindByUsername() {
        repository.save(c1);

        Optional<User> found = repository.findByUsername(c1.getUserName());

        assertTrue(found.isPresent(), "Failure - should be present");
    }

    @Test
    void testFindByUsernameInvalid() {
        assertTrue(repository.findByUsername("").isEmpty(), "Failure - shouldn't find");
        assertTrue(repository.findByUsername(null).isEmpty(), "Failure - shouldn't find");
    }


    @Test
    void testSaveNewUser() {
        repository.save(c1);

        List<User> users = repository.load();
        assertEquals(1, users.size(), "Failure - should be size = 1");
        assertEquals(c1.getUserName(), users.get(0).getUserName(), "Failure - usernames should match");
    }

    @Test
    void testSaveFail() {
        assertDoesNotThrow(() -> repository.save(null),
                        "Failure - saving null user shouldn't throw");
        c1.setUserName(null);
        assertDoesNotThrow(() -> repository.save(c1),
                        "Failure - saving invalid user shouldn't throw");
        c1.setUserName("");
        assertDoesNotThrow(() -> repository.save(c1),
                        "Failure - saving invalid user shouldn't throw");
    }

    @Test
    void testSaveUpdatesExistingUser() {
        repository.save(c1);

        c1.setFullName("updated");
        repository.save(c1);

        List<User> users = repository.load();
        assertEquals(1, users.size(), "Failure - should be size = 1");
        assertEquals("updated", users.get(0).getFullName(), "Failure - fullName not updated");
    }


    @Test
    void testExistsById() {
        repository.save(c1);
        assertTrue(repository.existsById(c1.getId()), "Failure - user should exist");
    }

    @Test
    void testExistsByIdNull() {
        assertFalse(repository.existsById(null),
                                "Failure - shouldn't search with null");
    }

    @Test
    void testUsernameExists() {
        repository.save(c1);
        assertTrue(repository.usernameExists(c1.getUserName()),
                                "Failure - userName should exist");
    }

    @Test
    void testUserNameExistsNull() {
        assertDoesNotThrow(() -> repository.usernameExists(null),
                                "Failure - shouldn't search with null UserName");
    }

    @Test
    void testUserNameExistsBlank() {
        assertDoesNotThrow(() -> repository.usernameExists(""),
                                "Failure - shouldn't search with blank UserName");
    }

    @Test
    void testPrimeMinisterExists() {
        repository.save(pm);
        assertTrue(repository.primeMinisterExists(),
                                "Failure - PrimeMinister should exist");
    }

    @Test
    void testDelete() {
        repository.save(c1);

        repository.delete(c1);

        assertTrue(repository.load().isEmpty(),
                                "Failure - delete not working properly");
    }

    @Test
    void testDeleteFalseUser() {
        assertDoesNotThrow(() -> repository.delete(c1),
                                "Failure deleting non existing user shouldn't throw"); 
    }

    @Test
    void testDeleteNullDoesNothing() {
        assertDoesNotThrow(() -> repository.delete(null),
                                "Failure - deleting null shouldn't throw");
        
    }

    @Test
    void testDeleteAllUsers() {
        repository.save(c1);
        repository.save(gm);
        repository.save(pm);
        repository.deleteAllUsers();

        assertTrue(repository.load().isEmpty(),
                                "Failure - delete not working properly");
    }

    private void writeJson(JsonObject json) throws IOException {
        try (Writer w = Files.newBufferedWriter(usersFile, StandardCharsets.UTF_8)) {
            GSON.toJson(json, w);
        }
    }

    @Test
    void testLoadWhenFileNotFound(@TempDir Path emptyTempDir) throws IOException{
        System.setProperty("budget.data.dir", emptyTempDir.toString());
        Path resourceFile = Paths.get("target/classes/users.json");
        Path renamedFile = Paths.get("target/classes/users-temp.json");
        try {
            
            if (Files.exists(resourceFile)) {
                Files.move(resourceFile, renamedFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            List<User> result = repository.load();

            assertTrue(result.isEmpty(), "Failure - should return empty list when file not found");
        } finally {
            if (Files.exists(renamedFile)) {
                Files.move(renamedFile, resourceFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
