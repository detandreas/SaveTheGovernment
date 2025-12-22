package budget.backend.util;

import budget.constants.Limits;
import javafx.scene.chart.XYChart.Series;

/**
 * Calculates linear regression parameters (slope and intercept)
 * from a series of (x, y) data points.
 */
public final class Regression {
    private final Series<Number, Number> series;
    private final double slope;
    private final double intercept;
    private final int n;

    /**
     * Constructs a Regression calculator for the given series.
     *
     * @param series the data series containing (x, y) points
     * @throws IllegalArgumentException if series is null, empty,
     *         or has fewer than 2 data points
     */
    public Regression(Series<Number, Number> series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        if (series.getData() == null || series.getData().size() < 2) {
            throw new IllegalArgumentException(
                "Series must contain at least 2 data points");
        }

        // Create defensive copy to avoid exposing internal representation
        Series<Number, Number> copy = new Series<>();
        copy.getData().addAll(series.getData());
        this.series = copy;
        this.n = series.getData().size();
        this.slope = calcSlope();
        this.intercept = calcIntercept();
    }

    /**
     * Returns the slope of the linear regression line.
     *
     * @return the slope (m) in the equation y = mx + b
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Returns the y-intercept of the linear regression line.
     *
     * @return the intercept (b) in the equation y = mx + b
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * Returns the number of data points used in the regression calculation.
     *
     * @return the number of data points (N)
     */
    public int getN() {
        return n;
    }

    /**
     * Calculates the sum of the product of x and y values (Σxy).
     *
     * @return the sum of x * y for all data points
     */
    private double calcSumXY() {
        return series.getData()
            .stream()
            .mapToDouble(data ->
            data.getXValue().doubleValue() * data.getYValue().doubleValue())
            .sum();
    }

    /**
     * Calculates the sum of all x values (Σx).
     *
     * @return the sum of all x values in the series
     */
    private double calcSumX() {
        return series.getData()
            .stream()
            .mapToDouble(data -> data.getXValue().doubleValue())
            .sum();
    }

    /**
     * Calculates the sum of all y values (Σy).
     *
     * @return the sum of all y values in the series
     */
    private double calcSumY() {
        return series.getData()
            .stream()
            .mapToDouble(data -> data.getYValue().doubleValue())
            .sum();
    }

    /**
     * Calculates the sum of the squares of x values (Σx²).
     *
     * @return the sum of x² for all data points
     */
    private double calcSumXSquared() {
        return series.getData()
            .stream()
            .mapToDouble(data ->
            data.getXValue().doubleValue() * data.getXValue().doubleValue())
            .sum();
    }

    /**
     * Calculates the slope of the linear regression line using the formula:.
     * slope = (N * Σxy - Σx * Σy) / (N * Σx² - (Σx)²)
     *
     * @return the calculated slope
     * @throws ArithmeticException if the denominator is zero
     *         (occurs when all x values are the same)
     */
    private double calcSlope() {
        double sumXY = calcSumXY();
        double sumX = calcSumX();
        double sumY = calcSumY();
        double sumXSquared = calcSumXSquared();

        double denominator = n * sumXSquared - sumX * sumX;

        if (Math.abs(denominator) < Limits.SMALL_NUMBER) {
            throw new ArithmeticException(
                "Cannot calculate slope: denominator is zero. "
                + "All x values may be the same.");
        }

        return (n * sumXY - sumX * sumY) / denominator;
    }

    /**
     * Calculates the y-intercept of the linear regression
     *                                          line using the formula:.
     * intercept = (Σy - slope * Σx) / N
     *
     * @return the calculated y-intercept
     */
    private double calcIntercept() {
        double sumY = calcSumY();
        double sumX = calcSumX();

        return (sumY - slope * sumX) / n;
    }
}
