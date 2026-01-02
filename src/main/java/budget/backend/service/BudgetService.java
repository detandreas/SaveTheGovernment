package budget.backend.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.repository.BudgetRepository;
import budget.backend.util.Regression;
import budget.constants.Limits;
import budget.frontend.constants.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * Service responsible for budget calculations and operations.
 * Provides methods for recalculating budget totals
 *                                  (revenue, expense, net result)
 * when budget items are modified, and preparing data
 *                                  for JavaFX charts and tables.
 */
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private static final int REGRESSION_START_YEAR = 2019;
    private static final int REGRESSION_END_YEAR = 2029;


    /**
     * Constructs a BudgetService with the specified repository.
     *
     * @param budgetRepository the repository used for budget data access
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "This allows testability and shared "
                                        + "state across service instances."
    )
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Recalculates and updates the budget totals
     *                              (totalRevenue, totalExpense, netResult)
     * based on the current budget items.
     *
     * This method should be called whenever budget items
     *                                      are added, removed, or modified
     * to ensure the budget totals remain accurate.
     *
     * @param budget the budget to recalculate totals for; must not be null
     * @throws IllegalArgumentException if budget is null
     */
    public void recalculateBudgetTotals(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }

        double totalRevenue = calculateTotalRevenue(budget);
        double totalExpense = calculateTotalExpense(budget);
        double netResult = totalRevenue - totalExpense;

        budget.setTotalRevenue(totalRevenue);
        budget.setTotalExpense(totalExpense);
        budget.setNetResult(netResult);
    }

    /**
     * Calculates the total revenue by summing all revenue budget items.
     *
     * @param budget the budget to calculate revenue from; must not be null
     * @return the sum of all revenue items (items where isRevenue is true)
     * @throws IllegalArgumentException if budget is null or items list is null
     */
    private double calculateTotalRevenue(Budget budget) {
        return budget.getItems()
                            .stream()
                            .filter(budgetItem -> budgetItem != null
                                && budgetItem.getIsRevenue())
                            .mapToDouble(budgetItem -> budgetItem.getValue())
                            .sum();
    }

    /**
     * Calculates the total expense by summing all expense budget items.
     *
     * @param budget the budget to calculate expenses from; must not be null
     * @return the sum of all expense items (items where isRevenue is false)
     * @throws IllegalArgumentException if budget is null or items list is null
     */
    private double calculateTotalExpense(Budget budget) {
        return budget.getItems()
                            .stream()
                            .filter(budgetItem -> budgetItem != null
                                && !budgetItem.getIsRevenue())
                            .mapToDouble(budgetItem -> budgetItem.getValue())
                            .sum();
    }

    /**
     * Calculates the sum of values in a Series.
     *
     * @param series the Series to sum values from
     * @return the sum of all Y values in the Series
     */
    private double calculateSeriesSum(Series<String, Number> series) {
        return series.getData().stream()
            .mapToDouble(data -> data.getYValue().doubleValue())
            .sum();
    }

    /**
     * Creates PieChart.Data entries from a Series with percentage labels.
     *
     * @param series the Series containing top items
     * @param total the total amount for calculating percentages
     * @return ObservableList of PieChart.Data with formatted labels
     */
    private ObservableList<PieChart.Data> createPieDataFromSeries(
        Series<String, Number> series,
        double total) {
        ObservableList<PieChart.Data> pieData =
                                        FXCollections.observableArrayList();

        for (var data : series.getData()) {
            double value = data.getYValue().doubleValue();
            double percentage = (value / total) * Limits.NUMBER_ONE_HUNDRED;
            String label = String.format("%s%n%.2f%%",
                data.getXValue(), percentage);

            pieData.add(new PieChart.Data(label, value));
        }

        return pieData;
    }

    /**
     * Creates a PieChart.Data entry for "Others" category if applicable.
     *
     * @param others the amount for "Others" category
     * @param total the total amount for calculating percentage
     * @return Optional PieChart.Data for Others, empty if others is too small
     */
    private Optional<PieChart.Data> createOthersPieData(
                                            double others, double total) {
        if (others <= Limits.SMALL_NUMBER) {
            return Optional.empty();
        }

        double percentage = (others / total) * Limits.NUMBER_ONE_HUNDRED;
        String label =
            String.format("%s%n%.2f%%", Constants.OTHERS_LABEL, percentage);

        return Optional.of(new PieChart.Data(label, others));
    }

    /**
    * Creates PieChart data for top budget items with an "Others" category.
    * Shows top N items and groups remaining items as "Others".
    *
    * @param year the year to get budget items from
    * @param isRevenue true for revenue items, false for expense items
    * @return ObservableList of PieChart.Data ready for display
    * @throws IllegalArgumentException if budget doesn't exist
    *                                               or year is invalid
    */
    public ObservableList<PieChart.Data> getBudgetItemsforPie(
                                                            int year,
                                                            boolean isRevenue
    ) {
        Series<String, Number> series =
                                getTopBudgetItemsSeries(year,
                                                        Constants.TOP_N_ITEMS,
                                                        isRevenue,
                                                        true
                                                    );
        Budget budget = getBudgetForYear(year);

        double total = isRevenue
        ? calculateTotalRevenue(budget)
        : calculateTotalExpense(budget);

        double topSum = calculateSeriesSum(series);
        double others = total - topSum;

        ObservableList<PieChart.Data> pieData =
                                        createPieDataFromSeries(series, total);
        createOthersPieData(others, total)
                    .ifPresent(pieData::add);
        return pieData;
    }

    //  Μέθοδοι για Πίνακες

    /**
     * Creates a Series for displaying loans trend over years.
     * Filters budget items that are "Loans".
     *
     * @param startYear the starting year (inclusive)
     * @param endYear the ending year (exclusive)
     * @param isRevenue true for revenue loans, false for expense loans
     * @return Series containing loans data across years
     * @throws IllegalArgumentException if startYear >= endYear or
     *                                                  if years are invalid
     */
    public Series<Number, Number> getLoansTrendSeries(
        int startYear,
        int endYear,
        boolean isRevenue
    ) throws IllegalArgumentException {
        validateYearRange(startYear, endYear);

        Series<Number, Number> loansSeries = new Series<>();
        loansSeries.setName(isRevenue
                            ? Constants.REVENUE_LOANS_LABEL
                            : Constants.EXPENSE_LOANS_LABEL
                        );

        for (int year = startYear; year < endYear; year++) {
            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            if (budgetOpt.isPresent()) {
                Budget budget = budgetOpt.get();

                // Sum all loan items for this year
                double totalLoans = budget.getItems().stream()
                    .filter(item -> item != null
                        && item.getIsRevenue() == isRevenue
                        && item.getName().equals(Constants.LOANS_ITEM_NAME))
                    .mapToDouble(BudgetItem::getValue)
                    .sum();

                loansSeries.getData().add(new Data<>(year, totalLoans));
            } else {
                // Budget doesn't exist for this year, add 0
                loansSeries.getData().add(new Data<>(year, 0.0));
            }
        }

        return loansSeries;
    }

    /**
     * Creates an ObservableList of BudgetItems for a specific year
     * suitable for use in JavaFX TableView.
     *
     * @param year the year of the budget to retrieve items from
     * @return ObservableList containing all budget items for the specified year
     */
    public ObservableList<BudgetItem> getBudgetItemsForTable(int year) {
        return budgetRepository
        .findById(year)
        .map(budget -> FXCollections.observableArrayList(budget.getItems()))
        .orElse(FXCollections.observableArrayList());
    }

    /**
     * Creates an ObservableList of BudgetItems sorted by value (descending)
     * for a specific year.
     *
     * @param year the year of the budget
     * @return ObservableList containing budget items sorted by value
     */
    public ObservableList<BudgetItem> getBudgetItemsSortedByValue(int year) {
        return budgetRepository.findById(year)
            .map(budget -> budget.getItems().stream()
                .filter(item -> item != null)
                .sorted(Comparator.comparingDouble(BudgetItem::getValue)
                                                                .reversed())
                .collect(Collectors.toList()))
            .map(FXCollections::observableArrayList)
            .orElse(FXCollections.observableArrayList());
    }

    //  Μέθοδοι για Γραφήματα

    /**
     * Gets the names of the top N budget items from a reference budget,
     * excluding loans.
     *
     * @param budget the budget to extract top items from
     * @param topN the number of top items to retrieve
     * @param isRevenue true for revenue items, false for expense items
     * @return List of item names sorted by value (descending)
     */
    private List<String> getTopItemNames(
            Budget budget,
            int topN,
            boolean isRevenue) {
        return budget.getItems().stream()
            .filter(item -> item != null
                && item.getIsRevenue() == isRevenue
                && !item.getName().equals(Constants.LOANS_ITEM_NAME))
            .sorted(Comparator.comparingDouble(BudgetItem::getValue).reversed())
            .limit(topN)
            .map(BudgetItem::getName)
            .collect(Collectors.toList());
    }

    /**
     * Finds the value of a specific budget item in a budget.
     *
     * @param budget the budget to search in
     * @param itemName the name of the item to find
     * @param isRevenue true for revenue items, false for expense items
     * @return Optional containing the item value if found, empty otherwise
     */
    private Optional<Double> findItemValueInBudget(
            Budget budget,
            String itemName,
            boolean isRevenue) {
        return budget.getItems().stream()
            .filter(item -> item != null
                && item.getIsRevenue() == isRevenue
                && itemName.equals(item.getName()))
            .findFirst()
            .map(BudgetItem::getValue);
    }

    /**
     * Creates a Series for a specific item showing its trend over years.
     *
     * @param itemName the name of the item
     * @param startYear the starting year (inclusive)
     * @param endYear the ending year (exclusive)
     * @param isRevenue true for revenue items, false for expense items
     * @return Series containing the item's values across years
     */
    private Series<Number, Number> createItemTrendSeries(
            String itemName,
            int startYear,
            int endYear,
            boolean isRevenue) {
        Series<Number, Number> itemSeries = new Series<>();
        itemSeries.setName(itemName);

        for (int year = startYear; year < endYear; year++) {
            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            double value = budgetOpt
                .flatMap(budget ->
                            findItemValueInBudget(budget, itemName, isRevenue))
                .orElse(0.0);

            itemSeries.getData().add(new Data<>(year, value));
        }

        return itemSeries;
    }

    /**
     * Creates a Map of Series, one for each of the top N budget items
     * from a specific reference year, showing their trend
     *                                              over a range of years.
     *
     * @param referenceYear the year to determine top items from
     * @param startYear the starting year for the trend (inclusive)
     * @param endYear the ending year for the trend (exclusive)
     * @param topN the number of top items to include (must be > 0)
     * @param isRevenue true for revenue items, false for expense items
     * @return Map where key is the item name and value is the Series
     *         containing the item's values across years
     * @throws IllegalArgumentException if startYear >= endYear,
     *                         if topN <= 0, if reference year doesn't exist,
     *                         or if years are invalid
     */
    public Map<String, Series<Number, Number>> getTopItemsTrendSeries(
        int referenceYear,
        int startYear,
        int endYear,
        int topN,
        boolean isRevenue
    ) throws IllegalArgumentException {
        validateYearRange(startYear, endYear);
        validateYear(referenceYear);
        validateTopN(topN);

        Budget referenceBudget = getBudgetForYear(referenceYear);
        List<String> topItemNames =
                getTopItemNames(referenceBudget, topN, isRevenue);

        Map<String, Series<Number, Number>> seriesMap = new HashMap<>();

        for (String itemName : topItemNames) {
            Series<Number, Number> itemSeries = createItemTrendSeries(
                itemName, startYear, endYear, isRevenue);
            seriesMap.put(itemName, itemSeries);
        }

        return seriesMap;
    }

    /**
     * Creates an Series for displaying revenue and expense trends over years.
     * Suitable for LineChart or AreaChart.
     *
     * @param startYear the starting year (inclusive)
     * @param endYear the ending year (exclusive)
     * @return Map containing "Revenue" and "Expense" series
     * @throws IllegalArgumentException if startYear >= endYear or
     *                                  if years are invalid
     */
    public Map<String, Series<Number, Number>> getRevenueExpenseTrendSeries(
            int startYear, int endYear) throws IllegalArgumentException {
        validateYearRange(startYear, endYear);

        Series<Number, Number> revenueSeries = new Series<>();
        revenueSeries.setName(Constants.REVENUE_LABEL);

        Series<Number, Number> expenseSeries = new Series<>();
        expenseSeries.setName(Constants.EXPENSES_LABEL);

        for (int year = startYear; year < endYear; year++) {
            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            if (budgetOpt.isEmpty()) {
                continue;
            }

            Budget budget = budgetOpt.get();
            revenueSeries.getData()
                            .add(new Data<>(year, budget.getTotalRevenue()));
            expenseSeries.getData()
                            .add(new Data<>(year, budget.getTotalExpense()));
        }
        return Map.of(Constants.REVENUE_LABEL,
                        revenueSeries,
                        Constants.EXPENSE_LABEL,
                        expenseSeries
                    );
    }

    /**
     * Creates an Series for displaying net result (balance) over years.
     * Suitable for LineChart or AreaChart.
     *
     * @param startYear the starting year (inclusive)
     * @param endYear the ending year (exclusive)
     * @return Series containing net result data
     * @throws IllegalArgumentException if startYear >= endYear or
     *                                  if years are invalid
     */
    public Series<Number, Number> getNetResultSeries(
        int startYear, int endYear) throws IllegalArgumentException {
        validateYearRange(startYear, endYear);

        Series<Number, Number> netSeries = new Series<>();
        netSeries.setName(Constants.NET_RESULT_LABEL);

        for (int year = startYear; year < endYear; year++) {
            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            if (budgetOpt.isEmpty()) {
                continue;
            }

            Budget budget = budgetOpt.get();
            netSeries.getData().add(new Data<>(year, budget.getNetResult()));
        }
        return netSeries;
    }

    /**
     * Creates an Series for displaying top N budget items by value
     * for a specific year. Suitable for BarChart.
     *
     * @param year the year of the budget
     * @param topN the number of top items to include (must be > 0)
     * @param isRevenue true for revenue items, false for expense items
     * @param includeLoans true to include loan items, false to exclude them
     * @return Series containing top N items
     * @throws IllegalArgumentException if budget for the
     *                                      specified year doesn't exist,
     *                                      if topN <= 0, or if year is invalid
     */
    public Series<String, Number> getTopBudgetItemsSeries(
                                        int year,
                                        int topN,
                                        boolean isRevenue,
                                        boolean includeLoans
    ) throws IllegalArgumentException {
        validateYear(year);
        validateTopN(topN);

        Series<String, Number> series = new Series<>();
        series.setName(isRevenue
                        ? Constants.TOP_REVENUE_LABEL
                        : Constants.TOP_EXPENSE_LABEL
                    );

        Budget budget = getBudgetForYear(year);
        budget.getItems().stream()
            .filter(item -> item != null
                && item.getIsRevenue() == isRevenue
                && (includeLoans
                    || !item.getName().equals(Constants.LOANS_ITEM_NAME)))
            .sorted(Comparator.comparingDouble(BudgetItem::getValue).reversed())
            .limit(topN)
            .forEach(item -> series.getData().add(
                new Data<>(item.getName(), item.getValue())
            ));
        return  series;
    }

    /**
     * Validates that two years are different.
     *
     * @param year1 the first year
     * @param year2 the second year
     * @throws IllegalArgumentException if year1 equals year2
     */
    private void validateDifferentYears(int year1, int year2) {
        if (year1 == year2) {
            throw new IllegalArgumentException("Can't compare same year");
        }
    }

    /**
     * Creates a Series for a specific year with its name.
     *
     * @param year the year to create Series for
     * @return Series with name set to the year string
     */
    private Series<String, Number> createYearSeries(int year) {
        Series<String, Number> series = new Series<>();
        series.setName(String.valueOf(year));
        return series;
    }

    /**
     * Adds budget metrics (Revenue and Expense) to a Series.
     *
     * @param series the Series to add data to
     * @param budget the budget containing the metrics
     */
    private void addBudgetMetricsToSeries(
            Series<String, Number> series,
            Budget budget) {
        series.getData().add(
            new Data<>(Constants.REVENUE_LABEL, budget.getTotalRevenue()));
        series.getData().add(
            new Data<>(Constants.EXPENSE_LABEL, budget.getTotalExpense()));
    }

    /**
     * Creates a Series for year comparison by loading budget
     *                                                      and adding metrics.
     *
     * @param year the year to create Series for
     * @return Series containing Revenue and Expense data points
     * @throws IllegalArgumentException if budget doesn't exist
     *                                                       or year is invalid
     */
    private Series<String, Number> createYearComparisonSeries(int year) {
        Budget budget = getBudgetForYear(year);
        Series<String, Number> series = createYearSeries(year);
        addBudgetMetricsToSeries(series, budget);
        return series;
    }

    /**
     * Creates a Map of Series for comparing budget results (revenue, expense)
     * between two years. Each Series contains the two budget metrics
     *                                                          for comparison.
     *
     * @param year1 the first year to compare
     * @param year2 the second year to compare
     * @return Map where key is the year name (e.g., "2024") and value
                    * is a Series containing "Revenue", "Expense", data points
     * @throws IllegalArgumentException if year1 equals year2, if budget
     *                                  for either year doesn't exist,
     *                                  or if years are invalid
     */
    public Map<String, Series<String, Number>> getYearComparisonSeries(
        int year1, int year2
    ) throws IllegalArgumentException {
        validateDifferentYears(year1, year2);
        validateYear(year1);
        validateYear(year2);

        Series<String, Number> year1Series = createYearComparisonSeries(year1);
        Series<String, Number> year2Series = createYearComparisonSeries(year2);

        return Map.of(
            String.valueOf(year1), year1Series,
            String.valueOf(year2), year2Series
        );
    }

    /**
     * Creates a Series for displaying total revenue and expense in a BarChart.
     * Suitable for comparing revenue vs expense for a specific year.
     *
     * @param year the year of the budget
     * @return Series containing "Revenue" and "Expense" data points
     * @throws IllegalArgumentException if budget for the specified year
     *                                      doesn't exist or if year is invalid
     */
    public Series<String, Number> getRevenueExpenseBarSeries(int year)
    throws IllegalArgumentException {
        validateYear(year);

        Budget budget = getBudgetForYear(year);
        Series<String, Number> series = new Series<>();
        series.setName(Constants.BUDGET_OVERVIEW_LABEL);

        series.getData().add(
            new Data<>(Constants.REVENUE_LABEL, budget.getTotalRevenue()));
        series.getData().add(
            new Data<>(Constants.EXPENSE_LABEL, budget.getTotalExpense()));

        return series;
    }

    /**
     * Creates data suitable for PieChart showing
     *                              revenue vs expense distribution
     * for a specific year.
     *
     * @param year the year of the budget
     * @return ObservableList of PieChart.Data containing revenue and expense
     * @throws IllegalArgumentException if budget for the
     *                                              specified year doesn't exist
     *                                              or if year is invalid
     */
    public ObservableList<PieChart.Data> getRevenueExpensePieData(int year)
            throws IllegalArgumentException {
        validateYear(year);

        Budget budget = getBudgetForYear(year);
        var revenueData = new PieChart.Data(
                            Constants.REVENUE_LABEL, budget.getTotalRevenue());
        var expenseData = new PieChart.Data(
                            Constants.EXPENSE_LABEL, budget.getTotalExpense());

        return FXCollections.observableArrayList(revenueData, expenseData);
    }

    /**
     * Creates a Series representing a linear Regression from 2018 to 2030.
     * @param existingSeries the series for which we will
     *                                              compute the Regression
     * @return regression series
     */
    public Series<Number, Number> createRegressionSeries(
                                    Series<Number, Number> existingSeries) {
        Regression regression = new Regression(existingSeries);
        double m = regression.getSlope();
        double b = regression.getIntercept();
        String operator = "+";
        double absB = b;
        if (b < Limits.SMALL_NUMBER) {
            absB = Math.abs(b);
            operator = "-";
        }

        Series<Number, Number> regressionSeries = new Series<>();
        regressionSeries.setName(
        String.format("Trend Line: Y = %,.2fX %s %,.2f", m, operator, absB));
        for (int year = REGRESSION_START_YEAR;
                                        year <= REGRESSION_END_YEAR; year++) {
            double y = m * year + b;
            regressionSeries.getData().add(new Data<>(year, y));
        }

        return regressionSeries;
    }

    /**
     * Validates that a year is within acceptable range.
     *
     * @param year the year to validate
     * @throws IllegalArgumentException if year is less than MIN_BUDGET_YEAR
     */
    private void validateYear(int year) {
        if (year < Limits.MIN_BUDGET_YEAR) {
            throw new IllegalArgumentException(
                "Year must be >= " + Limits.MIN_BUDGET_YEAR
                + ", but was: " + year);
        }
    }

    /**
     * Validates that a year range is valid.
     *
     * @param startYear the starting year
     * @param endYear the ending year
     * @throws IllegalArgumentException if startYear >= endYear or
     *                                  if years are invalid
     */
    private void validateYearRange(int startYear, int endYear) {
        validateYear(startYear);
        validateYear(endYear);
        if (startYear >= endYear) {
            throw new IllegalArgumentException(
                "startYear must be less than endYear, "
                + "but was: startYear=" + startYear
                + ", endYear=" + endYear);
        }
    }

    /**
     * Validates that topN is a positive number.
     *
     * @param topN the number to validate
     * @throws IllegalArgumentException if topN <= 0
     */
    private void validateTopN(int topN) {
        if (topN <= 0) {
            throw new IllegalArgumentException(
                "topN must be greater than 0, but was: " + topN);
        }
    }

    /**
     * Retrieves and validates budget for a given year.
     *
     * @param year the year to retrieve budget for
     * @return the Budget for the specified year
     * @throws IllegalArgumentException if budget doesn't exist
     *                                              or year is invalid
     */
    private Budget getBudgetForYear(int year) {
        validateYear(year);
        Optional<Budget> budgetOpt = budgetRepository.findById(year);

        if (budgetOpt.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Budget for year %d doesn't exist", year));
        }
        return budgetOpt.get();
    }
    /**
     * Updates the value of a specific budget item,
     * recalculates the budget totals,
     * and persists the changes using the repository.
     *
     * @param itemId   the ID of the item to update
     * @param year     the year of the budget containing the item
     * @param newValue the new value to set
     * @param isRevenue the type of item we are updating
     * @throws IllegalArgumentException if the budget or item is not found
     */
    public void updateItemValue(int itemId, int year,
                                double newValue, boolean isRevenue
    ) {
        validateYear(year);

        Optional<Budget> budgetOpt = budgetRepository.findById(year);

        if (budgetOpt.isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot update item. Budget for year %d not found.", year
                )
            );
        }
        Budget budget = budgetOpt.get();
        Optional<BudgetItem> itemOpt =
                    budgetRepository.findItemById(itemId, year, isRevenue);

        if (itemOpt.isPresent()) {
            BudgetItem item = itemOpt.get();
            System.out.print(item);
            item.setValue(newValue);
            recalculateBudgetTotals(budget);
            budgetRepository.save(budget);
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Item with ID %d not found in budget year %d",
                    itemId, year
                )
            );
        }
    }
}
