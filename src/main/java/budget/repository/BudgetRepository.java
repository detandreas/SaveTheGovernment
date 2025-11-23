package budget.repository;

import budget.model.domain.Budget;
import budget.model.domain.user.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import budget.util.PathsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;




/**
 * Repository class for managing budget data. Handles loading, saving, and
 * validation of files from JSON storage.
 * */

public class BudgetRepository implements GenericInterfaceRepository<Budget, Integer> {

    private static final String  BUDGET_FILE = "src/main/resources/budget.json";

    private static final Gson GSON =
                    new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER =
                    Logger.getLogger(BudgetRepository.class.getName());
    private static final Object LOCK = new Object();



    /**
     * Loads all budgets from the budget.json File
     * @return list of budget or empty list if file is not found or load fails
     */
    @Override
     public List<Budget> load() {
        synchronized (LOCK) {
            InputStream input = PathsUtil.getBudgetInputStream();
            if (input == null) {
                LOGGER.log(
                    Level.WARNING,
                    "Resource {0} was not found returning empty list",
                    PathsUtil.BUDGET_RESOURCE
                );
                return Collections.emptyList();
            }
            try (input; InputStreamReader reader =
                    new InputStreamReader(input, StandardCharsets.UTF_8)) {
                Type budgetListType = new TypeToken<List<Budget>>() {}.getType();
                List<Budget> budgets = GSON.fromJson(reader, budgetListType);
                return budgets != null ? budgets : Collections.emptyList();
            } catch (IOException io) {
                LOGGER.log(
                    Level.SEVERE,
                    "Error reading " + PathsUtil.BUDGET_RESOURCE,
                    io
                );
                return Collections.emptyList();
            } catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE,
                    "Malformed budget payload",
                    e
                );
                return Collections.emptyList();
            }
        }
    }
    /**
     * Saves a Budget entity to the JSON file.
     * Removes any existing budget with the same year to prevent duplicates.
     * @param entity the Budget object to be saved; must not be null
     */
    @Override
    public void save(final Budget budget) {
        synchronized (LOCK) {
            if (budget == null) {
                LOGGER.warning("Cannot save a null budget");
                return;
            }
            List<Budget> budgets = new ArrayList<>(load());
            OptionalInt index = findIndexByYear(budgets, budget.getYear());
            if (index.isPresent()) {
                budgets.set(index.getAsInt(), budget);
            } else {
                budgets.add(budget);
            }
            saveToFile(budgets);
        }
    }
    /**
     * Helper method that finds the index of a Budget in a list by its year.
     * @param budgets the list of budgets to search through
     * @param year the year of the budget to locate
     * @return an OptionalInt containing the index if found, or empty if no
     *         budget with the specified year exists in the list
     */
    private OptionalInt findIndexByYear(final List<Budget> budgets, final int year) {
        return IntStream.range(0,budgets.size())
            .filter(i -> budgets.get(i).getYear() == year)
            .findFirst();
    }
    /**
     * Checks if a budget exists for the given year.
     * If the provided year is null, the method returns false.
     * @param year the year to check for existence; may be null
     * @return true if a budget with the specified year exists, false otherwise
     */
    @Override
    public boolean existsById(final Integer year) {
        if (year == null) {
            return false;
        }
        List<Budget> budgets = load();
        return budgets.stream()
                .anyMatch(b -> b.getYear() == year);
    }
    /**
    * Deletes budgets that match the year of the provided Budget entity.
    * If the provided entity is null, no deletion occurs and an error is logged.
    * Prints a message indicating whether any budgets were deleted.
    * Saves the updated list of budgets back to the JSON file.
    * @param entity the Budget object whose year is used to identify budgets to delete; may not be null
    */
    @Override
    public void delete(final Budget entity) {
        if (entity == null) {
            System.err.println("Cannot delete null budget");
            return;
        }

        List<Budget> budgets = load();
        boolean removed = budgets.removeIf(b -> b.getYear() == entity.getYear());

        if(removed) {
            System.out.println("Budget for year " + entity.getYear() + " was deleted");
        } else {
            System.out.println("No budget found for year " + entity.getYear());
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(BUDGET_FILE), StandardCharsets.UTF_8)) {
        gson.toJson(budgets, writer);
    } catch (IOException e) {
        System.err.println("Error saving budgets: " + e.getMessage());
    }
    }
    /**
     * Finds a Budget by its year.
     * @param year the year of the budget to find; if null, returns an empty Optional
     * @return an Optional containing the Budget if found, or Optional.empty() if not found
     */
    @Override
    public Optional<Budget> findById(final Integer year) {
        if (year == null) {
            System.err.println("Cannot find null budget");
            return Optional.empty();
        }
        return load().stream()
                    .filter(b -> year.equals(b.getYear()))
                    .findFirst();
    }
}
