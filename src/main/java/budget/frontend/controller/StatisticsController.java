package budget.frontend.controller;

import java.util.Map;

import budget.backend.repository.BudgetRepository;
import budget.backend.service.StatisticsService;
import budget.frontend.constants.Constants;
import budget.frontend.util.BarChartViewModel;
import budget.frontend.util.ChartConfigurationHelper.BudgetResultsViewConfig;
import budget.frontend.util.ChartConfigurationHelper.TopItemsViewConfig;
import budget.frontend.util.ChartTitles;
import budget.frontend.util.PieChartViewModel;
import budget.frontend.util.TrendLineChartViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Controller for the Statistics View.
 * Handles display of various budget statistics and charts.
 */
public class StatisticsController {

    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> expenseComboBox;
    @FXML private ComboBox<String> revenueComboBox;

    @FXML private Label categoryLabel;
    @FXML private Label revenueLabel;
    @FXML private Label expenseLabel;

    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> revenueExpenseLineChart;
    @FXML private LineChart<Number, Number> netResultLineChart;
    @FXML private BarChart<String, Number> topItemsBarChart;
    @FXML private LineChart<Number, Number> trendLineChart1;
    @FXML private LineChart<Number, Number> trendLineChart2;


    private final StatisticsService statisticsService =
                                new StatisticsService(new BudgetRepository());
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2019;
    private static final int DEFAULT_END_YEAR = 2027;

    private PieChartViewModel pieChartVM;
    private TrendLineChartViewModel revenueExpenseLineChartVM;
    private TrendLineChartViewModel netResultLineChartVM;
    private TrendLineChartViewModel trendLineChart1VM;
    private TrendLineChartViewModel trendLineChart2VM;
    private BarChartViewModel topItemsBarChartVM;

    /**
     * Initializes the controller by setting up combo boxes and loading charts.
     */
    @FXML
    public void initialize() {
        initializeViewModels();
        setupComboBoxes();
        loadTopItems();
    }

    /**
     * Initializes ViewModels for all charts.
     */
    private void initializeViewModels() {
        pieChartVM = new PieChartViewModel(pieChart);
        revenueExpenseLineChartVM = new TrendLineChartViewModel(
                                                    revenueExpenseLineChart);
        netResultLineChartVM = new TrendLineChartViewModel(netResultLineChart);
        trendLineChart1VM = new TrendLineChartViewModel(trendLineChart1);
        trendLineChart2VM = new TrendLineChartViewModel(trendLineChart2);
        topItemsBarChartVM = new BarChartViewModel(topItemsBarChart);
    }

    /**
     * Gets the selected year from combo box or returns default.
     * @return selected year or CURRENT_YEAR if null
     */
    private int getSelectedYear() {
        Integer year = yearComboBox.getValue();
        return year != null ? year : CURRENT_YEAR;
    }

    /**
     * Gets the selected category from combo box or returns default.
     * @return selected category or "Revenue" if null
     */
    private String getSelectedCategory() {
        String category = categoryComboBox.getValue();
        return category != null ? category : "Revenue";
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

        // Setup expense and revenue combo boxes
        updateRevenueOrExpenseComboBox(true);
        updateRevenueOrExpenseComboBox(false);
    }

    /**
    * Updates the expense or revenue combo box with "all" option
    * and top 5 items for the selected year.
    * @param isRevenue true if revenueComboBox is selected
    *                   false if expenseCombobox is selected
    */
   private void updateRevenueOrExpenseComboBox(boolean isRevenue) {
        Integer selectedYear = getSelectedYear();
        ObservableList<String> items = statisticsService.getTopItemsForComboBox(
            selectedYear, Constants.TOP_N_ITEMS, isRevenue);
        ComboBox<String> targetComboBox = isRevenue
            ? revenueComboBox
            : expenseComboBox;
        targetComboBox.setItems(items);
        targetComboBox.setValue("All");
   }

