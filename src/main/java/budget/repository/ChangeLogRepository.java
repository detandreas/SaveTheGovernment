package budget.repository;

import budget.model.domain.ChangeLog;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.ArrayList;
/**
 * Repository for managing ChangeLog persistence.
 * Stateless implementation (load-on-demand).
 */
public class ChangeLogRepository
    implements GenericInterfaceRepository<ChangeLog, Integer>  {

    private static final Logger LOGGER =
    Logger.getLogger(ChangeLogRepository.class.getName());

    /** Gson instance used for JSON serialization and deserialization. */
    private static final Gson GSON =
     new GsonBuilder()
     .setPrettyPrinting()
     .create();

    private static final Object LOCK = new Object(); // for thread-safety

    /**
     * Loads all ChangeLog records from the JSON file.
     *
     * @return a list of ChangeLog entries;
     * returns an empty list if not found.
     */
    @Override
    public List<ChangeLog> load() {
        synchronized (LOCK) {
            InputStream in = PathsUtil.getBudgetChangesInputStream();

            if (in == null) {
                LOGGER
                .warning("budget-changes.json not found. "
                                + "Returning empty list.");
                return Collections.emptyList();
            }

            try (in; InputStreamReader reader =
                new InputStreamReader(in, StandardCharsets.UTF_8)) {
                ChangeLog[] logs = GSON.fromJson(reader, ChangeLog[].class);
                return logs != null ? Arrays.asList(logs)
                                    : Collections.emptyList();
            } catch (IOException | RuntimeException e) {
                LOGGER.log(Level.SEVERE, "Failed to load ChangeLog data", e);
                return Collections.emptyList();
            }
        }
    }

    /**
     * Saves or updates a ChangeLog record.
     * If a record with the same ID exists,
     * it is updated; otherwise, it is inserted.
     * @param entity the ChangeLog record to save or update.
     */
    @Override
    public void save(ChangeLog entity) {
        if (entity == null) {
            LOGGER.warning("Cannot save a null ChangeLog");
            return;
        }
        synchronized (LOCK) {
            List<ChangeLog> logs = new ArrayList<>(load());
            OptionalInt index = findIndexById(logs, entity.id());

            if (index.isPresent()) {
                logs.set(index.getAsInt(), entity);
            } else {
                logs.add(entity);
            }
            saveListToFile(logs);
        }
    }

    /**
    * Helper method that finds the index of a ChangeLog in a list by its ID.
    * This is a utility method used internally by other repository operations
    * to locate existing changes for update or deletion purposes.
    *
    * @param logs the list of change logs to search through
    * @param id the ID of the change log to locate
    * @return an OptionalInt containing the index if found, or empty if no
    *         change with the specified ID exists in the list
    */
    private OptionalInt findIndexById(
        final List<ChangeLog> logs,
        final int id
    ) {
            return IntStream
                    .range(0, logs.size())
                    .filter(i -> logs.get(i).id() == id)
                    .findFirst();
    }
    /**
     * Checks if a ChangeLog record with the given ID exists.
     *
     * @param id the ID to search for.
     * @return true if an entry exists, false otherwise
     */
    @Override
    public boolean existsById(Integer id) {
        if (id == null) {
            LOGGER.warning("Cannot search with a null id");
            return false;
        }
        synchronized (LOCK) {
            return load().stream().anyMatch(log -> log.id() == id);
        }
    }

    /**
     * Finds a ChangeLog record by its ID.
     *
     * @param id the unique ID of the ChangeLog
     * @return an Optional containing the ChangeLog if found, otherwise empty
     */
    @Override
    public Optional<ChangeLog> findById(Integer id) {
        if (id == null) {
            LOGGER.warning("Cannot search with a null id");
            return Optional.empty();
        }
        synchronized (LOCK) {
            return load().stream()
                    .filter(log -> log.id() == id)
                    .findFirst();
        }
    }

    /**
     * Deletes a ChangeLog record.
     *
     * @param entity the ChangeLog record to delete
     */
    @Override
    public void delete(ChangeLog entity) {
        if (entity == null) {
            LOGGER.warning("Cannot delete a null ChangeLog");
            return;
        }
        synchronized (LOCK) {
            List<ChangeLog> logs = new ArrayList<>(load());
            boolean removed = logs.removeIf(log -> log.id() == entity.id());
            if (removed) {
                saveListToFile(logs);
            }
        }
    }

    /**
     * Saves all ChangeLog records to the JSON file.
     * @param logs list of logs to store.
     */
    private void saveListToFile(List<ChangeLog> logs) {

        Path output = PathsUtil.getBudgetChangesWritablePath();
        try (BufferedWriter writer =
            Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
             GSON.toJson(logs, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save ChangeLog data", e);
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
                    .mapToInt(ChangeLog::id)
                    .max()
                    .orElse(0) + 1;
        }
    }
}
