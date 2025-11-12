package budget.repository;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
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
 * Repository class for managing budget data. Handles loading, saving, and
 * validation of files from JSON storage.
 * */

public class BudgetRepository implements GenericInterfaceRepository<Budget, Integer> {

    private static final String  BUDGET_FILE = "src/main/resources/budget.json";

    private final Gson gson = new Gson();
    private final List<Budget> budgets = new ArrayList<>();

    /**
     * Loads all budgets from the budget.json File
     * @return list of budget or empty list if file is not found or load fails
     */
    @Override
     public List<Budget> load() {
        final File file = new File(BUDGET_FILE);
        if(!file.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            final Type budgetListType = new TypeToken<List<Budget>>() {
            }.getType();
            final List<Budget> loadBudgets = gson.fromJson(reader,budgetListType);
            return loadBudgets != null ? loadBudgets : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading budgets" + e.getMessage());
            return new ArrayList<>();
        }
        
    }
    /**
     * 
     */
}
