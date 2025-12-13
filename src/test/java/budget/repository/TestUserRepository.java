package budget.repository;

import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.UserRole;
import budget.util.PathsUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.io.Writer;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static Path tempDir;
    private static Path usersFile;
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

    private static void writeEmptyJson() throws IOException {
        try (Writer w = Files.newBufferedWriter(usersFile, StandardCharsets.UTF_8)) {
            JsonObject obj = new JsonObject();
            GSON.toJson(obj, w);
        }
    }

    @Test
    void testLoadEmptyJsonReturnsEmptyUserList() {
        List<User> users = repository.load();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testLoadCitizens() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();

        Citizen c = new Citizen(UUID.randomUUID(), "nikos", "pass", UserRole.CITIZEN);
        arr.add(GSON.toJsonTree(c));
        root.add("citizens", arr);

        writeJson(root);

        List<User> users = repository.load();

        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Citizen);
        assertEquals("nikos", users.get(0).getUserName());
    }

    @Test
    void testLoadGovernmentMembers() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();

        GovernmentMember gm =
                new GovernmentMember(UUID.randomUUID(), "alex", "pass", UserRole.GOVERNMENT_MEMBER, "Ministry");
        arr.add(GSON.toJsonTree(gm));
        root.add("governmentMembers", arr);

        writeJson(root);

        List<User> users = repository.load();

        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof GovernmentMember);
    }

    @Test
    void testLoadPrimeMinister() throws IOException {
        JsonObject root = new JsonObject();
        PrimeMinister pm =
                new PrimeMinister(UUID.randomUUID(), "leader", "123", UserRole.PRIME_MINISTER, "Greece");
        root.add("primeMinister", GSON.toJsonTree(pm));

        writeJson(root);

        List<User> users = repository.load();

        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof PrimeMinister);
    }

    @Test
    void testLoadMixedUsers() throws IOException {
        JsonObject root = new JsonObject();

        JsonArray citizens = new JsonArray();
        citizens.add(GSON.toJsonTree(new Citizen(UUID.randomUUID(), "c", "1", UserRole.CITIZEN)));
        root.add("citizens", citizens);

        JsonArray members = new JsonArray();
        members.add(GSON.toJsonTree(new GovernmentMember(UUID.randomUUID(), "g", "1", UserRole.GOVERNMENT_MEMBER, "M")));
        root.add("governmentMembers", members);

        root.add("primeMinister",
                GSON.toJsonTree(new PrimeMinister(UUID.randomUUID(), "p", "1", UserRole.PRIME_MINISTER, "Greece")));

        writeJson(root);

        List<User> users = repository.load();

        assertEquals(3, users.size());
    }

    @Test
    void testFindByIdFound() {
        UUID id = UUID.randomUUID();
        Citizen c = new Citizen(id, "manos", "1", UserRole.CITIZEN);
        repository.save(c);

        Optional<User> found = repository.findById(id);

        assertTrue(found.isPresent());
        assertEquals("manos", found.get().getUserName());
    }

    @Test
    void testFindByIdNullReturnsEmpty() {
        assertTrue(repository.findById(null).isEmpty());
    }

    @Test
    void testFindByUsername() {
        Citizen c = new Citizen(UUID.randomUUID(), "Maria", "1", UserRole.CITIZEN);
        repository.save(c);

        Optional<User> found = repository.findByUsername("maria");

        assertTrue(found.isPresent());
    }

    @Test
    void testFindByUsernameInvalid() {
        assertTrue(repository.findByUsername("").isEmpty());
        assertTrue(repository.findByUsername(null).isEmpty());
    }


    @Test
    void testSaveNewUser() {
        Citizen c = new Citizen(UUID.randomUUID(), "kostas", "pass", UserRole.CITIZEN);
        repository.save(c);

        List<User> users = repository.load();
        assertEquals(1, users.size());
        assertEquals("kostas", users.get(0).getUserName());
    }

    @Test
    void testSaveUpdatesExistingUser() {
        UUID id = UUID.randomUUID();
        Citizen c1 = new Citizen(id, "user1", "pass", UserRole.CITIZEN);
        repository.save(c1);

        Citizen updated = new Citizen(id, "changed", "pass", UserRole.CITIZEN);
        repository.save(updated);

        List<User> users = repository.load();
        assertEquals(1, users.size());
        assertEquals("changed", users.get(0).getUserName());
    }

    @Test
    void testSaveInvalidUserDoesNothing() {
        Citizen c = new Citizen(UUID.randomUUID(), "", "pass", UserRole.CITIZEN);
        repository.save(c);
        assertTrue(repository.load().isEmpty());
    }


    @Test
    void testExistsById() {
        UUID id = UUID.randomUUID();
        repository.save(new Citizen(id, "x", "1", UserRole.CITIZEN));
        assertTrue(repository.existsById(id));
    }

    @Test
    void testExistsByIdNull() {
        assertFalse(repository.existsById(null));
    }

    @Test
    void testUsernameExists() {
        repository.save(new Citizen(UUID.randomUUID(), "abc", "pw", UserRole.CITIZEN));
        assertTrue(repository.usernameExists("ABC"));
    }

    @Test
    void testPrimeMinisterExists() {
        repository.save(new PrimeMinister(UUID.randomUUID(), "pm", "1", UserRole.PRIME_MINISTER, "Greece"));
        assertTrue(repository.primeMinisterExists());
    }

    @Test
    void testDelete() {
        Citizen c = new Citizen(UUID.randomUUID(), "del", "1", UserRole.CITIZEN);
        repository.save(c);

        repository.delete(c);

        assertTrue(repository.load().isEmpty());
    }

    @Test
    void testDeleteNullDoesNothing() {
        repository.delete(null);
        assertTrue(repository.load().isEmpty());
    }

    @Test
    void testDeleteAllUsers() {
        repository.save(new Citizen(UUID.randomUUID(), "a", "1", UserRole.CITIZEN));
        repository.save(new Citizen(UUID.randomUUID(), "b", "1", UserRole.CITIZEN));
        repository.deleteAllUsers();

        assertTrue(repository.load().isEmpty());
    }

    private void writeJson(JsonObject json) throws IOException {
        try (Writer w = Files.newBufferedWriter(usersFile, StandardCharsets.UTF_8)) {
            GSON.toJson(json, w);
        }
    }
}
