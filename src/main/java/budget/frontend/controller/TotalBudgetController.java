package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.BudgetService;
import budget.backend.service.BudgetValidationService;
import budget.frontend.constants.Constants;
import budget.frontend.util.UserSession;
import budget.frontend.util.WindowUtils;
import budget.frontend.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    @FXML private Label budgetLabel;
    @FXML private TableView<BudgetItem> budgetTable;
    @FXML private TableColumn<BudgetItem, String> nameColumn;
    @FXML private TableColumn<BudgetItem, String> typeColumn;
    @FXML private TableColumn<BudgetItem, Double> valueColumn;
    @FXML private TableColumn<BudgetItem, Void> actionColumn;
    @FXML private ComboBox<Integer> budgetYearComboBox;

    private final BudgetRepository budgetRepository = new BudgetRepository();
    private final BudgetService budgetService =
                                new BudgetService(budgetRepository);
    private final BudgetValidationService validationService =
                                new BudgetValidationService(budgetRepository);
    private static final int CURRENT_YEAR = 2026;
    private static final int DEFAULT_START_YEAR = 2019;
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
        setUpComboBox();
        if (isFinanceMinister()) {
            setupActionColumn();
            actionColumn.setVisible(true);
        } else {
            actionColumn.setVisible(false);
        }
        loadData();
    }

    private boolean isFinanceMinister() {

        User currentUser = UserSession.getInstance().getUser();
        if (currentUser == null) {
            return false;
        }
        if (currentUser instanceof GovernmentMember) {
            GovernmentMember minister = (GovernmentMember) currentUser;
            return minister.getMinistry() == Ministry.FINANCE;
        }

        return false;
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
    * Sets up the year combo box with available years and configures
    * the action handler to reload data when the year changes.
    */
    private void setUpComboBox() {
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int year = CURRENT_YEAR; year >= DEFAULT_START_YEAR; year--) {
            years.add(year);
        }

        budgetYearComboBox.setItems(years);
        budgetYearComboBox.setValue(CURRENT_YEAR);
        budgetYearComboBox.setOnAction(e -> loadData());
    }
    /**
     * Configures the action column with edit button.
     * The edit button opens the EditBudgetView for the selected budget item.
     */
    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            {
                editButton.getStyleClass().add("btn-edit");
                editButton.setOnAction(event -> {
                    BudgetItem item = getTableView().getItems().get(getIndex());
                    handleDirectEdit(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
    }
    private void handleDirectEdit(BudgetItem item) {
        EditBudgetController controller = WindowUtils.openModal(
            Constants.EDIT_BUDGET_VIEW,
            "Edit Budget Item",
            budgetTable.getScene().getWindow(),
            (ctrl, stage) -> {
                ctrl.setDialogStage(stage);
                ctrl.setBudgetItem(item);
                ctrl.setValidationService(this.validationService);
            }
        );

        if (controller == null) {
            return;
        }

        if (controller.isSaveClicked()) {
            double newValue = controller.getResultValue();

            budgetService.updateItemValue(
                item.getId(), item.getYear(), newValue
            );

            item.setValue(newValue);
            budgetTable.refresh();

            LOGGER.log(Level.INFO, "Updated item {0} to new value: {1}",
                       new Object[]{item.getName(), newValue});

            loadData();

            AlertUtils.showSuccess(
                "Success",
                "Item updated successfully!"
            );
        }
    }
    /**
     * Loads budget data into the table and updates summary labels.
     */
    private void loadData() {
        Integer selectedYear = budgetYearComboBox.getValue();
        if (selectedYear == null) {
            selectedYear = CURRENT_YEAR;
        }
        try {
            Optional<Budget> budgetOpt =
                                budgetRepository.findById(selectedYear);
            if (budgetOpt.isEmpty()) {
                throw new IllegalArgumentException(
                String.format("No data available for year %d", selectedYear));
            }
            Budget budget = budgetOpt.get();
            budgetLabel.setText(String.format("Budget %d", selectedYear));
            allItems = budgetService.getBudgetItemsForTable(selectedYear);
            setupFilters(selectedYear);

            budgetService.recalculateBudgetTotals(budget);
            updateLabels(budget);
        } catch (IllegalArgumentException e) {
            clearTable();
        }
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

    /**
     * Sets up the filtered and sorted lists for the budget table.
     *
     * @param year the year to load budget items for
     */
    private void setupFilters(int year) {
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

    /**
     * Clears the budget table and resets all labels when data is not available.
     */
    private void clearTable() {
        budgetTable.getItems().clear();
        totalBudgetLabel.setText(currencyFormat.format(0.0));
        totalExpensesLabel.setText(currencyFormat.format(0.0));
        totalRevenueLabel.setText(currencyFormat.format(0.0));
    }
}
