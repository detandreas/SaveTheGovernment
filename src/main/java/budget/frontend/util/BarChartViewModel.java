package budget.frontend.util;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.chart.BarChart;
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
    public void loadSingleSereis(Series<String, Number> series) {
        chart.getData().clear();
        if (series != null && !series.getData().isEmpty()) {
            chart.getData().add(series);
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
     * Clears the chart.
     */
    public void clear() {
        chart.getData().clear();
    }

}
