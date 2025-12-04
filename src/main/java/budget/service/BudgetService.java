package budget.service;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.repository.BudgetRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sun.javafx.scene.EnteredExitedHandler;

/**
 * Service responsible for budget calculations and operations.
 * Provides methods for recalculating budget totals (revenue, expense, net result)
 * when budget items are modified, and preparing data for JavaFX charts and tables.
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
        justification = "This allows testability and shared state across service instances."
    )
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Recalculates and updates the budget totals (totalRevenue, totalExpense, netResult)
     * based on the current budget items.
     * 
     * This method should be called whenever budget items are added, removed, or modified
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
     * @param budget the budget to calculate revenue from
     * @return the sum of all revenue items (items where isRevenue is true)
     */
    public double calculateTotalRevenue(Budget budget) {
        return budget.getItems()
                            .stream()
                            .filter(budgetItem -> budgetItem.getIsRevenue())
                            .mapToDouble(budgetItem -> budgetItem.getValue())
                            .sum();
    }

    /**
     * Calculates the total expense by summing all expense budget items.
     *
     * @param budget the budget to calculate expenses from
     * @return the sum of all expense items (items where isRevenue is false)
     */
    public double calculateTotalExpense(Budget budget) {
        return budget.getItems()
                            .stream()
                            .filter(budgetItem -> !budgetItem.getIsRevenue())
                            .mapToDouble(budgetItem -> budgetItem.getValue())
                            .sum();
    }

    //  Μέθοδοι για Πίνακες

    /**
     * Creates an ObservableList of BudgetItems for a specific year
     * suitable for use in JavaFX TableView.
     *
     * @param year the year of the budget to retrieve items from
     * @return ObservableList containing all budget items for the specified year
     */
    public ObservableList<BudgetItem> getBudgetItemForTable(int year) {
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
                .sorted(Comparator.comparingDouble(BudgetItem::getValue)
                                                                .reversed())
                .collect(Collectors.toList()))
            .map(FXCollections::observableArrayList)
            .orElse(FXCollections.observableArrayList());
    }

    //  Μέθοδοι για Πίνακες

    /**
     * Creates an Series for displaying revenue and expense trends over years.
     * Suitable for LineChart or AreaChart.
     *
     * @param startYear the starting year (inclusive)
     * @param endYear the ending year (exclusive)
     * @return Map containing "Revenue" and "Expense" series
     */
    public Map<String, Series<Integer, Number>> getRevenueExpenseTrendSeries(
            int startYear, int endYear) {
        Series<Integer, Number> revenueSeries = new Series<>();
        revenueSeries.setName("Revenue");

        Series<Integer, Number> expenseSeries = new Series<>();
        expenseSeries.setName("Expenses");

        for (int year = startYear; year < endYear; year++) {
            Optional<Budget> budgetOpt = budgetRepository.findById(year);

            if(budgetOpt.isEmpty()) {
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
     */
    public Series<Integer, Number> getNetResultSeries(
        int startYear, int endYear) {
        Series<Integer, Number> netSeries = new Series<>();
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

}