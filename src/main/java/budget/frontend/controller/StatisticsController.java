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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * Controller for the Statistics View.
 * Handles display of various budget statistics and charts.
 */
public class StatisticsController {

    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;

    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> revenueExpenseLineChart;
    @FXML private LineChart<Number, Number> netResultLineChart;
    @FXML private BarChart<String, Number> topItemsBarChart;

    private final BudgetService budgetService =
                                new BudgetService(new BudgetRepository());
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2019;
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
        for (int year = CURRENT_YEAR; year >= DEFAULT_START_YEAR; year--) {
            years.add(year);
        }
        yearComboBox.setItems(years);
        yearComboBox.setValue(CURRENT_YEAR);
        yearComboBox.setOnAction(e -> loadAllCharts());

        // Setup chart type combo box (for future use)
        ObservableList<String> chartTypes = FXCollections.observableArrayList(
            "Top Items",
            "Budget Results"
        );
        chartTypeComboBox.setItems(chartTypes);
        chartTypeComboBox.setValue("Top Items");
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
        pieChart.getData().clear();

        try {
            ObservableList<PieChart.Data> pieData =
                                budgetService.getRevenueExpensePieData(year);

            double total = pieData.stream()
                                    .mapToDouble(data -> data.getPieValue())
                                    .sum();

            // Format labels with amounts
            NumberFormat currencyFormat =
                            NumberFormat.getCurrencyInstance(Locale.GERMANY);

            for (PieChart.Data data : pieData) {
                String name = data.getName();
                double value = data.getPieValue();
                double pct = (value / total) * Limits.NUMBER_ONE_HUNDRED;
                String formattedValue = currencyFormat.format(value);
                String pctFormatted = String.format("%.2f", pct);
                data.setName(name + "\n("
                    + formattedValue + ")" + "\n"
                    + pctFormatted + "%"
                );
            }

            pieChart.setData(pieData);
        } catch (IllegalArgumentException e) {
            pieChart.getData().clear();
        }
    }

    /**
     * Sets up the X-axis formatter for LineChart to display years
     *                                          without decimal separators.
     *
     * @param chart the LineChart to configure
     */
    private void setupYearAxisFormatter(LineChart<Number, Number> chart) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) {
                    return "";
                }
                // Convert to int and format as string
                // to avoid decimal formatting
                return String.valueOf(object.intValue());
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });
    }

    /**
     * Loads the revenue and expense trend line chart.
     */
    private void loadRevenueExpenseTrendChart() {
        revenueExpenseLineChart.getData().clear();
        setupYearAxisFormatter(revenueExpenseLineChart);

        try {
            Map<String, XYChart.Series<Number, Number>> seriesMap =
                budgetService.getRevenueExpenseTrendSeries(
                    DEFAULT_START_YEAR, DEFAULT_END_YEAR);

            XYChart.Series<Number, Number> revenueSeries =
                                            seriesMap.get("Revenue");
            XYChart.Series<Number, Number> expenseSeries =
                                            seriesMap.get("Expense");

                revenueExpenseLineChart.getData().add(revenueSeries);
                revenueExpenseLineChart.getData().add(expenseSeries);
        } catch (IllegalArgumentException e) {
                revenueExpenseLineChart.getData().clear();
        }
    }

    /**
     * Loads the net result trend line chart.
     */
    private void loadNetResultTrendChart() {
        netResultLineChart.getData().clear();
        setupYearAxisFormatter(netResultLineChart);

        try {
            XYChart.Series<Number, Number> netSeries =
                budgetService.getNetResultSeries(
                    DEFAULT_START_YEAR, DEFAULT_END_YEAR);

                netResultLineChart.getData().add(netSeries);
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

    private void loadTop5Pie(int year, boolean isRevenue) {
        ObservableList<PieChart.Data> pieData =
                                budgetService.getBudgetItemsforPie(year, isRevenue);

        pieChart.setData(pieData);
        pieChart.setTitle(isRevenue ?
                                    "Top revenue budget items" :
                                    "Top expense budget items"
                                    );
        
    }

    /**
     * Clears all charts when data is not available.
     */
    private void clearAllCharts() {
        pieChart.getData().clear();
        revenueExpenseLineChart.getData().clear();
        netResultLineChart.getData().clear();
        topItemsBarChart.getData().clear();
    }
}
