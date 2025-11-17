package budget.repository;

import budget.model.domain.ChangeLog;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final Object lock = new Object(); // for thread-safety

    /**
     * Loads all ChangeLog records from the JSON file.
     *
     * @return a list of ChangeLog entries;
     * returns an empty list if not found.
     */
    @Override
    public List<ChangeLog> load() {

        InputStream in = PathsUtil.getBudgetChangesInputStream();

        if (in == null) {
            LOGGER
            .warning("budget-changes.json not found. Returning empty list.");
            return Collections.emptyList();
        }

        try (in; InputStreamReader reader =
            new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<ChangeLog>>() {
            }.getType();
            List<ChangeLog> logs = GSON.fromJson(reader, listType);
            return logs != null ? logs : Collections.emptyList();
        } catch (IOException| RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to load ChangeLog data", e);
            return Collections.emptyList() ;
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
        synchronized (lock) {
            List<ChangeLog> logs = load();
            boolean updated = false;

            for (int i = 0; i < logs.size(); i++) {
                if (logs.get(i).id() == entity.id()) {
                    logs.set(i, entity); // in-place update
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                int newId = generateId(logs);
                ChangeLog newLog = new ChangeLog(
                        newId,
                        entity.budgetItemId(),
                        entity.oldValue(),
                        entity.newValue(),
                        entity.submittedDate(),
                        entity.actorUserName(),
                        entity.actorId()
                );
                logs.add(newLog);
            }

            saveListToFile(logs);
        }
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
        return load().stream().anyMatch(log -> log.id() == id);
    }

    /**
     * Retrieves all ChangeLog records.
     *
     * @return a list of ChangeLog records.
     */
    public List<ChangeLog> findAll() {
        return load();
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
        return load().stream()
                .filter(log -> log.id() == id)
                .findFirst();
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
        synchronized (lock) {
            List<ChangeLog> logs = load();
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
     * @param logs list of existing logs.
     * @return a new unique integer ID
     */
    private int generateId(List<ChangeLog> logs) {
        return logs.stream()
                .mapToInt(ChangeLog::id)
                .max()
                .orElse(0) + 1;
    }
}
