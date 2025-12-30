package budget.frontend.util;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Helper class for managing chart visibility and titles.
 * Reduces boilerplate code in controllers.
 */
public final class ChartConfigurationHelper {

    /**
     * Class constructor.
     */
    private ChartConfigurationHelper() {
        // prevents initialization
    }

    /**
     * Sets visibility for multiple nodes.
     * @param visible true if we are making the nodes visible false otherwise
     * @param nodes nodes we are editing
     */
    public static void setVisibility(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    /**
     * Configuration for "Top Items" view.
     */
    public static class TopItemsViewConfig {

        // CHECKSTYLE:OFF ParameterNumber
        /**
         * Applies visibility settings for top items view components.
         * @param categoryComboBox the category combo box
         * @param revenueComboBox the revenue combo box
         * @param expenseComboBox the expense combo box
         * @param categoryLabel the category label
         * @param revenueLabel the revenue label
         * @param expenseLabel the expense label
         * @param trendLineChart1 the first trend line chart
         * @param trendLineChart2 the second trend line chart
         */
        public static void applyVisibility(
            ComboBox<String> categoryComboBox,
            ComboBox<String> revenueComboBox,
            ComboBox<String> expenseComboBox,
            Label categoryLabel,
            Label revenueLabel,
            Label expenseLabel,
            Node trendLineChart1,
            Node trendLineChart2
        ) {
            setVisibility(true,
                categoryComboBox, categoryLabel,
                revenueComboBox, revenueLabel,
                expenseComboBox, expenseLabel,
                trendLineChart1, trendLineChart2
            );
        }
        // CHECKSTYLE:ON ParameterNumber

        /**
         * Gets chart titles for top items view.
         * @param selectedCategory the selected category name
         * @return ChartTitles object with configured titles
         */
        public static ChartTitles getTitles(String selectedCategory) {
            return new ChartTitles(
                String.format("Top 5 %s Distribution ", selectedCategory),
                "Top 5 Expenses Trend ",
                "Top 5 Revenues Trend ",
                "Top 5 Expenses & Revenues  "
            );
        }
    }

    /**
     * Configuration for "Budget Results" view.
     */
    public static class BudgetResultsViewConfig {
        // CHECKSTYLE:OFF ParameterNumber
        /**
         * Applies visibility settings for budget results view components.
         * @param categoryComboBox the category combo box
         * @param revenueComboBox the revenue combo box
         * @param expenseComboBox the expense combo box
         * @param categoryLabel the category label
         * @param revenueLabel the revenue label
         * @param expenseLabel the expense label
         * @param trendLineChart1 the first trend line chart
         * @param trendLineChart2 the second trend line chart
         */
        public static void applyVisibility(
            ComboBox<String> categoryComboBox,
            ComboBox<String> revenueComboBox,
            ComboBox<String> expenseComboBox,
            Label categoryLabel,
            Label revenueLabel,
            Label expenseLabel,
            Node trendLineChart1,
            Node trendLineChart2
        ) {
            setVisibility(true, revenueComboBox);
            setVisibility(false,
                categoryComboBox, categoryLabel,
                expenseComboBox, expenseLabel,
                revenueLabel,
                trendLineChart1, trendLineChart2
            );
        }
        // CHECKSTYLE:ON ParameterNumber

        /**
         * Gets chart titles for budget results view.
         * @return ChartTitles object with configured titles
         */
        public static ChartTitles getTitles() {
            return new ChartTitles(
                "Revenue vs Expense Distribution",
                "Revenue & Expense Trend",
                "Net Result Trend",
                 "Year Comparison"
                );
        }
    }
}
