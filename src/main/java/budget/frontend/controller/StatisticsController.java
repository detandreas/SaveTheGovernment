package budget.frontend.controller;

import java.util.Map;

import budget.backend.repository.BudgetRepository;
import budget.backend.service.StatisticsService;
import budget.frontend.constants.Constants;
import budget.frontend.util.BarChartViewModel;
import budget.frontend.util.PieChartViewModel;
import budget.frontend.util.TrendLineChartViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
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
    
    // View Models
    private PieChartViewModel pieChartViewModel;
    private TrendLineChartViewModel revenueExpenseViewModel;
    private TrendLineChartViewModel netResultViewModel;
    private BarChartViewModel topItemsBarChartViewModel;
    private TrendLineChartViewModel trendViewModel1;
    private TrendLineChartViewModel trendViewModel2;
    
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2019;
    private static final int DEFAULT_END_YEAR = 2027;

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
     * Initializes all view models for chart management.
     */
    private void initializeViewModels() {
        pieChartViewModel = new PieChartViewModel(pieChart);
        revenueExpenseViewModel = new TrendLineChartViewModel(revenueExpenseLineChart);
        netResultViewModel = new TrendLineChartViewModel(netResultLineChart);
        topItemsBarChartViewModel = new BarChartViewModel(topItemsBarChart);
        trendViewModel1 = new TrendLineChartViewModel(trendLineChart1);
        trendViewModel2 = new TrendLineChartViewModel(trendLineChart2);
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
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        ComboBox<String> targetComboBox = isRevenue
            ? revenueComboBox
            : expenseComboBox;
        
        ObservableList<String> items = statisticsService.getTopItemsForComboBox(
            selectedYear, 
            Constants.TOP_N_ITEMS, 
            isRevenue
        );
        targetComboBox.setItems(items);
        targetComboBox.setValue("All");
   }

    /**
     * Updates the top 5 pie chart based on the selected year and category.
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
            pieChartViewModel.showError("No data available");
        }
    }

    /**
     * Loads the appropriate charts based on the selected chart type.
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
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

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
        revenueComboBox.setVisible(true);
        revenueComboBox.setManaged(true);

        categoryComboBox.setVisible(false);
        categoryComboBox.setManaged(false);
        expenseComboBox.setVisible(false);
        expenseComboBox.setManaged(false);
        revenueLabel.setVisible(false);
        revenueLabel.setManaged(false);
        expenseLabel.setVisible(false);
        expenseLabel.setManaged(false);
        categoryLabel.setVisible(false);
        categoryLabel.setManaged(false);
        
        trendViewModel1.setVisible(false);
        trendViewModel2.setVisible(false);
    }

    /**
     * Sets up titles for all charts view.
     */
    private void setupTitlesForAllCharts() {
        pieChartViewModel.setTitle("Revenue vs Expense Distribution ");
        revenueExpenseViewModel.setTitle("Revenue & Expense Trend ");
        netResultViewModel.setTitle("Net Result Trend ");
        topItemsBarChartViewModel.setTitle("Year Comparison ");
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
        trendViewModel1.setVisible(true);
        trendViewModel2.setVisible(true);
        
        revenueComboBox.setVisible(true);
        revenueComboBox.setManaged(true);
        expenseComboBox.setVisible(true);
        expenseComboBox.setManaged(true);
        revenueLabel.setVisible(true);
        revenueLabel.setManaged(true);
        expenseLabel.setVisible(true);
        expenseLabel.setManaged(true);
        categoryLabel.setVisible(true);
        categoryLabel.setManaged(true);
        categoryComboBox.setVisible(true);
        categoryComboBox.setManaged(true);
        
        revenueComboBox.setOnAction(e -> updateTrendChart(true));
        expenseComboBox.setOnAction(e -> updateTrendChart(false));
    }

    /**
     * Sets up titles for top items view.
     *
     * @param selectedCategory the selected category
     */
    private void setupTitlesForTopItems(String selectedCategory) {
        pieChartViewModel.setTitle(
            String.format("Top 5 %s Distribution ", selectedCategory));
        netResultViewModel.setTitle("Top 5 Expenses Trend ");
        revenueExpenseViewModel.setTitle("Top 5 Revenues Trend ");
        topItemsBarChartViewModel.setTitle("Top 5 Expenses & Revenues ");
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
        updateRevenueOrExpenseComboBox(true);
        updateRevenueOrExpenseComboBox(false);
        loadTop5ExpenseTrend(selectedYear);
        loadTop5RevenueTrend(selectedYear);
        loadTop5Pie(selectedYear, isRevenue, selectedCategory);
        loadTopItemsBarChart(selectedYear);
        loadLoansRevenueSeries();
        loadLoansExpenseSeries();
    }

    /**
     * Loads the loans revenue trend series.
     */
    private void loadLoansRevenueSeries() {
        trendViewModel1.clear();
        try {
            Map<String, Series<Number, Number>> seriesMap = 
                statisticsService.getLoansTrendWithRegression(true);
            trendViewModel1.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            trendViewModel1.clear();
        }
    }

    /**
     * Loads the loans expense trend series.
     */
    private void loadLoansExpenseSeries() {
        trendViewModel2.clear();
        try {
            Map<String, Series<Number, Number>> seriesMap = 
                statisticsService.getLoansTrendWithRegression(false);
            trendViewModel2.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            trendViewModel2.clear();
        }
    }

    /**
     * Updates the top 5 revenue items trend line chart.
     * @param year Selected year
     */
    private void loadTop5RevenueTrend(int year) {
        loadTop5ItemsTrend(year, true, revenueExpenseViewModel);
    }

    /**
     * Updates the top 5 expense items trend line chart.
     * @param year Selected year
     */
    private void loadTop5ExpenseTrend(int year) {
        loadTop5ItemsTrend(year, false, netResultViewModel);
    }

    /**
     * Loads the year comparison bar chart.
     */
    private void loadYearComparisonBarChart() {
        topItemsBarChartViewModel.clear();

        try {
            Integer selectedYear = yearComboBox.getValue();
            if (selectedYear == null) {
                selectedYear = CURRENT_YEAR;
            }
            int previousYear = selectedYear - 1;
            Map<String, Series<String, Number>> seriesMap =
                statisticsService.getBudgetService()
                    .getYearComparisonSeries(selectedYear, previousYear);

            topItemsBarChartViewModel.loadSeries(seriesMap);
        } catch (IllegalArgumentException e) {
            topItemsBarChartViewModel.clear();
        }
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
            pieChartViewModel.loadData(pieData);
        } catch (IllegalArgumentException e) {
            pieChartViewModel.clear();
        }
    }

    /**
     * Loads the revenue and expense trend line chart.
     */
    private void loadRevenueExpenseTrendChart() {
        revenueExpenseViewModel.clear();

        try {
            String selected = revenueComboBox.getValue();
            if (selected == null) {
                selected = "Revenue";
            }

            boolean isRevenue = "Revenue".equals(selected);
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getTrendWithRegression(
                    DEFAULT_START_YEAR, 
                    DEFAULT_END_YEAR, 
                    isRevenue
                );

            String title = isRevenue ? "Revenue Trend " : "Expense Trend ";
            revenueExpenseViewModel.setTitle(title);
            revenueExpenseViewModel.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            revenueExpenseViewModel.clear();
        }
    }

    /**
     * Loads the top 5 revenue items trend line chart for the specified year.
     *
     * @param year the year to determine top items from
     * @param isRevenue true for revenue items, false for expense items
     * @param viewModel the view model to update
     */
    private void loadTop5ItemsTrend(int year, boolean isRevenue,
                                    TrendLineChartViewModel viewModel) {
        viewModel.clear();
        try {
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getBudgetService().getTopItemsTrendSeries(
                    year,
                    DEFAULT_START_YEAR,
                    DEFAULT_END_YEAR,
                    Constants.TOP_N_ITEMS,
                    isRevenue
                );
            viewModel.loadMultipleSeries(seriesMap);
        } catch (IllegalArgumentException e) {
            viewModel.clear();
        }
    }

    /**
     * Updates the trend chart based on selected item filter.
     *
     * @param isRevenue true for revenue, false for expense
     */
    private void updateTrendChart(boolean isRevenue) {
        Integer selectedYear = yearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }

        ComboBox<String> targetComboBox = isRevenue
            ? revenueComboBox
            : expenseComboBox;
        TrendLineChartViewModel targetViewModel = isRevenue
            ? revenueExpenseViewModel
            : netResultViewModel;

        String selectedItem = targetComboBox.getValue();
        if (selectedItem == null || "All".equals(selectedItem)) {
            String trendType = isRevenue ? "Revenues" : "Expenses";
            targetViewModel.setTitle(String.format("Top 5 %s Trend ", trendType));
            loadTop5ItemsTrend(selectedYear, isRevenue, targetViewModel);
        } else {
            targetViewModel.setTitle(String.format("%s Trend ", selectedItem));
            loadSingleItemTrend(targetViewModel, selectedYear,
                                selectedItem, isRevenue);
        }
    }

    /**
     * Loads a single item trend chart.
     *
     * @param viewModel the view model to update
     * @param referenceYear the reference year
     * @param itemName the name of the item to display
     * @param isRevenue true for revenue, false for expense
     */
    private void loadSingleItemTrend(TrendLineChartViewModel viewModel,
                                    int referenceYear,
                                    String itemName,
                                    boolean isRevenue) {
        viewModel.clear();

        try {
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getSingleItemTrendWithRegression(
                    referenceYear,
                    itemName,
                    isRevenue
                );
            viewModel.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            viewModel.clear();
        }
    }

    /**
     * Loads the net result trend line chart.
     */
    private void loadNetResultTrendChart() {
        netResultViewModel.clear();

        try {
            Map<String, Series<Number, Number>> seriesMap =
                statisticsService.getNetResultWithRegression();
            netResultViewModel.loadSeriesWithRegression(seriesMap);
        } catch (IllegalArgumentException e) {
            netResultViewModel.clear();
        }
    }

    /**
     * Loads the top budget items bar chart for the selected year.
     *
     * @param year the year to display data for
     */
    private void loadTopItemsBarChart(int year) {
        try {
            // Load top revenue items
            Series<String, Number> revenueSeries =
                statisticsService.getBudgetService().getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, true, false);
            
            // Load top expense items
            Series<String, Number> expenseSeries =
                statisticsService.getBudgetService().getTopBudgetItemsSeries(
                    year, Constants.TOP_N_ITEMS, false, false);
            
            topItemsBarChartViewModel.loadTwoSeries(revenueSeries, expenseSeries);
        } catch (IllegalArgumentException e) {
            topItemsBarChartViewModel.clear();
        }
    }

    /**
     * Loads the top 5 items pie chart for the specified year and category.
     *
     * @param year the year to display data for
     * @param isRevenue true if displaying revenue items, false for expenses
     * @param categoryName the name of the category for display purposes
     */
    private void loadTop5Pie(int year, boolean isRevenue, String categoryName) {
        pieChartViewModel.clear();

        try {
            ObservableList<PieChart.Data> pieData =
                statisticsService.getBudgetService()
                    .getBudgetItemsforPie(year, isRevenue);

            if (pieData.isEmpty()) {
                pieChartViewModel.setTitle(String.format(
                    "Top 5 %s Distribution - No data available",
                    categoryName)
                );
                return;
            }

            pieChartViewModel.loadData(pieData);
            pieChartViewModel.setTitle(String.format(
                "Top 5 %s Distribution", categoryName));
        } catch (IllegalArgumentException e) {
            pieChartViewModel.showError(String.format(
                "Top 5 %s Distribution - Year %d not found",
                categoryName, year));
        }
    }

    /**
     * Clears all charts when data is not available.
     */
    private void clearAllCharts() {
        pieChartViewModel.clear();
        revenueExpenseViewModel.clear();
        netResultViewModel.clear();
        topItemsBarChartViewModel.clear();
    }
}
