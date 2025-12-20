package budget.frontend.controller;

import budget.frontend.constants.Constants;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.BudgetService;
import budget.constants.Limits;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

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
import javafx.scene.chart.XYChart.Series;

/**
 * Controller for the Statistics View.
 * Handles display of various budget statistics and charts.
 */
public class StatisticsController {

    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<String> categoryComboBox;

    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> revenueExpenseLineChart;
    @FXML private LineChart<Number, Number> netResultLineChart;
    @FXML private BarChart<String, Number> topItemsBarChart;
    @FXML private LineChart<Number, Number> trendLineChart1;
    @FXML private LineChart<Number, Number> trendLineChart2;

    private final BudgetService budgetService =
                                new BudgetService(new BudgetRepository());
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2019;
    private static final int DEFAULT_END_YEAR = 2027;

    /**
     * Initializes the controller by setting up combo boxes and loading charts.
     */
    @FXML
    public void initialize() {
        setupComboBoxes();
        loadTopItems();
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
        yearComboBox.setOnAction(e -> loadSelectedType());

        // Setup chart type combo box
        ObservableList<String> chartTypes = FXCollections.observableArrayList(
            "Top Items",
            "Budget Results"
        );
        chartTypeComboBox.setItems(chartTypes);
        chartTypeComboBox.setValue("Top Items");
        chartTypeComboBox.setOnAction(e -> loadSelectedType());

        // Setup category combo box
        ObservableList<String> categories = FXCollections.observableArrayList(
            "Revenue",
            "Expense"
        );
        categoryComboBox.setItems(categories);
        categoryComboBox.setValue("Revenue");
        categoryComboBox.setOnAction(e -> updateTop5PieChart());
    }

    /**
     * Updates the top 5 pie chart based on the selected year and category.
     * Handles cases where data is not available by clearing the chart
     * and displaying an appropriate message.
     */
    private void updateTop5PieChart() {
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            selectedCategory = "Revenue";
        }

        boolean isRevenue = "Revenue".equals(selectedCategory);

