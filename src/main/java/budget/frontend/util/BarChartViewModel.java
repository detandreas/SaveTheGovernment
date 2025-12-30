package budget.frontend.util;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart.Series;

/**
 * View model for BarChart.
 * Handles chart setup and data loading.
 */
public class BarChartViewModel {

    private final BarChart<String, Number> chart;

    /**
     * Class constructor.
     * @param chart the chart being edited
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2"
    )
    public BarChartViewModel(BarChart<String, Number> chart) {
        this.chart = chart;
    }

    /**
     * Loads multiple series into bar chart.
     * @param seriesMap a Map where key is the year name (e.g "2024") and value
     *                          is a Series containing Revenue , Expense data
     */
    public void loadSeries(Map<String, Series<String, Number>> seriesMap) {
        chart.getData().clear();

        for (var series : seriesMap.values()) {
            if (series != null && !series.getData().isEmpty()) {
                chart.getData().add(series);
            }
        }
    }

    /**
     * Loads a single series.
     * @param series the series which will be loaded
     */
    public void loadSingleSeries(Series<String, Number> series) {
        chart.getData().clear();
        if (series != null && !series.getData().isEmpty()) {
            chart.getData().add(series);
        }
    }

    /**
     * Loads two separate series into the chart.
     * This is useful for loading revenue and expense series separately.
     * @param series1 first series
     * @param series2 second series
     */
    public void loadTwoSeries(Series<String, Number> series1,
                              Series<String, Number> series2) {
        chart.getData().clear();

        if (series1 != null && !series1.getData().isEmpty()) {
            chart.getData().add(series1);
        }

        if (series2 != null && !series2.getData().isEmpty()) {
            chart.getData().add(series2);
        }
    }

    /**
     * Sets the chart title.
     * @param title chart title
     */
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    /**
     * Clears the chart and forces category axis refresh.
     * This ensures that when switching between different chart types,
     * the category axis properly updates its categories.
     */
    public void clear() {
        chart.getData().clear();
        // Force the CategoryAxis to recalculate categories
        if (chart.getXAxis() instanceof CategoryAxis categoryAxis) {
            categoryAxis.getCategories().clear();
            categoryAxis.invalidateRange(categoryAxis.getCategories());
        }
    }

}
