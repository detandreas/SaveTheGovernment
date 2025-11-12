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
}