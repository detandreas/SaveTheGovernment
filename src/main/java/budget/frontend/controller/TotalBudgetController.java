package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.collections.ListChangeListener;

import budget.backend.model.domain.BudgetItem;
import budget.backend.model.domain.Budget;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.BudgetService;
import javafx.beans.property.SimpleStringProperty;
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

                getStyleClass().removeAll("status-revenue", "status-expense");
                setText(null);

                if (!empty && item != null) {
                    setText(item);

                    if (item.equals("Revenue")) {
                        getStyleClass().add("status-revenue");
                    } else {
                        getStyleClass().add("status-expense");
                    }
                }
            }
        });
    }
    /**
     * Loads budget data into the table and updates summary labels.
     */
    private void loadData() {
        budgetTable
                .setItems(budgetService.getBudgetItemsForTable(CURRENT_YEAR));

        List<BudgetItem> items = budgetService.getBudgetItemsForTable(CURRENT_YEAR);

        Budget currentBudget = new Budget(items, CURRENT_YEAR);
        budgetService.recalculateBudgetTotals(currentBudget);

        updateLabels(currentBudget);

        budgetTable.getItems().addListener((ListChangeListener<BudgetItem>) change -> {
            List<BudgetItem> updatedItems = budgetTable.getItems();
            Budget updatedBudget = new Budget(updatedItems, CURRENT_YEAR);
            budgetService.recalculateBudgetTotals(updatedBudget);
            updateLabels(updatedBudget);
        });
    }

    private void updateLabels(Budget budget) {
        System.out.println("Total Revenue: " + budget.getTotalRevenue());
        System.out.println("Total Expense: " + budget.getTotalExpense());
        System.out.println("Net Result: " + budget.getNetResult());

        totalRevenueLabel.setText(currencyFormat.format(budget.getTotalRevenue()));
        totalExpensesLabel.setText(currencyFormat.format(budget.getTotalExpense()));
        totalBudgetLabel.setText(currencyFormat.format(budget.getNetResult()));
    }
}
