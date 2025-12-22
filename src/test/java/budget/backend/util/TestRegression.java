package budget.backend.util;

import javafx.scene.chart.XYChart;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestRegression {

    @Test
    void testLinearRegressionCalculation() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(1, 3));
        series.getData().add(new XYChart.Data<>(2, 5));
        series.getData().add(new XYChart.Data<>(3, 7));

        Regression regression = new Regression(series);

        assertEquals(2.0, regression.getSlope(), 0.0001,
        "Slope calculation is incorrect");
        assertEquals(1.0, regression.getIntercept(), 0.0001,
        "Intercept calculation is incorrect");
        assertEquals(3, regression.getN(),
        "Number of data points (N) is incorrect");
    }

    @Test
    void testConstructorThrowsExceptionForNullSeries() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> {
        new Regression(null);});

        assertEquals("Series cannot be null", ex.getMessage());
    }

    @Test
    void testConstructorThrowsExceptionForInsufficientData() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        assertThrows(IllegalArgumentException.class, () -> {
        new Regression(series);
        }, "Should throw exception for empty series");
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.getData().add(new XYChart.Data<>(1, 3));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
        new Regression(series1);});

        assertEquals("Series must contain at least 2 data points",
        ex2.getMessage());
    }

    @Test
    void testVerticalLineThrowsArtithmeticException() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        series.getData().add(new XYChart.Data<>(5, 1));
        series.getData().add(new XYChart.Data<>(5, 10));
        series.getData().add(new XYChart.Data<>(5, 20));

        assertThrows(ArithmeticException.class, () -> {
        new Regression(series);
        }, "Should throw ArithmeticException when all X values are the same");
    }
}
