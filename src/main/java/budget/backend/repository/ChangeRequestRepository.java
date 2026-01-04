package budget.backend.repository;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import budget.backend.model.domain.PendingChange;
import budget.backend.util.PathsUtil;

/**
 * Repository that encapsulates data-access operations for {@link PendingChange}
 * records. Responsible for retrieving and persisting pending change requests,
 * hiding the underlying storage details from higher layers of the application.
 */
public class ChangeRequestRepository
implements GenericInterfaceRepository<PendingChange, Integer> {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER =
            Logger.getLogger(ChangeRequestRepository.class.getName());
    private static final Object LOCK = new Object();

    /**
     * Loads every pending change request from the JSON resource.
     *
     * @return a collection of {@link PendingChange} or an empty list when no
     *         records are found or a read error occurs.
     */
    @Override
    public List<PendingChange> load() {
        synchronized (LOCK) {
            // try with resources --> closes automatically reader, input
            InputStream input = PathsUtil.getPendingChangesInputStream();
            if (input == null) {
                LOGGER.log(
                    Level.WARNING,
                    "Resource {0} not found, returning empty list.",
                    PathsUtil.PENDING_CHANGES_RESOURCE
                );
                return Collections.emptyList();
            }
                try (input; InputStreamReader reader =
                        new InputStreamReader(input, StandardCharsets.UTF_8)) {
                        PendingChange[] changes = GSON.fromJson(
                                                reader, PendingChange[].class);
                        return changes == null ? Collections.emptyList()
                                                : Arrays.asList(changes);
                } catch (IOException io) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Error reading " + PathsUtil.PENDING_CHANGES_RESOURCE,
                        io
                    );
                    return Collections.emptyList();
                } catch (RuntimeException e) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Malformed pending changes payload",
                        e
                    );
                    return Collections.emptyList();
                }
        }
    }

    /**
     * Persists a pending change to the backing JSON file. If a change with the
     * same identifier already exists, that entry is replaced with the provided
     * instance, otherwise the change is appended to the collection.
     *
     * @param change the pending change to persist; ignored when {@code null}
     */
    @Override
    public void save(final PendingChange change) {
        synchronized (LOCK) {
            if (change == null) {
                LOGGER.warning("Cannot save a null PendingChange");
                return;
            }

            List<PendingChange> pendingChanges = new ArrayList<>(load());
            OptionalInt index = findIndexById(pendingChanges, change.getId());
            if (index.isPresent()) {
                pendingChanges.set(index.getAsInt(), change);
            } else {
                pendingChanges.add(change);
            }
            saveToFile(pendingChanges);
        }
    }
    /**
    * Helper method that finds the index of a PendingChange in a list by its ID.
    * This is a utility method used internally by other repository operations
    * to locate existing changes for update or deletion purposes.
    *
    * @param changes the list of pending changes to search through
    * @param id the ID of the pending change to locate
    * @return an OptionalInt containing the index if found, or empty if no
    *         change with the specified ID exists in the list
    */
    private OptionalInt findIndexById(
        final List<PendingChange> changes,
        final int id
    ) {
        return IntStream.range(0, changes.size())
                .filter(i -> changes.get(i).getId() == id)
                .findFirst();
    }
    /**
     * Checks if a PendingChange exists in pending-changes.json
     * through its id.
     * @return {@code true} if it exists {@code false} otherwise.
     */
    @Override
    public boolean existsById(Integer id) {
        synchronized (LOCK) {
            if (id == null) {
                LOGGER.warning("Cannot search with a null id");
                return false;
            }
            /* το id τυπου Integer γινεται auto-unboxing σε int
            για αυτο ειναι εγκυρη η συγκριση*/
            return load()
                    .stream()
                    .anyMatch(change -> change.getId() == id);
        }
    }

    /**
     * Deletes the provided pending change from the backing store, if present.
     * When a matching entry is found it is removed and the updated collection
     * is persisted; otherwise no action is taken.
     *
     * @param change the pending change to remove, ignored when {@code null}
     */
    @Override
    public void delete(PendingChange change) {
        synchronized (LOCK) {
            if (change == null) {
                LOGGER.warning("Cannot delete a null PendingChange");
                return;
            }

            List<PendingChange> pendingChanges = new ArrayList<>(load());
            OptionalInt index = findIndexById(pendingChanges, change.getId());
            if (index.isPresent()) {
                pendingChanges.remove(index.getAsInt());
                saveToFile(pendingChanges);
            } else {
                LOGGER
                .warning("Can't delete PendingChange cause it doesn't exist");
            }
        }
    }

    /**
     * Retrieves the pending change associated with the supplied identifier.
     *
     * @param id the unique identifier of the pending change to look up
     *           must not be {@code null}
     * @return an {@link Optional} containing the matching
     * {@link PendingChange} or {@link Optional#empty()}
     * when the id is null or no entry exists
     */
    @Override
    public Optional<PendingChange> findById(Integer id) {
        synchronized (LOCK) {
            if (id == null) {
                LOGGER.warning("Cannot search with a null id");
                return Optional.empty();
            }
            return load()
                    .stream()
                    .filter(change -> change.getId() == id)
                    .findFirst();
        }
    }

    /**
     * Generates a new unique ID based on the highest current ID in the list.
     * @return a new unique integer ID
     */
    public int generateId() {
        synchronized (LOCK) {
            return load()
                    .stream()
                    .mapToInt(PendingChange::getId)
                    .max()
                    .orElse(0) + 1;
        }
    }

    /**
     * Serializes the supplied pending changes collection to the backing JSON
     * file using the configured {@link Gson} instance. Any I/O failure is
     * logged and swallowed so that callers are not forced to handle checked
     * exceptions.
     *
     * @param pendingChanges the collection of changes that should be persisted
     */
    private void saveToFile(List<PendingChange> pendingChanges) {
        Path target = PathsUtil.getPendingChangesWritablePath();
        try (Writer writer = Files.newBufferedWriter(
                            target,
                            StandardCharsets.UTF_8)) {
            GSON.toJson(pendingChanges, writer);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist pending changes", e);
        }
    }
}
