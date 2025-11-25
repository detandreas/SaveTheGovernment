package budget.repository;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.enums.Ministry;
import budget.util.PathsUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;




/**
 * Repository class for managing budget data. Handles loading, saving, and
 * validation of files from JSON storage.
 * */

public class BudgetRepository
        implements GenericInterfaceRepository<Budget, Integer> {

    private static final Gson GSON =
                    new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
    private static final Logger LOGGER =
                    Logger.getLogger(BudgetRepository.class.getName());
    private static final Object LOCK = new Object();

    /**
     * Loads all budgets from the budget.json File.
     * @return list of budget or empty list if file is not found or load fails.
     */
    @Override
     public List<Budget> load() {
        synchronized (LOCK) {
            //load budget.json
            InputStream budgetInput = PathsUtil.getBudgetInputStream();
            if (budgetInput == null) {
                LOGGER.log(
                    Level.WARNING,
                    "Resource {0} was not found returning empty list",
                    PathsUtil.BUDGET_RESOURCE
                );
                return Collections.emptyList();
            }
            //load bill-ministry-map.json
            InputStream ministryInput =
                            PathsUtil.getBillMinistryMapInputStream();
            if (ministryInput == null) {
                LOGGER.log(
                    Level.WARNING,
                    "Resource {0} was not found returning empty list",
                    PathsUtil.BILL_MINISTRY_MAP_RESOURCE
                );
                return Collections.emptyList();
            }
            try (budgetInput; ministryInput;
                InputStreamReader budgetReader =
                    new InputStreamReader(budgetInput, StandardCharsets.UTF_8);
                InputStreamReader ministryReader =
                    new InputStreamReader(
                        ministryInput, StandardCharsets.UTF_8)) {
                return buildBudgetsFromJson(budgetReader, ministryReader);
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
    private List<Budget> buildBudgetsFromJson(
        InputStreamReader budgetReader,
        InputStreamReader ministryReader
    ) {
        JsonObject budgetJson = parseBudgetJson(budgetReader);
        if (budgetJson == null) {
            return Collections.emptyList();
        }

        JsonObject ministryMapJson = parseMinistryMapJson(ministryReader);
        if (ministryMapJson == null) {
            return Collections.emptyList();
        }
        //παίρνει το json με key byID
        JsonObject byIdMap = ministryMapJson.getAsJsonObject("byId");
        //παίρνει το json με key byName
        JsonObject byNameMap = ministryMapJson.getAsJsonObject("byName");

        List<Budget> budgets = new ArrayList<>();
        // θυμιθείτε την δομή του budget.json
        // εχεις ως key τον χρόνο και value είναι ένα αλλο json
        // που αποτελεί τα στοιχεία του προυπολογισμού ενός έτους
        for (String yearStr : budgetJson.keySet()) {
            int year = Integer.parseInt(yearStr);
            JsonObject yearBudgetData = budgetJson.getAsJsonObject(yearStr);
            List<BudgetItem> items = buildBudgetItemsForYear(
                yearBudgetData, year, byIdMap, byNameMap
            );
            Budget budget = buildBudgetFromItems(items, year);
            budgets.add(budget);
        }
        return budgets;
    }
    private JsonObject parseBudgetJson(InputStreamReader budgetReader) {
        JsonObject budgetJson = GSON.fromJson(budgetReader, JsonObject.class);
        return budgetJson;
    }

    private JsonObject parseMinistryMapJson(InputStreamReader ministryReader) {
        JsonObject mapJson = GSON.fromJson(ministryReader, JsonObject.class);
        return mapJson;
    }

    private List<BudgetItem> buildBudgetItemsForYear(
        JsonObject yearData,
        int year,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        List<BudgetItem> items = new ArrayList<>();

        if (yearData.has("esoda")) {
            items.addAll(parseRevenueItems(
                yearData.getAsJsonArray("esoda"),
                year,
                byIdMap,
                byNameMap
            ));
        }

        if (yearData.has("eksoda")) {
            items.addAll(parseExpenseItems(
                yearData.getAsJsonArray("eksoda"),
                year,
                byIdMap,
                byNameMap
            ));
        }
        return items;
    }

    private List<BudgetItem> parseRevenueItems(
        JsonArray revenueData,
        int year,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        List<BudgetItem> items = new ArrayList<>();
        for (JsonElement element : revenueData) {
            JsonObject item = element.getAsJsonObject();
            BudgetItem budgetItem = createBudgetItem(
                item, year, true, byIdMap, byNameMap
            );
            items.add(budgetItem);
        }
        return items;
    }

    private List<BudgetItem> parseExpenseItems(
        JsonArray exepenseData,
        int year,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        List<BudgetItem> items = new ArrayList<>();
        for (JsonElement element : exepenseData) {
            JsonObject item = element.getAsJsonObject();
            BudgetItem budegtItem = createBudgetItem(
                item, year, false, byIdMap, byNameMap
            );
            items.add(budegtItem);
        }
        return items;
    }

    private BudgetItem createBudgetItem(
        JsonObject item,
        int year,
        boolean isRevenue,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        int id = item.get("ID").getAsInt();
        String name = item.get("BILL").getAsString();
        double value = item.get("VALUE").getAsDouble();
        List<Ministry> ministries =
                            extractMinistries(id, name, byIdMap, byNameMap);

        return new BudgetItem(id, year, name, value, isRevenue, ministries);
    }

    private List<Ministry> extractMinistries(
        int id,
        String name,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        List<Ministry> ministries = new ArrayList<>();
        String idStr = String.valueOf(id);

        if (byIdMap != null && byIdMap.has(idStr)) {
            ministries.addAll(parseMinistriesFromArray(
                byIdMap.getAsJsonArray(idStr)
            ));
        } else if (byNameMap != null && byNameMap.has(name)) {
            ministries.addAll(parseMinistriesFromArray(
                byNameMap.getAsJsonArray(name)
            ));
        }

        return ministries;
    }

    private List<Ministry> parseMinistriesFromArray(JsonArray ministryArray) {
        List<Ministry> ministries = new ArrayList<>();
        for (JsonElement ministryElement : ministryArray) {
            String ministryStr = ministryElement.getAsString();
            try {
                ministries.add(Ministry.valueOf(ministryStr));
            } catch (IllegalArgumentException e) {
                LOGGER.log(
                    Level.WARNING,
                    "Unknown ministry: {0}",
                    ministryStr
                );
            }
        }
        return ministries;
    }

    private Budget buildBudgetFromItems(List<BudgetItem> items, int year) {
        double totalRevenue = items.stream()
            .filter((budgetItem) -> budgetItem.getIsRevenue())
            .mapToDouble((budgetItem) -> budgetItem.getValue())
            .sum();

        double totalExpense = items.stream()
            .filter(item -> !item.getIsRevenue())
            .mapToDouble((budgetItem) -> budgetItem.getValue())
            .sum();

        double netResult = totalRevenue - totalExpense;

        return new Budget(items, year, totalRevenue, totalExpense, netResult);
    }
    /**
     * Saves a Budget entity to the JSON file.
     * Removes any existing budget with the same year to prevent duplicates.
     * @param budget the Budget object to be saved; must not be null.
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
     * @param budgets the list of budgets to search through.
     * @param year the year of the budget to locate.
     * @return an OptionalInt containing the index if found, or empty if no.
     *budget with the specified year exists in the list.
     */
    private OptionalInt findIndexByYear(final List<Budget> budgets,
            final int year) {
        return IntStream.range(0, budgets.size())
            .filter(i -> budgets.get(i).getYear() == year)
            .findFirst();
    }
    /**
     * Serializes the supplied budgets collection to the backing JSON.
     * file using the configured {@link Gson} instance. Any I/O failure is.
     * logged and swallowed so that callers are not forced to handle checked.
     * exceptions.
     * @param budgets the collection of budgets that should be expand.
     */
    private void saveToFile(List<Budget> budgets) {
        Path target = PathsUtil.getBudgetWritablePath();
        try (Writer writer = Files.
                newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(budgets, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist budgets", e);
        }

    }
    /**
     * Checks if a budget exists for the given year.
     * If the provided year is null, the method returns false.
     * @param year the year to check for existence; may be null.
     * @return true if a budget with the specified year exists, false otherwise.
     */
    @Override
    public boolean existsById(final Integer year) {
        synchronized (LOCK) {
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
    * When a matching entry is found it is removed and the updated collection.
     * is persisted; otherwise no action is taken.
     * @param budget the budget to remove, ignored when {@code null}.
    */
    @Override
    public void delete(final Budget budget) {
        synchronized (LOCK) {
            if (budget == null) {
                LOGGER.warning("Cannot delete a null budget");
                return;
            }
            List<Budget> budgets = new ArrayList<>(load());
            OptionalInt index = findIndexByYear(budgets, budget.getYear());
            if (index.isPresent()) {
                budgets.remove(index.getAsInt());
                saveToFile(budgets);
            } else {
                LOGGER.
                warning("Cannot delete a budget because it doesn't exist");
            }
        }

    }
    /**
    * Retrieves the Budget associated with the specified year.
    * If a matching entry exists, it is returned wrapped in an Optional;
    * otherwise, an empty Optional is returned. Null input is safely ignored.
    * @param year the year of the budget to search for;
    * ignored when {@code null}.
    * @return an Optional containing the matching Budget,
    * or Optional.empty() if none is found.
    */
    @Override
    public Optional<Budget> findById(final Integer year) {
        synchronized (LOCK) {
            if (year == null) {
                LOGGER.warning("Cannot search for a budget with null year");
                return Optional.empty();
            }
            return load()
                    .stream()
                    .filter(b -> year.equals(b.getYear()))
                    .findFirst();
        }
    }
}
