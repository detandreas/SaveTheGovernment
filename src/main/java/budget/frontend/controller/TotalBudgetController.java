package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.BudgetService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
/**
 * Controller class for managing the total budget view
 *                                              in the JavaFX application.
 * It initializes the table view and its columns, formats currency values,
 * and applies conditional styling based on budget item types.
 */
public class TotalBudgetController {

    @FXML private Label totalBudgetLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label totalRevenueLabel;

    @FXML private TableView<BudgetItem> budgetTable;
    @FXML private TableColumn<BudgetItem, String> nameColumn;
    @FXML private TableColumn<BudgetItem, String> typeColumn;
    @FXML private TableColumn<BudgetItem, Double> valueColumn;

    private BudgetService budgetService =
                                new BudgetService(new BudgetRepository());
    private static final int CURRENT_YEAR = 2025;
    private final NumberFormat currencyFormat =
                            NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private static final Logger LOGGER =
                    Logger.getLogger(TotalBudgetController.class.getName());

    private ObservableList<BudgetItem> allItems;
    private FilteredList<BudgetItem> filteredItems;
    private SortedList<BudgetItem> sortedItems;
    /**
     * Initializes the controller by setting up table columns and loading data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }
    /**
     * Configures the table columns, including cell value factories
     * and custom cell factories
     * for formatting and styling.
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        typeColumn.setCellValueFactory(cellData -> {
            boolean isRev = cellData.getValue().getIsRevenue();
            return new SimpleStringProperty(isRev ? "Revenue" : "Expense");
        });

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        valueColumn.setCellFactory(column ->
            new TableCell<BudgetItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        typeColumn.setCellFactory(column ->
            new TableCell<BudgetItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("status-green", "status-red");
                setText(null);

                if (!empty && item != null) {
                    setText(item);

                    if (item.equals("Revenue")) {
                        getStyleClass().add("status-green");
                    } else {
                        getStyleClass().add("status-red");
                    }
                }
            }
        });
    }
    /**
     * Loads budget data into the table and updates summary labels.
     */
    private void loadData() {
        setupFilters();

        List<BudgetItem> items = allItems;
        Budget currentBudget = new Budget(items, CURRENT_YEAR);

        budgetService.recalculateBudgetTotals(currentBudget);
        updateLabels(currentBudget);
        budgetTable.getItems()
                   .addListener((ListChangeListener<BudgetItem>) change -> {

            List<BudgetItem> updatedItems = budgetTable.getItems();

            Budget updatedBudget = new Budget(updatedItems, CURRENT_YEAR);

            budgetService.recalculateBudgetTotals(updatedBudget);

            updateLabels(updatedBudget);
        });
    }
    /**
     * Updates the summary labels with the latest budget totals.
     *
     * @param budget The budget object containing the totals.
     */
    private void updateLabels(Budget budget) {
        LOGGER.log(
            Level.INFO,
            "Total Revenue: {0}",
            budget.getTotalRevenue()
        );
        LOGGER.log(
            Level.INFO,
            "Total Expense: {0}",
            budget.getTotalExpense()
        );
        LOGGER.log(
            Level.INFO,
            "Net Result: {0}",
            budget.getNetResult()
        );

        totalRevenueLabel.setText(
            currencyFormat.format(budget.getTotalRevenue())
        );
        totalExpensesLabel.setText(
            currencyFormat.format(budget.getTotalExpense())
        );
        totalBudgetLabel.setText(currencyFormat.format(budget.getNetResult()));
    }

    private void setupFilters() {
        allItems = budgetService.getBudgetItemsForTable(CURRENT_YEAR);
        filteredItems = new FilteredList<>(allItems, p -> true);
        sortedItems = new SortedList<>(filteredItems);
        budgetTable.setItems(sortedItems);
    }

    @FXML
    private void handleSortAmountAsc() {
        sortedItems.setComparator(Comparator.comparing(BudgetItem::getValue));
    }

    @FXML
    private void handleSortAmountDesc() {
        sortedItems.setComparator(
            Comparator.comparing(BudgetItem::getValue).reversed()
        );
    }

    @FXML
    private void handleFilterExpenses() {
        filteredItems.setPredicate(item -> !item.getIsRevenue());
    }

    @FXML
    private void handleFilterRevenue() {
        filteredItems.setPredicate(item -> item.getIsRevenue());
    }

    @FXML
    private void handleClearFilters() {
        filteredItems.setPredicate(p -> true);
        sortedItems.setComparator(null);
    }

}
