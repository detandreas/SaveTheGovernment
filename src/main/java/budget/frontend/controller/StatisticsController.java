package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import budget.backend.repository.BudgetRepository;
import budget.backend.service.BudgetService;
import budget.constants.Limits;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

/**
 * Controller for the Statistics View.
 * Handles display of various budget statistics and charts.
 */
public class StatisticsController {

    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;

    @FXML private PieChart revenueExpensePieChart;
    @FXML private LineChart<Number, Number> revenueExpenseLineChart;
    @FXML private LineChart<Number, Number> netResultLineChart;
    @FXML private BarChart<String, Number> topItemsBarChart;

    private final BudgetService budgetService =
                                new BudgetService(new BudgetRepository());
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2015;
    private static final int DEFAULT_END_YEAR = 2027;
    private static final int TOP_N_ITEMS = 5;

    /**
     * Initializes the controller by setting up combo boxes and loading charts.
     */
    @FXML
    public void initialize() {
        setupComboBoxes();
        loadAllCharts();
    }

    /**
     * Sets up the year and chart type combo boxes with their options.
     */
    private void setupComboBoxes() {
        // Setup year combo box
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int year = Limits.MIN_BUDGET_YEAR; year <= CURRENT_YEAR; year++) {
            years.add(year);
        }
        yearComboBox.setItems(years);
        yearComboBox.setValue(CURRENT_YEAR);
        yearComboBox.setOnAction(e -> loadAllCharts());

        // Setup chart type combo box (for future use)
        ObservableList<String> chartTypes = FXCollections.observableArrayList(
            "All Charts",
            "Revenue vs Expense",
            "Trends",
            "Top Items"
        );
        chartTypeComboBox.setItems(chartTypes);
        chartTypeComboBox.setValue("All Charts");
        chartTypeComboBox.setOnAction(e -> loadAllCharts());
    }

    /**
     * Loads all charts with data from the BudgetService.
     */
    private void loadAllCharts() {
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        try {
            loadRevenueExpensePieChart(selectedYear);
            loadRevenueExpenseTrendChart();
            loadNetResultTrendChart();
            loadTopItemsBarChart(selectedYear);
        } catch (IllegalArgumentException e) {
            // Handle case where budget doesn't exist for selected year
            clearAllCharts();
        }
    }

    /**
     * Loads the revenue vs expense pie chart for the selected year.
     *
     * @param year the year to display data for
     */
    private void loadRevenueExpensePieChart(int year) {
        revenueExpensePieChart.getData().clear();

        try {
            ObservableList<PieChart.Data> pieData =
                                    budgetService.getRevenueExpensePieData(year);
            
            // Format labels with amounts
            NumberFormat currencyFormat =
                            NumberFormat.getCurrencyInstance(Locale.GERMANY);
            
            for (PieChart.Data data : pieData) {
                String name = data.getName();
                double value = data.getPieValue();
                String formattedValue = currencyFormat.format(value);
                data.setName(name + "\n(" + formattedValue + ")");
            }
            
            revenueExpensePieChart.setData(pieData);
        } catch (IllegalArgumentException e) {
            revenueExpensePieChart.getData().clear();
        }
    }

    /**
     * Loads the revenue and expense trend line chart.
     */
    private void loadRevenueExpenseTrendChart() {
        revenueExpenseLineChart.getData().clear();

        try {
            Map<String, XYChart.Series<Integer, Number>> seriesMap =
                budgetService.getRevenueExpenseTrendSeries(
                    DEFAULT_START_YEAR, DEFAULT_END_YEAR);

            XYChart.Series<Integer, Number> revenueSeries =
                                            seriesMap.get("Revenue");
            XYChart.Series<Integer, Number> expenseSeries =
                                            seriesMap.get("Expense");

            if (revenueSeries != null) {
                XYChart.Series<Number, Number> convertedRevenueSeries =
                                                        convertSeries(revenueSeries);
                revenueExpenseLineChart.getData().add(convertedRevenueSeries);
            }

            if (expenseSeries != null) {
                XYChart.Series<Number, Number> convertedExpenseSeries =
                                                        convertSeries(expenseSeries);
                revenueExpenseLineChart.getData().add(convertedExpenseSeries);
            }
        } catch (IllegalArgumentException e) {
            revenueExpenseLineChart.getData().clear();
        }
    }

    /**
     * Loads the net result trend line chart.
     */
    private void loadNetResultTrendChart() {
        netResultLineChart.getData().clear();

        try {
            XYChart.Series<Integer, Number> netSeries =
                budgetService.getNetResultSeries(
                    DEFAULT_START_YEAR, DEFAULT_END_YEAR);

            if (netSeries != null) {
                XYChart.Series<Number, Number> convertedSeries =
                                                        convertSeries(netSeries);
                netResultLineChart.getData().add(convertedSeries);
            }
        } catch (IllegalArgumentException e) {
            netResultLineChart.getData().clear();
        }
    }

    /**
     * Loads the top budget items bar chart for the selected year.
     *
     * @param year the year to display data for
     */
    private void loadTopItemsBarChart(int year) {
        topItemsBarChart.getData().clear();

        try {
            // Load top revenue items
            XYChart.Series<String, Number> revenueSeries =
                budgetService.getTopBudgetItemsSeries(year, TOP_N_ITEMS, true);
            if (revenueSeries != null && !revenueSeries.getData().isEmpty()) {
                topItemsBarChart.getData().add(revenueSeries);
            }

            // Load top expense items
            XYChart.Series<String, Number> expenseSeries =
                budgetService.getTopBudgetItemsSeries(year, TOP_N_ITEMS, false);
            if (expenseSeries != null && !expenseSeries.getData().isEmpty()) {
                topItemsBarChart.getData().add(expenseSeries);
            }
        } catch (IllegalArgumentException e) {
            topItemsBarChart.getData().clear();
        }
    }

    /**
     * Converts a Series with Integer X values to Number X values
     * for compatibility with NumberAxis.
     *
     * @param series the series to convert
     * @return the converted series
     */
    private XYChart.Series<Number, Number> convertSeries(
            XYChart.Series<Integer, Number> series) {
        XYChart.Series<Number, Number> convertedSeries =
                                        new XYChart.Series<>();
        convertedSeries.setName(series.getName());

        for (XYChart.Data<Integer, Number> data : series.getData()) {
            convertedSeries.getData().add(
                new XYChart.Data<>(data.getXValue(), data.getYValue())
            );
        }

        return convertedSeries;
    }

    /**
     * Clears all charts when data is not available.
     */
    private void clearAllCharts() {
        revenueExpensePieChart.getData().clear();
        revenueExpenseLineChart.getData().clear();
        netResultLineChart.getData().clear();
        topItemsBarChart.getData().clear();
    }
}
