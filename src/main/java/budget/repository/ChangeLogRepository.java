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

}