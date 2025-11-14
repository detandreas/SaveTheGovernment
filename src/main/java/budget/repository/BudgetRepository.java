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
            final Type budgetListType = new TypeToken<List<Budget>>() {}.getType();       // Solves Type erasure problem
            final List<Budget> loadBudgets = gson.fromJson(reader,budgetListType);
            return loadBudgets != null ? loadBudgets : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading budgets" + e.getMessage());
            return new ArrayList<>();
        }
        
    }
    /**
     * Saves a Budget entity to the JSON file.
     * Removes any existing budget with the same year to prevent duplicates.
     * @param entity the Budget object to be saved; must not be null
     */
    @Override
    public void save(Budget entity) {
        if (entity == null) {
            System.err.println("Cannot save null budget");
            return;
        }

        List<Budget> budgets = load();
        budgets.removeIf(b -> b.getYear() == entity.getYear());
        budgets.add(entity);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(BUDGET_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(budgets, writer);
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }
    /**
     * Checks if a budget exists for the given year.
     * If the provided year is null, the method returns false.
     * @param year the year to check for existence; may be null
     * @return true if a budget with the specified year exists, false otherwise
     */
    @Override
    public boolean existsById(Integer year) {
        if (year == null) {
            return false;
        }
        List<Budget> budgets = load();
        return budgets.stream()
                .anyMatch(b -> b.getYear() == year);
    }
}
