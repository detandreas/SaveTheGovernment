package budget.repository;

import budget.model.domain.ChangeLog;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing ChangeLog records.
 * This class provides basic create,read,update,delete operations for ChangeLog objects,
 * persisting them to a JSON file using Gson.
 */
public class ChangeLogRepository {
    /** Gson instance used for JSON serialization and deserialization */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Path to the ChangeLog JSON file */
    private static final String RESOURCE_PATH = PathsUtil.BUDGET_CHANGES_RESOURCE;

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
    public List<ChangeLog> load() {
        try (FileReader reader = PathsUtil.getBudgetChangesFileReader()) {
            if (reader == null) {
                System.err.println("[WARN] budget-changes.json not found. Returning empty list.");
                return new ArrayList<>();
            }
            Type listType = new TypeToken<List<ChangeLog>>() {}.getType();
            return GSON.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load ChangeLog data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Saves the current in-memory list of ChangeLog records to the JSON file.
     */
    public void save() {
        try (FileWriter writer = new FileWriter("src/main/resources" + RESOURCE_PATH)) {
            GSON.toJson(changeLogs, writer);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save ChangeLog data: " + e.getMessage());
        }
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
    public Optional<ChangeLog> findById(final int id) {
        return changeLogs.stream()
                .filter(log -> log.id() == id)
                .findFirst();
    }

    /**
     * Inserts a new ChangeLog record.
     * <p>
     * Generates a new unique ID and adds the record to the in-memory list,
     * then persists the changes to the JSON file.
     * </p>
     *
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
        save();
    }

    /**
     * Updates an existing ChangeLog record.
     *
     * @param updatedLog the updated ChangeLog object
     */
    public void update(final ChangeLog updatedLog) {
        for (int i = 0; i < changeLogs.size(); i++) {
            if (changeLogs.get(i).id() == updatedLog.id()) {
                changeLogs.set(i, updatedLog);
                save();
                return;
            }
        }
    }

    /**
     * Deletes a ChangeLog record by its ID.
     *
     * @param id the ID of the ChangeLog record to remove
     */
    public void delete(final int id) {
        changeLogs.removeIf(log -> log.id() == id);
        save();
    }

}