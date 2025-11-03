package budget.repository;

import budget.model.domain.user.User;
import budget.model.enums.UserRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing user data. Handles loading, saving, and
 * validation of users from JSON storage.
 */
public class UserRepository {

    private static final String USERS_FILE = "src/main/resources/users.json";

    private final Gson gson;
    private final List<User> users;

    /**
     * Default constructor. Initializes the repository and loads existing users.
     */
    public UserRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.users = loadUsers();
    }

    /**
     * Loads all users from the JSON file.
     *
     * @return list of users, or empty list if file not found or load fails
     */
    private List<User> loadUsers() {
        final File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            final Type userListType = new TypeToken<List<User>>() {
            }.getType();
            final List<User> loadedUsers = gson.fromJson(reader, userListType);
            return loadedUsers != null ? loadedUsers : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return optional containing the user if found, otherwise empty
     */
    public Optional<User> findByUsername(final String username) {
        return users.stream()
                .filter(u -> u.getUserName().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Saves a user to the repository.
     * If a user with the same username exists,
     * it is updated.
     * Null users or users with null/blank username are ignored.
     * @param user the user to save;
     * must not be null and must have a valid username
     */
    public void saveUser(final User user) {
        if (user == null || user.getUserName() == null
                || user.getUserName().isBlank()) {
            System.err.println("Cannot save user: null or invalid username.");
            return;
        }

        findByUsername(user.getUserName()).ifPresent(users::remove);
        users.add(user);
        saveToFile();
    }

    /**
     * Writes the user list to the JSON file.
     */
    private void saveToFile() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(USERS_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Returns an unmodifiable list of all users.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Checks if a username already exists in the repository.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(final String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return users.stream()
                .anyMatch(u -> u.getUserName().equalsIgnoreCase(username));
    }

    /**
     * Checks if a Prime Minister user already exists.
     *
     * @return true if there is at least one user with role PRIME_MINISTER
     */
    public boolean primeMinisterExists() {
        return users.stream()
                .anyMatch(u -> u.getUserRole() == UserRole.PRIME_MINISTER);
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username of the user to delete
     * @return true if the user was deleted, false if not found
     */
    public boolean deleteUser(final String username) {
        Optional<User> userToDelete = findByUsername(username);
        if (userToDelete.isPresent()) {
            users.remove(userToDelete.get());
            saveToFile();
            return true;
        }
        return false;
    }

    /**
     * Deletes all users from the repository.
     * Clears the in-memory list and updates the JSON file.
     */
    public void deleteAllUsers() {
        users.clear();
        saveToFile();
    }
}