    /**
     * Updates the top 5 pie chart based on the selected year and category.
     * Handles cases where data is not available by clearing the chart
     * and displaying an appropriate message.
     */
    private void updateTop5PieChart() {
        int selectedYear = getSelectedYear();
        String selectedCategory = getSelectedCategory();
        boolean isRevenue = "Revenue".equals(selectedCategory);

        try {
            loadTop5Pie(selectedYear, isRevenue, selectedCategory);
        } catch (IllegalArgumentException e) {
            pieChartVM.showError("No data available");
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
     * Loads all charts with data from the StatisticsService.
     */
    private void loadBudgetCharts() {
        int selectedYear = getSelectedYear();

        try {
            setupVisibilityForAllCharts();
            setupTitlesForAllCharts();
            loadChartsForAllCharts(selectedYear);
        } catch (IllegalArgumentException e) {
            clearAllCharts();
        }
    }

    /**
     * Sets up visibility for all charts view.
     */
    private void setupVisibilityForAllCharts() {
        BudgetResultsViewConfig.applyVisibility(
            categoryComboBox, revenueComboBox, expenseComboBox,
            categoryLabel, revenueLabel, expenseLabel,
            trendLineChart1, trendLineChart2
        );
    }

    /**
     * Sets up titles for all charts view.
     */
    private void setupTitlesForAllCharts() {
        ChartTitles titles = BudgetResultsViewConfig.getTitles();
        pieChartVM.setTitle(titles.getPieChartTitle());
        revenueExpenseLineChartVM.setTitle(titles.getLineChart1Title());
        netResultLineChartVM.setTitle(titles.getLineChart2Title());
        topItemsBarChartVM.setTitle(titles.getBarChartTitle());
    }

    /**
     * Loads all charts data for the all charts view.
     *
     * @param selectedYear the selected year
     */
    private void loadChartsForAllCharts(int selectedYear) {
        setupRevenueComboBoxForBudgetResults();
        loadRevenueExpensePieChart(selectedYear);
        loadRevenueExpenseTrendChart();
        loadNetResultTrendChart();
        loadYearComparisonBarChart();
    }

    /**
     * Sets up revenueComboBox with Revenue/Expense options
     *                                          for Budget Results view.
     */
    private void setupRevenueComboBoxForBudgetResults() {
        ObservableList<String> revenueExpenseOptions =
            FXCollections.observableArrayList("Revenue", "Expense");
        revenueComboBox.setItems(revenueExpenseOptions);
        revenueComboBox.setValue("Revenue");
        revenueComboBox.setOnAction(e -> loadRevenueExpenseTrendChart());
    }

    /**
     * Loads all top items charts with data from the StatisticsService.
     * Sets up visibility, titles, and loads all relevant charts for the
     * top items view including trends, pie chart, bar chart, and loans series.
     */
    private void loadTopItems() {
        int selectedYear = getSelectedYear();
        String selectedCategory = getSelectedCategory();
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
        TopItemsViewConfig.applyVisibility(
            categoryComboBox, revenueComboBox, expenseComboBox,
            categoryLabel, revenueLabel, expenseLabel,
            trendLineChart1, trendLineChart2
        );
        revenueComboBox.setOnAction(e -> updateTrendChart(true));
        expenseComboBox.setOnAction(e -> updateTrendChart(false));
    }

    /**
     * Sets up titles for top items view.
     *
     * @param selectedCategory the selected category
     */
    private void setupTitlesForTopItems(String selectedCategory) {
        ChartTitles titles = TopItemsViewConfig.getTitles(selectedCategory);
        pieChartVM.setTitle(titles.getPieChartTitle());
        netResultLineChartVM.setTitle(titles.getLineChart1Title());
        revenueExpenseLineChartVM.setTitle(titles.getLineChart2Title());
        topItemsBarChartVM.setTitle(titles.getBarChartTitle());
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
        updateRevenueOrExpenseComboBox(true);  // Update revenue combo
        updateRevenueOrExpenseComboBox(false); // Update expense combo
        loadTop5ExpenseTrend(selectedYear);
        loadTop5RevenueTrend(selectedYear);
        loadTop5Pie(selectedYear, isRevenue, selectedCategory);
        loadTopItemsBarChart(selectedYear);
        loadLoansExpenseSeries();
        loadLoansRevenueSeries();
    }

    /**
     * Loads the loans revenue trend series into trendLineChart1.
     */
    private void loadLoansRevenueSeries() {
        trendLineChart1VM.clear();
        Map<String, Series<Number, Number>> seriesMap =
                            statisticsService.getLoansTrendWithRegression(true);
        trendLineChart1VM.loadSeriesWithRegression(seriesMap);
    }

    /**
     * Updates the top 5 revenue items trend line chart.
     * @param year Selected year
     */
    private void loadTop5RevenueTrend(int year) {
        loadTop5ItemsTrend(year, true, revenueExpenseLineChartVM);
    }

    /**
     * Updates the top 5 expense items trend line chart.
     * @param year Selected year
     */
    private void loadTop5ExpenseTrend(int year) {
        loadTop5ItemsTrend(year, false, netResultLineChartVM);
    }

    /**
     * Loads the year comparison bar chart comparing revenue and expense
     * between the selected year and the previous year.
     */
    private void loadYearComparisonBarChart() {
        topItemsBarChartVM.clear();

        try {
            int selectedYear = getSelectedYear();
            int previousYear = selectedYear - 1;
            Map<String, XYChart.Series<String, Number>> seriesMap =
                statisticsService.getBudgetService().getYearComparisonSeries(
                    selectedYear, previousYear);
            topItemsBarChartVM.loadSeries(seriesMap);
        } catch (IllegalArgumentException e) {
            topItemsBarChartVM.clear();
        }
    }

    /**
     * Loads the loans expense trend series into trendLineChart2.
     */
    private void loadLoansExpenseSeries() {
        trendLineChart2VM.clear();
        Map<String, Series<Number, Number>> seriesMap =
                        statisticsService.getLoansTrendWithRegression(false);
        trendLineChart2VM.loadSeriesWithRegression(seriesMap);
    }
    /**
     * Loads the revenue vs expense pie chart for the selected year.
     *
     * @param year the year to display data for
     */
    private void loadRevenueExpensePieChart(int year) {
        try {
            ObservableList<PieChart.Data> pieData =
                    statisticsService.getFormattedRevenueExpensePieData(year);
            pieChartVM.loadData(pieData);
        } catch (IllegalArgumentException e) {
            pieChartVM.clear();
        }
    }

    /**
     * Loads the revenue and expense trend line chart.
     */
    private void loadRevenueExpenseTrendChart() {
        revenueExpenseLineChartVM.clear();

        try {
            String selected = revenueComboBox.getValue();
            if (selected == null) {
                selected = "Revenue";
            }

            boolean isRevenue = "Revenue".equals(selected);
            String title = isRevenue ? "Revenue Trend " : "Expense Trend ";
            revenueExpenseLineChartVM.setTitle(title);

            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getTrendWithRegression(
                    DEFAULT_START_YEAR, DEFAULT_END_YEAR, isRevenue);
            revenueExpenseLineChartVM.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            revenueExpenseLineChartVM.clear();
        }
    }

    /**
     * Loads the top 5 revenue items trend line chart for the specified year.
     * Can display either revenue or expense items.
     *
     * @param year the year to determine top items from
     * @param isRevenue true for revenue items, false for expense items
     * @param chartVM the chart view model to update
     */
    private void loadTop5ItemsTrend(int year, boolean isRevenue,
                                    TrendLineChartViewModel chartVM) {
         try {
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getBudgetService().getTopItemsTrendSeries(
                    year,
                    DEFAULT_START_YEAR,
                    DEFAULT_END_YEAR,
                    Constants.TOP_N_ITEMS,
                    isRevenue
                );
            chartVM.loadMultipleSeries(seriesMap);
         } catch (IllegalArgumentException e) {
            chartVM.clear();
         }
    }

    /**
     * Updates the trend chart based on selected item filter.
     *
     * @param isRevenue true for revenue, false for expense
     */
    private void updateTrendChart(boolean isRevenue) {
        int selectedYear = getSelectedYear();

        ComboBox<String> targetComboBox = isRevenue
            ? revenueComboBox
            : expenseComboBox;
        TrendLineChartViewModel targetChartVM = isRevenue
            ? revenueExpenseLineChartVM
            : netResultLineChartVM;

        String selectedItem = targetComboBox.getValue();
        if (selectedItem == null || "All".equals(selectedItem)) {
            String trendType = isRevenue ? "Revenues" : "Expenses";
            targetChartVM.setTitle(String.format("Top 5 %s Trend ", trendType));
            loadTop5ItemsTrend(selectedYear, isRevenue, targetChartVM);
        } else {
            targetChartVM.setTitle(String.format("%s Trend ", selectedItem));
            loadSingleItemTrend(targetChartVM, selectedYear,
                                selectedItem, isRevenue);
        }
    }

    /**
     * Loads a single item trend chart.
     *
     * @param chartVM the chart view model to update
     * @param referenceYear the reference year
     * @param itemName the name of the item to display
     * @param isRevenue true for revenue, false for expense
     */
    private void loadSingleItemTrend(TrendLineChartViewModel chartVM,
                                    int referenceYear,
                                    String itemName,
                                    boolean isRevenue) {
        chartVM.clear();

        try {
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getSingleItemTrendWithRegression(
                    referenceYear, itemName, isRevenue);
            chartVM.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            chartVM.clear();
        }
    }

    /**
     * Loads the net result trend line chart.
     */
    private void loadNetResultTrendChart() {
        netResultLineChartVM.clear();

        try {
            Map<String, Series<Number, Number>> seriesMap =
                                statisticsService.getNetResultWithRegression();
            netResultLineChartVM.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            netResultLineChartVM.clear();
        }
    }

    /**
     * Loads the top budget items bar chart for the selected year.
     *
     * @param year the year to display data for
     */
    private void loadTopItemsBarChart(int year) {
        topItemsBarChartVM.clear();

        try {
            // Load top revenue and expense items
            XYChart.Series<String, Number> revenueSeries =
                statisticsService.getBudgetService().getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, true, false);
            XYChart.Series<String, Number> expenseSeries =
                statisticsService.getBudgetService().getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, false, false);
            topItemsBarChartVM.loadTwoSeries(revenueSeries, expenseSeries);
        } catch (IllegalArgumentException e) {
            topItemsBarChartVM.clear();
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
        pieChartVM.clear();

        try {
            ObservableList<PieChart.Data> pieData =
                statisticsService.getBudgetService()
                                .getBudgetItemsforPie(year, isRevenue);

            if (pieData.isEmpty()) {
                pieChartVM.showError(String.format(
                    "Top 5 %s Distribution - No data available", categoryName));
                return;
            }

            pieChartVM.loadData(pieData);
            pieChartVM.setTitle(String.format(
                "Top 5 %s Distribution", categoryName));
        } catch (IllegalArgumentException e) {
            pieChartVM.showError(String.format(
                "Top 5 %s Distribution - Year %d not found",
                categoryName, year));
        }
    }

    /**
     * Clears all charts when data is not available.
     */
    private void clearAllCharts() {
        pieChartVM.clear();
        revenueExpenseLineChartVM.clear();
        netResultLineChartVM.clear();
        topItemsBarChartVM.clear();
    }
}
