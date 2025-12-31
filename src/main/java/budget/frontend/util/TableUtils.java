package budget.frontend.util;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.text.NumberFormat;
import java.util.function.Function;

/**
 * Utility class for configuring JavaFX TableView columns.
 *
 * This class provides static methods to reduce boilerplate code when setting up
 * table columns, specifically for currency formatting and conditional styling.
 */
public final class TableUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TableUtils() {
    }

    /**
     * Configures a TableColumn to display currency values.
     *
     * This method sets both the cellValueFactory (to retrieve the data)
     * and the cellFactory (to format the data).
     *
     * @param column The TableColumn to configure.
     * @param mapper A function that extracts the Double value from the row item (e.g., Item::getValue).
     * @param format The NumberFormat to use for display (e.g., Locale.GERMANY).
     * @param <S>    The type of the TableView items.
     */
    public static <S> void setupCurrencyColumn(
            TableColumn<S, Double> column,
            Function<S, Double> mapper,
            NumberFormat format) {

        // 1. Set the CellValueFactory using the provided mapper
        column.setCellValueFactory(cellData -> {
            Double value = mapper.apply(cellData.getValue());
            return new SimpleDoubleProperty(value != null ? value : 0.0).asObject();
        });

        // 2. Set the CellFactory for formatting
        column.setCellFactory(createCurrencyCellFactory(format));
    }

    /**
     * Configures a TableColumn to display currency values with conditional styling.
     *
     * Positive values are styled with the CSS class "status-green".
     * Negative values are styled with the CSS class "status-red".
     * The displayed value is absolute (no negative sign), relying on color to indicate the sign.
     *
     * @param column The TableColumn to configure.
     * @param mapper A function that extracts the Double value from the row item.
     * @param format The NumberFormat to use for display.
     * @param <S>    The type of the TableView items.
     */
    public static <S> void setupStyledCurrencyColumn(
            TableColumn<S, Double> column,
            Function<S, Double> mapper,
            NumberFormat format) {

        // 1. Set the CellValueFactory
        column.setCellValueFactory(cellData -> {
            Double value = mapper.apply(cellData.getValue());
            return new SimpleDoubleProperty(value != null ? value : 0.0).asObject();
        });

        // 2. Set the CellFactory for formatting and styling
        column.setCellFactory(createStyledCurrencyCellFactory(format));
    }

    // --- Private Helper Methods ---

    /**
     * Creates a cell factory that formats Double values as currency.
     */
    private static <S> Callback<TableColumn<S, Double>, TableCell<S, Double>>
            createCurrencyCellFactory(NumberFormat format) {
        
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        };
    }
    /**
     * Creates a cell factory that formats Double values and applies CSS classes based on the value.
     */
    private static <S> Callback<TableColumn<S, Double>, TableCell<S, Double>>
            createStyledCurrencyCellFactory(NumberFormat format) {
        
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("status-green", "status-red");

                if (empty || item == null) {
                    setText(null);
                } else {
                    double absValue = Math.abs(item);
                    setText(format.format(absValue));

                    if (item > 0) {
                        getStyleClass().add("status-green");
                    } else if (item < 0) {
                        getStyleClass().add("status-red");
                    }
                }
            }
        };
    }
}
