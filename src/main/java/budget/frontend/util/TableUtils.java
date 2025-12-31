package budget.frontend.util;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.text.NumberFormat;
import java.util.function.Function;

/**
 * Utility class for configuring JavaFX TableView columns.
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
     * @param mapper A function that extracts the Double value
     *               from the row item (e.g., Item::getValue).
     * @param format The NumberFormat to use for display.
     * @param <S>    The type of the TableView items.
     */
    public static <S> void setupCurrencyColumn(
            TableColumn<S, Double> column,
            Function<S, Double> mapper,
            NumberFormat format) {

        column.setCellValueFactory(cellData -> {
            Double value = mapper.apply(cellData.getValue());
            return new SimpleDoubleProperty(
                value != null ? value : 0.0
            ).asObject();
        });

        column.setCellFactory(createCurrencyCellFactory(format));
    }

    /**
     * Configures a TableColumn to display currency values
     * with conditional styling.
     *
     * The displayed value is absolute,
     * relying on color to indicate the sign.
     *
     * @param column The TableColumn to configure.
     * @param mapper A function that extracts
     *               the Double value from the row item.
     * @param format The NumberFormat to use for display.
     * @param <S>    The type of the TableView items.
     */
    public static <S> void setupStyledCurrencyColumn(
            TableColumn<S, Double> column,
            Function<S, Double> mapper,
            NumberFormat format) {

        column.setCellValueFactory(cellData -> {
            Double value = mapper.apply(cellData.getValue());
            return new SimpleDoubleProperty(
                value != null ? value : 0.0
            ).asObject();
        });

        column.setCellFactory(createStyledCurrencyCellFactory(format));
    }

    // --- Private Helper Methods ---

    /**
     * Creates a cell factory that formats Double values
     * using the specified NumberFormat.
     *
     * @param format the NumberFormat to use for string conversion
     * @param <S>    the type of the TableView items
     * @return a Callback to create the TableCell
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
     * Creates a cell factory that formats Double values
     * and applies CSS classes based on the value.
     *
     * @param format the NumberFormat to use for string conversion
     * @param <S>    the type of the TableView items
     * @return a Callback to create the styled TableCell
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
