package budget.repository;

import budget.model.domain.ChangeLog;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository class for managing ChangeLog records.
 * This class provides basic create,read,update,delete operations for ChangeLog objects,
 * persisting them to a JSON file using Gson.
 */
public class ChangeLogRepository implements GenericInterfaceRepository<ChangeLog, Integer>  {

    private static final Logger LOGGER = Logger.getLogger(ChangeLogRepository.class.getName());

    /** Gson instance used for JSON serialization and deserialization */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** In-memory list of all ChangeLog records */
    private List<ChangeLog> changeLogs;

    /**
     * Constructor initializes the repository by loading existing ChangeLog records.
     */
    public ChangeLogRepository() {
        this.changeLogs = load();
    }

    /**
     * Loads all ChangeLog records from the JSON file.
     *
     * @return a list of ChangeLog objects; returns an empty list if the file is missing or unreadable
     */
    @Override
    public List<ChangeLog> load() {
        InputStream in = PathsUtil.getBudgetChangesInputStream();
        if (in == null) {
            LOGGER.warning("budget-changes.json not found. Returning empty list.");
            return new ArrayList<>();
        }

        try (InputStreamReader reader = new InputStreamReader(in)) {
            Type listType = new TypeToken<List<ChangeLog>>() {}.getType();
            List<ChangeLog> logs = GSON.fromJson(reader, listType);
            return logs != null ? logs : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load ChangeLog data", e);
            return new ArrayList<>();
        }
    }

    /**
     * Saves or updates a ChangeLog record.
     * If a record with the same ID exists, it is updated; otherwise, it is inserted.
     *
     * @param entity the ChangeLog record to save
     */
    @Override
    public void save(final ChangeLog entity) {
        Optional<ChangeLog> existing = findById(entity.id());
        if (existing.isPresent()) {
            update(entity);
        } else {
            insert(entity);
        }
        saveListToFile();
    }

    /**
     * Checks if a ChangeLog record with the given ID exists.
     *
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsById(final Integer id) {
        return changeLogs.stream().anyMatch(log -> log.id() == id);
    }


    /**
     * Retrieves all ChangeLog records.
     *
     * @return a copy of the list of ChangeLog records
     */
    public List<ChangeLog> findAll() {
        return new ArrayList<>(changeLogs);
    }

    /**
     * Finds a ChangeLog record by its ID.
     *
     * @param id the unique ID of the ChangeLog
     * @return an Optional containing the ChangeLog if found, otherwise empty
     */
    @Override
    public Optional<ChangeLog> findById(final Integer id) {
        return changeLogs.stream()
                .filter(log -> log.id() == id)
                .findFirst();
    }

    /**
     * Inserts a new ChangeLog record.
     * Generates a new unique ID and adds the record to the in-memory list.
     * @param changeLog the ChangeLog object to insert
     */
    public void insert(final ChangeLog changeLog) {
        int newId = generateId();
        ChangeLog newLog = new ChangeLog(
                newId,
                changeLog.budgetItemId(),
                changeLog.oldValue(),
                changeLog.newValue(),
                changeLog.submittedDate(),
                changeLog.actorUserName(),
                changeLog.actorId()
        );
        changeLogs.add(newLog);
    }

    /**
     * Updates an existing ChangeLog record.
     *
     * @param updatedLog the ChangeLog record with updated values
     */
    public void update(final ChangeLog updatedLog) {
        for (int i = 0; i < changeLogs.size(); i++) {
            if (changeLogs.get(i).id() == updatedLog.id()) {
                changeLogs.set(i, updatedLog);
                return;
            }
        }
    }

    /**
     * Deletes a ChangeLog record.
     *
     * @param entity the ChangeLog record to delete
     */
    @Override
    public void delete(final ChangeLog entity) {
        changeLogs.removeIf(log -> log.id() == entity.id());
        saveListToFile();
    }

    /**
     * Saves the entire list of ChangeLog records to the JSON file.
     */
    private void saveListToFile() {
        Path output = PathsUtil.getBudgetChangesWritablePath();
        try (FileWriter writer = new FileWriter(output.toFile())) {
            GSON.toJson(changeLogs, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save ChangeLog data", e);
        }
    }

    /**
     * Generates a new unique ID based on the highest current ID in the list.
     *
     * @return a new unique integer ID
     */
    private int generateId() {
        return changeLogs.stream()
                .mapToInt(ChangeLog::id)
                .max()
                .orElse(0) + 1;
    }
}