        try {
            loadTop5Pie(selectedYear, isRevenue, selectedCategory);
        } catch (IllegalArgumentException e) {
            pieChart.getData().clear();
            pieChart.setTitle("No data available");
        }
    }

    /**
     * Loads the appropriate charts based on the selected chart type
     * from the chart type combo box.
     * If "Top Items" is selected, loads the top items view.
     * If "Budget Results" is selected, loads the budget charts view.
     * Defaults to loading top items if the selection is null or unrecognized.
     */
    private void loadSelectedType() {
        String selectedType = chartTypeComboBox.getValue();
        if (selectedType != null) {
            switch (selectedType) {
                case "Top Items" ->
                    loadTopItems();
                case "Budget Results" ->
                    loadBudgetCharts();
                default ->
                    loadTopItems();
            }
        }
    }

    /**
     * Loads all charts with data from the BudgetService.
     */
    private void loadBudgetCharts() {
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        try {
            setupVisibilityForAllCharts();
            setupTitlesForAllCharts();
            loadChartsForAllCharts(selectedYear);
        } catch (IllegalArgumentException e) {
            // Handle case where budget doesn't exist for selected year
            clearAllCharts();
        }
    }

    /**
     * Sets up visibility for all charts view.
     */
    private void setupVisibilityForAllCharts() {
        categoryComboBox.setVisible(false);
        categoryComboBox.setManaged(false);
        trendLineChart1.setVisible(false);
        trendLineChart1.setManaged(false);
        trendLineChart2.setVisible(false);
        trendLineChart2.setManaged(false);
    }

    /**
     * Sets up titles for all charts view.
     */
    private void setupTitlesForAllCharts() {
        pieChart.setTitle("Revenue vs Expense Distribution");
        revenueExpenseLineChart.setTitle("Revenue & Expense Trend");
        netResultLineChart.setTitle("Net Result Trend");
        topItemsBarChart.setTitle("Year Comparison");
    }

    /**
     * Loads all charts data for the all charts view.
     *
     * @param selectedYear the selected year
     */
    private void loadChartsForAllCharts(int selectedYear) {
        loadRevenueExpensePieChart(selectedYear);
        loadRevenueExpenseTrendChart();
        loadNetResultTrendChart();
        loadYearComparisonBarChart();
    }

    /**
     * Loads all top items charts with data from the BudgetService.
     * Sets up visibility, titles, and loads all relevant charts for the
     * top items view including trends, pie chart, bar chart, and loans series.
     */
    private void loadTopItems() {
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            selectedCategory = "Revenue";
        }

        boolean isRevenue = "Revenue".equals(selectedCategory);

        try {
            setupVisibilityForTopItems();
            setupTitlesForTopItems(selectedCategory);
            loadChartsForTopItems(selectedYear, isRevenue, selectedCategory);
        } catch (IllegalArgumentException e) {
            clearAllCharts();
        }
    }

    /**
     * Sets up visibility for top items view.
     */
    private void setupVisibilityForTopItems() {
        trendLineChart1.setVisible(true);
        trendLineChart1.setManaged(true);
        trendLineChart2.setVisible(true);
        trendLineChart2.setManaged(true);
        categoryComboBox.setVisible(true);
        categoryComboBox.setManaged(true);
    }

    /**
     * Sets up titles for top items view.
     *
     * @param selectedCategory the selected category
     */
    private void setupTitlesForTopItems(String selectedCategory) {
        pieChart.setTitle(
            String.format("Top 5 %s Distribution", selectedCategory));
        netResultLineChart.setTitle("Top 5 Expenses Trend");
        revenueExpenseLineChart.setTitle("Top 5 Revenues Trend");
        topItemsBarChart.setTitle("Top 5 Expenses & Revenues");
    }

    /**
     * Loads all charts data for the top items view.
     *
     * @param selectedYear the selected year
     * @param isRevenue whether the category is revenue
     * @param selectedCategory the selected category name
     */
    private void loadChartsForTopItems(int selectedYear, boolean isRevenue,
                                       String selectedCategory) {
        loadTop5ExpenseTrend(selectedYear);
        loadTop5RevenueTrend(selectedYear);
        loadTop5Pie(selectedYear, isRevenue, selectedCategory);
        loadTopItemsBarChart(selectedYear);
        loadLoansExpenseSeries();
        loadLoansRevenueSeries();
    }

    /**
     * Loads the loans revenue trend series into trendLineChart1.
     * Clears existing data and sets up the year axis formatter before
     * adding the revenue loans trend data.
     */
    private void loadLoansRevenueSeries() {
        trendLineChart1.getData().clear();
        setupYearAxisFormatter(trendLineChart1);
        Series<Number, Number> series = budgetService.getLoansTrendSeries(
                                                            DEFAULT_START_YEAR,
                                                            DEFAULT_END_YEAR,
                                                            true
                                                        );
        trendLineChart1.getData().add(series);
    }

    /**
     * Loads the year comparison bar chart comparing revenue and expense
     * between the selected year and the previous year.
     */
    private void loadYearComparisonBarChart() {
        topItemsBarChart.getData().clear();

        try {
            Integer selectedYear = yearComboBox.getValue();
            if (selectedYear == null) {
                selectedYear = CURRENT_YEAR;
            }
            int previousYear = selectedYear - 1;
            Map<String, XYChart.Series<String, Number>> seriesMap =
                budgetService.getYearComparisonSeries(
                                            selectedYear, previousYear);

            // Add both series to the chart
            XYChart.Series<String, Number> year1Series =
                                seriesMap.get(String.valueOf(selectedYear));
            XYChart.Series<String, Number> year2Series =
                                seriesMap.get(String.valueOf(previousYear));

            if (year1Series != null && !year1Series.getData().isEmpty()) {
                topItemsBarChart.getData().add(year1Series);
            }

            if (year2Series != null && !year2Series.getData().isEmpty()) {
                topItemsBarChart.getData().add(year2Series);
            }
        } catch (IllegalArgumentException e) {
            topItemsBarChart.getData().clear();
        }
    }

    /**
     * Loads the loans expense trend series into trendLineChart2.
     * Clears existing data and sets up the year axis formatter before
     * adding the expense loans trend data.
     */
    private void loadLoansExpenseSeries() {
        trendLineChart2.getData().clear();
        setupYearAxisFormatter(trendLineChart2);
        Series<Number, Number> series = budgetService.getLoansTrendSeries(
                                                            DEFAULT_START_YEAR,
                                                            DEFAULT_END_YEAR,
                                                            false
                                                        );
        trendLineChart2.getData().add(series);
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
     * Loads the top 5 revenue items trend line chart for the specified year.
     * Displays the trend of the top revenue items across multiple years.
     *
     * @param year the year to determine top items from
     */
    private void loadTop5RevenueTrend(int year) {
        setupYearAxisFormatter(revenueExpenseLineChart);
        revenueExpenseLineChart.getData().clear();
         try {
            Map<String, Series<Number, Number>> seriesMap =
                budgetService.getTopItemsTrendSeries(
                                                year,
                                                DEFAULT_START_YEAR,
                                                DEFAULT_END_YEAR,
                                                Constants.TOP_N_ITEMS,
                                                true
                                            );
            for (var valueSeries : seriesMap.values()) {
                revenueExpenseLineChart.getData().add(valueSeries);
            }
         } catch (IllegalArgumentException e) {
            revenueExpenseLineChart.getData().clear();
         }
    }

    /**
     * Loads the top 5 expense items trend line chart for the specified year.
     * Displays the trend of the top expense items across multiple years.
     *
     * @param year the year to determine top items from
     */
    private void loadTop5ExpenseTrend(int year) {
        setupYearAxisFormatter(netResultLineChart);
        netResultLineChart.getData().clear();
         try {
            Map<String, Series<Number, Number>> seriesMap =
                budgetService.getTopItemsTrendSeries(
                                                year,
                                                DEFAULT_START_YEAR,
                                                DEFAULT_END_YEAR,
                                                Constants.TOP_N_ITEMS,
                                                false
                                            );
            for (var valueSeries : seriesMap.values()) {
                netResultLineChart.getData().add(valueSeries);
            }
         } catch (IllegalArgumentException e) {
            netResultLineChart.getData().clear();
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
                budgetService.getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, true, false);
            if (revenueSeries != null && !revenueSeries.getData().isEmpty()) {
                topItemsBarChart.getData().add(revenueSeries);
            }

            // Load top expense items
            XYChart.Series<String, Number> expenseSeries =
                budgetService.getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, false, false);
            if (expenseSeries != null && !expenseSeries.getData().isEmpty()) {
                topItemsBarChart.getData().add(expenseSeries);
            }
        } catch (IllegalArgumentException e) {
            topItemsBarChart.getData().clear();
        }
    }

    /**
     * Loads the top 5 items pie chart for the specified year and category.
     * Displays the distribution of top revenue or expense items.
     *
     * @param year the year to display data for
     * @param isRevenue true if displaying revenue items, false for expenses
     * @param categoryName the name of the category for display purposes
     */
    private void loadTop5Pie(int year, boolean isRevenue, String categoryName) {
        pieChart.getData().clear();

        try {
            ObservableList<PieChart.Data> pieData =
                budgetService.getBudgetItemsforPie(year, isRevenue);

            if (pieData.isEmpty()) {
                pieChart.setTitle(String.format(
                    "Top 5 %s Distribution - No data available",
                    categoryName)
                );
                return;
            }

            pieChart.setData(pieData);
            pieChart.setTitle(String.format(
                "Top 5 %s Distribution", categoryName));
        } catch (IllegalArgumentException e) {
            pieChart.setTitle(String.format(
                "Top 5 %s Distribution - Year %d not found",
                categoryName, year));
            pieChart.getData().clear();
        }
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
