package budget.backend.service;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.repository.BudgetRepository;
import budget.constants.Limits;
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
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        if (budget.getItems() == null) {
            throw new IllegalArgumentException("Budget items cannot be null");
        }
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
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        if (budget.getItems() == null) {
            throw new IllegalArgumentException("Budget items cannot be null");
        }
        return budget.getItems()
                            .stream()
                            .filter(budgetItem -> budgetItem != null
                                && !budgetItem.getIsRevenue())
                            .mapToDouble(budgetItem -> budgetItem.getValue())
                            .sum();
    }

    //  Μέθοδοι για Πίνακες

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
        revenueSeries.setName("Revenue");

        Series<Number, Number> expenseSeries = new Series<>();
        expenseSeries.setName("Expenses");

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
        return Map.of("Revenue", revenueSeries, "Expense", expenseSeries);
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
        netSeries.setName("Net result");

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
     * @return Series containing top N items
     * @throws IllegalArgumentException if budget for the
     *                                      specified year doesn't exist,
     *                                      if topN <= 0, or if year is invalid
     */
    public Series<String, Number> getTopBudgetItemsSeries(
                                        int year,
                                        int topN,
                                        boolean isRevenue
    ) throws IllegalArgumentException {
        validateYear(year);
        if (topN <= 0) {
            throw new IllegalArgumentException(
                "topN must be greater than 0, but was: " + topN);
        }

        Series<String, Number> series = new Series<>();
        series.setName(isRevenue ? "Top Revenue" : "Top Expense");

        Optional<Budget> budgetOpt = budgetRepository.findById(year);

        if (budgetOpt.isEmpty()) {
            throw new IllegalArgumentException("Budget for year: "
            + year + " doesn't exist");
        }

        Budget budget = budgetOpt.get();
        budget.getItems().stream()
            .filter(item -> item != null
                && item.getIsRevenue() == isRevenue)
            .sorted(Comparator.comparingDouble(BudgetItem::getValue).reversed())
            .limit(topN)
            .forEach(item -> series.getData().add(
                new Data<>(item.getName(), item.getValue())
            ));
        return  series;
    }

    /**
     * Creates an Series for displaying budget items comparison
     * between two years.
     *
     * @param year1 the first year to compare
     * @param year2 the second year to compare
     * @param isRevenue true for revenue items, false for expense items
     * @return Map containing "Year1" and "Year2" series
     * @throws IllegalArgumentException if year1 equals year2, if budget
     *                                  for either year doesn't exist,
     *                                  or if years are invalid
     */
    public Map<String, Series<String, Number>> getYearComparisonSeries(
        int year1, int year2, boolean isRevenue
    ) throws IllegalArgumentException {
        if (year1 == year2) {
            throw new IllegalArgumentException("Can't compare same year");
        }
        validateYear(year1);
        validateYear(year2);

        Series<String, Number> year1Series = new Series<>();
        year1Series.setName(String.valueOf(year1));

        Series<String, Number> year2Series = new Series<>();
        year2Series.setName(String.valueOf(year2));

        Map<Integer, Series<String, Number>> years =
                                Map.of(year1, year1Series, year2, year2Series);

        for (var entry : years.entrySet()) {
            int year = entry.getKey();
            var series = entry.getValue();

            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            if (budgetOpt.isEmpty()) {
                throw new IllegalArgumentException("Budget for year: "
            + year + " doesn't exist");
            }

            Budget budget = budgetOpt.get();
            budget.getItems().stream()
                .filter(item -> item != null
                    && item.getIsRevenue() == isRevenue)
                .forEach(item -> series.getData().add(
                    new Data<>(item.getName(), item.getValue())
                ));
        }
        return Map.of("Year1", year1Series, "Year2", year2Series);
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

        Optional<Budget> budgetOpt = budgetRepository.findById(year);

        if (budgetOpt.isEmpty()) {
            throw new IllegalArgumentException("Budget for year: "
            + year + " doesn't exist");
        }

        Budget budget = budgetOpt.get();
        var revenueData = new PieChart.Data(
                                        "Revenue", budget.getTotalRevenue());
        var expenseData = new PieChart.Data(
                                        "Expense", budget.getTotalExpense());

        return FXCollections.observableArrayList(revenueData, expenseData);
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
}
