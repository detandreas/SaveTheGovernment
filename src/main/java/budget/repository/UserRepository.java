package budget.repository;

import budget.model.user.User;
import budget.model.user.UserRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing user data.
 * Handles loading, saving, and validation of users from JSON storage.
 */
public class UserRepository {

    private static final String USERS_FILE = "src/main/resources/users.json";

    private final Gson gson;
    private final List<User> users;

    /**
     * Default constructor.
     * Initializes the repository and loads existing users.
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
    public List<User> loadUsers() {
        final File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            final Type userListType = new TypeToken<List<User>>() {}.getType();
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
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
    
    /**
     * Saves a user to the repository.
     * If a user with the same username exists, it is updated.
     *
     * @param user the user to save
     */
    public void saveUser(final User user) {
        findByUsername(user.getUsername()).ifPresent(users::remove);
        users.add(user);
        saveToFile();
    }

    /**
     * Writes the user list to the JSON file.
     */
    private void saveToFile() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
