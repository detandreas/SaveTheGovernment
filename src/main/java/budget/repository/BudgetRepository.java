package budget.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import budget.model.domain.Budget;
import budget.util.PathsUtil;




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
     * Serializes the supplied budgets collection to the backing JSON
     * file using the configured {@link Gson} instance. Any I/O failure is
     * logged and swallowed so that callers are not forced to handle checked
     * exceptions.
     * @param budgets the collection of budgets that should be expand
     */
    private void saveToFile(List<Budget> budgets) {
        Path target = PathsUtil.getBudgetWritablePath();
        try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(budgets, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Failed to persist budgets",e);
        }

    }
    /**
     * Checks if a budget exists for the given year.
     * If the provided year is null, the method returns false.
     * @param year the year to check for existence; may be null
     * @return true if a budget with the specified year exists, false otherwise
     */
    @Override
    public boolean existsById(final Integer year) {
        synchronized(LOCK) {
            if (year == null) {
                LOGGER.warning("Cannot search with a null year");
                return false;
            }
            return load()
                    .stream()
                    .anyMatch(b -> b.getYear() == year);
        }
    }
    /**
    * Deletes budgets that match the year of the provided Budget entity.
    * When a matching entry is found it is removed and the updated collection
     * is persisted; otherwise no action is taken
     * @param budget the budget to remove, ignored when {@code null}
    */
    @Override
    public void delete(final Budget budget) {
        synchronized(LOCK) {
            if (budget == null) {
                LOGGER.warning("Cannot delete null budget");
                return;
            }
            List<Budget> budgets = new ArrayList<>(load());
            OptionalInt index = findIndexByYear(budgets, budget.getYear());
            if (index.isPresent()) {
                budgets.remove(index.getAsInt());
                saveToFile(budgets);
            } else {
                LOGGER.warning("Cannot delete a budget because it doesn't exist");
            }
        }

    }
    /**
    * Retrieves the Budget associated with the specified year.
    * If a matching entry exists, it is returned wrapped in an Optional;
    * otherwise, an empty Optional is returned. Null input is safely ignored.
    *
    * @param year the year of the budget to search for; ignored when {@code null}
    * @return an Optional containing the matching Budget, or Optional.empty() if none is found
    */
    @Override
    public Optional<Budget> findById(final Integer year) {
        synchronized (LOCK) {
            if (year == null) {
                LOGGER.warning("Cannot find null budget");
                return Optional.empty();
            }
            return load()
                    .stream()
                    .filter(b -> year.equals(b.getYear()))
                    .findFirst();
        }
        
    }
}
