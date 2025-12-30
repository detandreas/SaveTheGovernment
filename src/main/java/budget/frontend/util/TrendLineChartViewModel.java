package budget.frontend.util;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.util.StringConverter;
import javafx.scene.chart.XYChart.Series;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * View model for LineChart with trend lines.
 * Handles chart setup, data loading, and formatting.
 */
public class TrendLineChartViewModel {

    private final LineChart<Number, Number> chart;

    /**
     * class constructor.
     * @param chart the chart being edited
    */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2"
    )
    public TrendLineChartViewModel(LineChart<Number, Number> chart) {
        this.chart = chart;
        setupYearAxisFormatter();
    }

    /**
     * Sets up the X-axis formatter to display years without decimals.
     */
    private void setupYearAxisFormatter() {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number num) {
                if (num == null) {
                    return  "";
                }
                return String.valueOf(num.intValue());
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
     * Loads series data with regression line.
     *
     * @param seriesMap map containing "data" and "regression" series
     */
    public void loadSeriesWithRegression(
        Map<String, Series<Number, Number>> seriesMap

    ) {
        Series<Number, Number> dataSeries = seriesMap.get("data");
        Series<Number, Number> regressionSeries = seriesMap.get("regression");

        if (dataSeries != null) {
            chart.getData().add(dataSeries);
        }

        if (regressionSeries != null) {
            chart.getData().add(regressionSeries);
        }

    }

    /**
     * Loads multiple series (for top items view).
     * @param seriesMap map containing "data" and "regression" series
     */
    public void loadMultipleSeries(
        Map<String, Series<Number, Number>> seriesMap
    ) {
        chart.getData().clear();
        for (var series : seriesMap.values()) {
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
     * Clears all data from chart.
     */
     public void clear() {
        chart.getData().clear();
    }

    /**
     * Sets visibility.
     * @param visible variable used to handle chart visibility
     */
    public void setVisible(boolean visible) {
        chart.setVisible(visible);
        chart.setManaged(visible);
    }

}
