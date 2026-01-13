package budget.backend.service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import budget.backend.repository.BudgetRepository;
import budget.constants.Limits;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Series;


/**
 * Service layer for statistics-related business logic.
 * Provides specialized methods for preparing statistical data
 *                                              and chart configurations.
 */
public class StatisticsService {

    private final BudgetService budgetService;
    private static final int DEFAULT_START_YEAR = 2019;
    private static final int DEFAULT_END_YEAR = 2027;
    private static final int DEFAULT_TOP_N = 5;
    private static final String REGRESSION_KEY = "regression";
    private static final String DATA_KEY = "data";

    /**
     * Service constructor.
     * @param repo BudgetRepository used to initialize budgetService instance
     */
    public StatisticsService(BudgetRepository repo) {
        this.budgetService = new BudgetService(repo);
    }

     /**
     * Retrieves revenue vs expense pie data with formatted labels.
     *
     * @param year the year
     * @return formatted pie data with percentages
     */
    public ObservableList<PieChart.Data>
                            getFormattedRevenueExpensePieData(int year) {
        ObservableList<PieChart.Data> pieData =
                        budgetService.getRevenueExpensePieData(year);

        double total = pieData.stream()
                            .mapToDouble(data -> data.getPieValue())
                            .sum();

        NumberFormat currencyFormat = NumberFormat
                                        .getCurrencyInstance(Locale.GERMANY);

        for (var data : pieData) {
            String name = data.getName();
            double value = data.getPieValue();
            double pct = (value / total) * Limits.NUMBER_ONE_HUNDRED;
            String formattedValue = currencyFormat.format(value);
            String pctFormatted = String.format("%.2f", pct);
            data.setName(
                name
                + "\n(" + formattedValue + ")"
                + "\n" + pctFormatted + "%"
            );
        }
        return pieData;
    }

    /**
     * Gets trend series with regression for a specific category.
     * @param startYear trend Series starting year
     * @param endYear   trend Series ending year
     * @param isRevenue true for revenue, false for expense
     * @return map containing data series and regression series
     */
    public Map<String, Series<Number, Number>> getTrendWithRegression(
        int startYear, int endYear, boolean isRevenue
    ) {
        Map<String, Series<Number, Number>> seriesMap =
            budgetService.getRevenueExpenseTrendSeries(startYear, endYear);

        String key = isRevenue ? "Revenue" : "Expense";
        Series<Number, Number> dataSeries = seriesMap.get(key);
        Series<Number, Number> regressionSeries =
            budgetService.createRegressionSeries(dataSeries);

        return Map.of(
            DATA_KEY, dataSeries,
            REGRESSION_KEY, regressionSeries
        );
    }

    /**
     * Gets top items for a combo box.
     *
     * @param year selected year
     * @param topN number of top items
     * @param isRevenue revenue or expense
     * @return list of item names with "All" as first option
     */
    public ObservableList<String> getTopItemsForComboBox(
        int year, int topN, boolean isRevenue
    ) {
        try {
            Map<String, Series<Number, Number>> map =
                budgetService.getTopItemsTrendSeries(
                    year,
                    DEFAULT_START_YEAR,
                    DEFAULT_END_YEAR,
                    topN,
                    isRevenue
                );
            ObservableList<String> items =
                            FXCollections.observableArrayList("All");
            items.addAll(map.keySet());
            return items;
        } catch (IllegalArgumentException e) {
            return FXCollections.observableArrayList("All");
        }
    }

    /**
     * Gets single item trend with regression.
     * @param referenceYear the year which we are perfoming the analysis
     * @param itemName  the item name
     * @param isRevenue true if item is revenue false if it is expense
     * @return map containing items trend series and it's regression series
     * @throws IllegalArgumentException if item Series(selectedSeries is null)
     */
    public Map<String, Series<Number, Number>>
                            getSingleItemTrendWithRegression(
        int referenceYear, String itemName, boolean isRevenue
    ) throws  IllegalArgumentException {
        Map<String, Series<Number, Number>> seriesMap =
            budgetService.getTopItemsTrendSeries(
                referenceYear,
                DEFAULT_START_YEAR,
                DEFAULT_END_YEAR,
                DEFAULT_TOP_N,
                isRevenue
            );

        Series<Number, Number> selectedSeries = seriesMap.get(itemName);
        if (selectedSeries == null) {
            throw new IllegalArgumentException("Item not found: " + itemName);
        }

        Series<Number, Number> regressionSeries =
            budgetService.createRegressionSeries(selectedSeries);

        return Map.of(
            DATA_KEY, selectedSeries,
            REGRESSION_KEY, regressionSeries
        );
    }

    /**
     * Gets loans series with regression.
     * @param isRevenue true if you are referring to loan Revenue item
     * @return a Map containig loan trend Series and it's regression Series
     */
    public Map<String, Series<Number, Number>> getLoansTrendWithRegression(
        boolean isRevenue
    ) {
        Series<Number, Number> dataSeries =
            budgetService.getLoansTrendSeries(
                DEFAULT_START_YEAR,
                DEFAULT_END_YEAR,
                isRevenue
            );

        Series<Number, Number> regressionSeries =
            budgetService.createRegressionSeries(dataSeries);

        return Map.of(
            DATA_KEY, dataSeries,
            REGRESSION_KEY, regressionSeries
        );
    }

     /**
     * Gets net result series with regression.
     * @return a Map containg net result trend Series and it's regression Series
     */
    public Map<String, Series<Number, Number>> getNetResultWithRegression() {
        Series<Number, Number> netSeries =
            budgetService.getNetResultSeries(
                DEFAULT_START_YEAR,
                DEFAULT_END_YEAR
            );

        Series<Number, Number> regressionSeries =
            budgetService.createRegressionSeries(netSeries);

            return Map.of(
                DATA_KEY, netSeries,
                REGRESSION_KEY, regressionSeries
            );
    }

    /**
     * Gets the underlying BudgetService.
     * @return underlying BudgetService
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP")
    public BudgetService getBudgetService() {
        return  budgetService;
    }
}
