package budget.backend.repository;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.backend.util.PathsUtil;
import budget.constants.Limits;

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

    /**
     * Builds Budget objects from JSON input streams.
     * Parses both budget.json and bill-ministry-map.json to create
     * complete Budget objects with BudgetItems and their associated ministries.
     *
     * @param budgetReader the input stream reader for budget.json
     * @param ministryReader the input stream reader for bill-ministry-map.json
     * @return list of Budget objects, or empty list if parsing fails
     */
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

    /**
     * Parses the budget.json file into a JsonObject.
     *
     * @param budgetReader the input stream reader for budget.json
     * @return JsonObject representing the budget data, or null if parsing fails
     */
    private JsonObject parseBudgetJson(InputStreamReader budgetReader) {
        JsonObject budgetJson = GSON.fromJson(budgetReader, JsonObject.class);
        return budgetJson;
    }

    /**
     * Parses the bill-ministry-map.json file into a JsonObject.
     *
     * @param ministryReader the input stream reader for bill-ministry-map.json
     * @return JsonObject representing the ministry map,
     *                                 or null if parsing fails
     */
    private JsonObject parseMinistryMapJson(InputStreamReader ministryReader) {
        JsonObject mapJson = GSON.fromJson(ministryReader, JsonObject.class);
        return mapJson;
    }

    /**
     * Builds BudgetItem objects for a specific year from JSON data.
     * Processes both revenue items (esoda) and expense items (eksoda)
     * and creates BudgetItem objects with their associated ministries.
     *
     * @param yearData the JSON object containing year-specific budget data
     * @param year the budget year
     * @param byIdMap the ministry mapping by bill ID
     * @param byNameMap the ministry mapping by bill name
     * @return list of BudgetItem objects for the year
     */
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

    /**
     * Parses revenue items (esoda) from JSON array.
     * Converts each JSON element into a BudgetItem with isRevenue set to true.
     *
     * @param revenueData the JSON array containing revenue items
     * @param year the budget year
     * @param byIdMap the ministry mapping by bill ID
     * @param byNameMap the ministry mapping by bill name
     * @return list of BudgetItem objects representing revenue items
     */
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

    /**
     * Parses expense items (eksoda) from JSON array.
     * Converts each JSON element into a BudgetItem with isRevenue set to false.
     *
     * @param expenseData the JSON array containing expense items
     * @param year the budget year
     * @param byIdMap the ministry mapping by bill ID
     * @param byNameMap the ministry mapping by bill name
     * @return list of BudgetItem objects representing expense items
     */
    private List<BudgetItem> parseExpenseItems(
        JsonArray expenseData,
        int year,
        JsonObject byIdMap,
        JsonObject byNameMap
    ) {
        List<BudgetItem> items = new ArrayList<>();
        for (JsonElement element : expenseData) {
            JsonObject item = element.getAsJsonObject();
            BudgetItem budgetItem = createBudgetItem(
                item, year, false, byIdMap, byNameMap
            );
            items.add(budgetItem);
        }
        return items;
    }

    /**
     * Creates a BudgetItem from JSON data.
     * Extracts ID, name, and value from the JSON object and associates
     * the appropriate ministries based on the provided mappings.
     *
     * @param item the JSON object containing item data
     * @param year the budget year
     * @param isRevenue true if this is a revenue item, false if expense
     * @param byIdMap the ministry mapping by bill ID
     * @param byNameMap the ministry mapping by bill name
     * @return a BudgetItem instance
     */
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

    /**
     * Extracts the list of ministries for a budget item.
     * First tries to find ministries by ID, then by name if not found.
     *
     * @param id the budget item ID
     * @param name the budget item name
     * @param byIdMap the ministry mapping by bill ID
     * @param byNameMap the ministry mapping by bill name
     * @return list of Ministry enums associated with the budget item
     */
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

    /**
     * Parses ministry names from JSON array and converts
     *                                  them to Ministry enums.
     * Invalid ministry names are logged as warnings and skipped.
     *
     * @param ministryArray the JSON array containing ministry name strings
     * @return list of Ministry enums
     */
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

    /**
     * Builds a Budget object from a list of BudgetItems, calculating totals.
     * Computes total revenue, total expense,
     *                              and net result (revenue - expense).
     *
     * @param items the list of BudgetItem objects
     * @param year the budget year
     * @return a Budget object with calculated totals
     */
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
     * Checks if a budget exists for the given year.
     * If the provided year is null, the method returns false.
     * @param year the year to check for existence; may be null.
     * @return true if a budget with the specified year exists,
     *                                              false otherwise.
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
    * Checks if a budget item with the specified name exists in a given year.
    * Searches through all budgets for the specified year and checks if any
    * budget item has a matching name (case-sensitive comparison).
    *
    * @param itemName the name of the budget item to search for
    * @param year the year of the budget to search within
    * @return true if a budget item with the specified name exists
    *                in the given year, false otherwise or if itemName is null
    */
    public boolean existsByName(final String itemName, final int year) {
        synchronized (LOCK) {
            if (itemName == null) {
                LOGGER.warning("Cannot search with a null item name");
                return false;
            }

            List<Budget> budgets = load();

            for (Budget budget : budgets) {
                if (budget.getYear() != year) {
                    continue;
                }

                List<BudgetItem> items = budget.getItems();
                boolean found = items.stream()
                        .filter(item -> item != null)
                        .anyMatch(item -> itemName.equals(item.getName()));

                if (found) {
                    return true;
                }
            }
            return false;
        }
    }
    /**
     * Checks if a budget item with the specified ID exists in any budget.
     * Searches through all budgets and their items to find a matching ID.
     *
     * @param itemId the ID of the budget item to search for
     * @param year the year of the Budget we are searching in
     * @return true if a budget item with the specified ID exists,
     *         false otherwise or if itemId is null or <= 0
     */
    public boolean existsByItemId(final int itemId, final int year) {
        synchronized (LOCK) {
            if (itemId <= 0) {
                LOGGER.warning("Cannot search with a invalid item ID");
                return false;
            }
            if (year < Limits.MIN_BUDGET_YEAR) {
                LOGGER.warning("Cannot search with year earlier than 2000");
                return false;
            }

            Optional<Budget> budget = findById(year);
            if (budget.isEmpty()) {
                return false;
            }

            Budget budgetForYear = budget.get();
            List<BudgetItem> items = budgetForYear.getItems();
            return items
                    .stream()
                    .filter(item -> item != null)
                    .anyMatch(item -> item.getId() == itemId);
        }
    }
    /**
     * Finds a BudgetItem by id across all budgets.
     * @param id the item id.
     * @param year the year of the budget to search in.
     * @return an Optional containing the BudgetItem if found, or empty if not
     */
    public Optional<BudgetItem> findItemById(int id, int year,
                                                boolean isRevenue
    ) {
        synchronized (LOCK) {
            if (id <= 0) {
                LOGGER.warning("Cannot search with a invalid item ID");
                return Optional.empty();
            }
            if (year < Limits.MIN_BUDGET_YEAR) {
                LOGGER.warning("Cannot search with year earlier than 2000");
                return Optional.empty();
            }

            Optional<Budget> budget = findById(year);
            if (budget.isEmpty()) {
                return Optional.empty();
            }

            Budget budgetForYear = budget.get();
            List<BudgetItem> items = budgetForYear.getItems();

            return items
                    .stream()
                    .filter(item -> item != null)
                    .filter(item -> item.getId() == id && item.getIsRevenue() == isRevenue)
                    .findFirst();
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
           if (year == null || year < Limits.MIN_BUDGET_YEAR) {
               LOGGER
               .warning("Cannot search for a budget with null year"
               + " or earlier than 2000");
               return Optional.empty();
            }
            return load()
            .stream()
            .filter(b -> year.equals(b.getYear()))
            .findFirst();
        }
    }
    /**
    * Serializes the supplied budgets collection to the backing JSON file
    * using the configured {@link Gson} instance.
    * Converts Budget objects back to the original JSON structure with
    * years as keys and esoda/eksoda arrays.
    * Any I/O failure is logged and swallowed so that callers are not
    * forced to handle checked exceptions.
    *
    * @param budgets the collection of budgets that should be persisted
    */
    private void saveToFile(List<Budget> budgets) {
        Path target = PathsUtil.getBudgetWritablePath();
        try (Writer writer = Files.
                newBufferedWriter(target, StandardCharsets.UTF_8)) {
            JsonObject root = buildJsonFromBudgets(budgets);
            GSON.toJson(root, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist budgets", e);
        }
    }

    /**
     * Builds a JsonObject from a list of Budget objects,
     * converting them back to the original JSON structure.
     * Creates a root object with years as keys
     *                                      and year-specific data as values.
     *
     * @param budgets the list of Budget objects to convert
     * @return JsonObject with years as keys and esoda/eksoda structure
     */
    private JsonObject buildJsonFromBudgets(List<Budget> budgets) {
        JsonObject root = new JsonObject();

        for (Budget budget : budgets) {
            String yearStr = String.valueOf(budget.getYear());
            //χτίζει τον προυπολογισμό της συγκεκριμένης χρονιάς
            JsonObject yearData = buildYearDataFromBudget(budget);
            // yearStr ειναι το key και yearData το value
            root.add(yearStr, yearData);
        }
        return root;
    }

    /**
     * Builds a JsonObject for a single year from a Budget object.
     * Separates BudgetItems into esoda (revenue) and eksoda (expense) arrays.
     *
     * @param budget the Budget object to convert
     * @return JsonObject with "esoda" and "eksoda" arrays
     */
    private JsonObject buildYearDataFromBudget(Budget budget) {
        JsonObject yearData = new JsonObject();

        JsonArray esodaArray = new JsonArray();
        JsonArray eksodaArray = new JsonArray();

        for (BudgetItem item : budget.getItems()) {
            if (item == null) {
                continue;
            }

            JsonObject itemJson = buildItemJson(item);

            if (item.getIsRevenue()) {
                esodaArray.add(itemJson);
            } else  {
                eksodaArray.add(itemJson);
            }
        }

        yearData.add("esoda", esodaArray);
        yearData.add("eksoda", eksodaArray);

        return  yearData;
    }

    /**
     * Builds a JsonObject from a BudgetItem, containing only
     * the fields that exist in the JSON structure (ID, BILL, VALUE).
     * Ministries and other metadata are not included in the JSON output.
     *
     * @param item the BudgetItem to convert
     * @return JsonObject with ID, BILL, and VALUE fields
     */
    private JsonObject buildItemJson(BudgetItem item) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("ID", item.getId());
        itemJson.addProperty("BILL", item.getName());
        itemJson.addProperty("VALUE", item.getValue());

        return itemJson;
    }
}
