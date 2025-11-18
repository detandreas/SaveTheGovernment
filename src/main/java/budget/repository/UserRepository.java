package budget.repository;

import budget.model.domain.user.User;
import budget.model.enums.UserRole;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Repository class for managing user data. Handles loading, saving, and
 * validation of users from JSON storage.
 */
public class UserRepository
implements GenericInterfaceRepository<User, UUID> {

    private static final Gson GSON =
                    new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER =
                    Logger.getLogger(UserRepository.class.getName());
    private static final Object LOCK = new Object();

    /**
     * Loads all users from the JSON resource.
     *
     * @return list of {@link User}, or empty list when no records are found
     * or a read error occurs.
     */
    @Override
    public List<User> load() {
        InputStream input = PathsUtil.getUsersInputStream();
        synchronized (LOCK) {
        if (input == null) {
            LOGGER.log(
                Level.WARNING,
                "Resource {0} was not found returning empty list",
                PathsUtil.USERS_RESOURCE
            );
            return Collections.emptyList();
        }

            try (input; InputStreamReader reader =
                new InputStreamReader(input, StandardCharsets.UTF_8)) {
                User[] users = GSON.fromJson(reader, User[].class);
                return users == null ? Collections.emptyList()
                                    : Arrays.asList(users);
            } catch (IOException io) {
                LOGGER.log(
                    Level.SEVERE,
                    "Error reading " + PathsUtil.USERS_RESOURCE,
                    io
                );
                return Collections.emptyList();
            } catch (RuntimeException e) {
                LOGGER.log(
                    Level.SEVERE,
                    "Malformed users.json",
                    e
                );
                return Collections.emptyList();
            }
        }
    }

    /**
     * Retrieves the User associated with the supplied identifier.
     *
     * @param id the unique identifier of the User to look up
     *           must not be {@code null}
     * @return an {@link Optional} containing the matching
     * {@link User} or {@link Optional#empty()}
     * when the id is null or no entry exists
     */
    @Override
    public Optional<User> findById(UUID id) {
        synchronized (LOCK) {
            if (id == null) {
                LOGGER.warning("Cannot search with a null id");
                return Optional.empty();
            }
            return load()
                    .stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst();
        }
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to search for must not be {@code null}
     * @return an {@link Optional} containing the matching
     * {@link User} or {@link Optional#empty()}
     * when the userName is null or no entry exists
     */
    public Optional<User> findByUsername(final String username) {
        synchronized (LOCK) {
            if (username == null || username.isBlank()) {
                LOGGER.warning("Cannot search with a null or blank userName");
                return Optional.empty();
            }
            return load()
                    .stream()
                    .filter(u -> u.getUserName().equalsIgnoreCase(username))
                    .findFirst();
        }
    }

    /**
     * Saves a user to the repository.
     * If a user with the same {@code id} exists,
     * it is updated.
     * Null users or users with null id are ignored.
     * @param user the user to save;
     * must not be null and must have a valid username
     */
    @Override
    public void save(final User user) {
        synchronized (LOCK) {
            if (user == null || user.getUserName() == null
                    || user.getUserName().isBlank()) {
                LOGGER.warning("Cannot save user: null or invalid username.");
                return;
            }
            List<User> users = new ArrayList<>(load());
            OptionalInt index = findIndexById(users, user.getId());
            if (index.isPresent()) {
                users.set(index.getAsInt(), user);
            } else {
                users.add(user);
            }
            saveToFile(users);
        }
    }

    /**
    * Helper method that finds the index of a User in a list by its ID.
    * This is a utility method used internally by other repository operations
    * to locate existing users for update or deletion purposes.
    *
    * @param users the list of users to search through
    * @param id the ID of the user to locate
    * @return an OptionalInt containing the index if found, or empty if no
    *         user with the specified ID exists in the list
    */
    private OptionalInt findIndexById(
        final List<User> users,
        final UUID id
    ) {
        return IntStream
                .range(0, users.size())
                .filter(i -> users.get(i).getId().equals(id))
                .findFirst();
    }

    /**
     * Checks if a User exists in users.json
     * through its id.
     * @return {@code true} if it exists {@code false} otherwise.
     */
    @Override
    public boolean existsById(UUID id) {
        synchronized (LOCK) {
            if (id == null) {
                LOGGER.warning("Cannot search with a null id");
                return false;
            }
            return load()
                    .stream()
                    .anyMatch(user -> user.getId().equals(id));
        }
    }

    /**
     * Checks if a username already exists in the repository.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(final String username) {
        synchronized (LOCK) {
            if (username == null || username.isBlank()) {
                return false;
            }
            return load()
                    .stream()
                    .anyMatch(u -> u.getUserName().equalsIgnoreCase(username));
        }
    }

    /**
     * Checks if a Prime Minister user already exists.
     *
     * @return true if there is at least one user with role PRIME_MINISTER
     */
    public boolean primeMinisterExists() {
        synchronized (LOCK) {
            return load()
                    .stream()
                    .anyMatch(u -> u.getUserRole() == UserRole.PRIME_MINISTER);
        }
    }

    /**
     * Deletes the provided user from the backing store, if present.
     * When a matching entry is found it is removed and the updated collection
     * is persisted, otherwise no action is taken.
     *
     * @param user the user to remove, ignored when {@code null}
     */
    @Override
    public void delete(User user) {
        synchronized (LOCK) {
            if (user == null) {
                LOGGER.warning("Cannot delete a null user");
                return;
            }

            List<User> users = new ArrayList<>(load());
            OptionalInt index = findIndexById(users, user.getId());
            if (index.isPresent()) {
                users.remove(index.getAsInt());
                saveToFile(users);
            } else {
                LOGGER.warning("Can't delete User cause it doesn't exist");
            }
        }
    }

    /**
     * Deletes all users from the repository.
     * Clears list and updates the JSON file.
     */
    public void deleteAllUsers() {
        synchronized (LOCK) {
            List<User> users = new ArrayList<>(load());
            users.clear();
            saveToFile(users);
        }
    }

    /**
     * Serializes the supplied users collection to the backing JSON
     * file using the configured {@link Gson} instance. Any I/O failure is
     * logged and swallowed so that callers are not forced to handle checked
     * exceptions.
     *
     * @param users the collection of users that should be persisted
     */
    private void saveToFile(List<User> users) {
        Path target = PathsUtil.getUsersWritablePath();
        try (Writer writer = Files.newBufferedWriter(
                            target,
                            StandardCharsets.UTF_8)) {
            GSON.toJson(users, writer);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist users", e);
        }
    }
}
