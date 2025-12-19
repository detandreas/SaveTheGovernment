package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.util.Callback;

import budget.backend.model.domain.ChangeLog;
import budget.backend.repository.ChangeLogRepository;
import budget.backend.service.ChangeLogService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller class for managing the change log history view.
 * It follows the same architecture as TotalBudgetController for consistency.
 */
public class ChangeLogController {

    @FXML private TableView<ChangeLog> changeLogTable;
    @FXML private TableColumn<ChangeLog, String> dateColumn;
    @FXML private TableColumn<ChangeLog, String> actorColumn;
    @FXML private TableColumn<ChangeLog, Integer> itemIdColumn;
    @FXML private TableColumn<ChangeLog, Double> oldValueColumn;
    @FXML private TableColumn<ChangeLog, Double> newValueColumn;
    @FXML private TableColumn<ChangeLog, Double> valueDifferenceColumn;

    private final ChangeLogService changeLogService =
        new ChangeLogService(new ChangeLogRepository());

    private ObservableList<ChangeLog> allItems;
    private FilteredList<ChangeLog> filteredItems;
    private SortedList<ChangeLog> sortedItems;

    /**
     * Initializes the controller by setting up
     * table columns and loading data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    /**
     * Configures the table columns with cell value factories and
     * custom cell factories for currency formatting.
     */
    private void setupTableColumns() {
        // Because change log is record, we use lamda expressions
        dateColumn.setCellValueFactory(cellData -> {
            String originalDate = cellData.getValue().submittedDate();
            return new SimpleStringProperty(
                originalDate.replace("T", " ")
            );
        });

        actorColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().actorName()));

        itemIdColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(
                cellData.getValue().budgetItemId()).asObject()
            );

        oldValueColumn.setCellValueFactory(cellData ->
            new SimpleDoubleProperty(
                cellData.getValue().oldValue()).asObject()
            );

        newValueColumn.setCellValueFactory(cellData ->
            new SimpleDoubleProperty(
                cellData.getValue().newValue()).asObject()
            );

        valueDifferenceColumn.setCellValueFactory(cellData -> {
            double difference =
                cellData.getValue().newValue() - cellData.getValue().oldValue();
            return new SimpleDoubleProperty(difference).asObject();
        });

        NumberFormat currencyFormat = NumberFormat
            .getCurrencyInstance(Locale.GERMANY);

        // Custom Cell Factory για τις στήλες τιμών
        var currencyCellFactory = createCurrencyCellFactory(currencyFormat);
        oldValueColumn.setCellFactory(currencyCellFactory);
        newValueColumn.setCellFactory(currencyCellFactory);
        valueDifferenceColumn.setCellFactory(
            createStyledCurrencyCellFactory(currencyFormat)
        );
    }

    /**
     * Helper method to create a formatted currency cell.
     * @param format the NumberFormat instance for currency formatting
     * @return a Callback for TableCell creation
     */
    private Callback<TableColumn<ChangeLog, Double>,
        TableCell<ChangeLog, Double>>
        createCurrencyCellFactory(NumberFormat format) {
        return column -> new TableCell<ChangeLog, Double>() {
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
     * Helper method to create a formatted currency cell
     * with conditional styling.
     * @param format the NumberFormat instance for currency formatting
     * @return a Callback for TableCell creation
     */
    private Callback<TableColumn<ChangeLog, Double>,
        TableCell<ChangeLog, Double>>
        createStyledCurrencyCellFactory(NumberFormat format) {
        return column -> new TableCell<ChangeLog, Double>() {
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

    /**
     * Loads change log history into the table view.
     */
    private void loadData() {
        setupFilters();
    }

    private void setupFilters() {
        allItems = changeLogService.getAllChangeLogsSortedByDate();
        filteredItems = new FilteredList<>(allItems, p -> true);
        sortedItems = new SortedList<>(filteredItems);
        changeLogTable.setItems(sortedItems);
    }

    @FXML
    private void  handleSortAmountAsc() {
        sortedItems.setComparator(
            Comparator.comparingDouble(changeLog ->
                Math.abs(changeLog.newValue() - changeLog.oldValue())
            )
        );
    }

    @FXML
    private void handleSortAmountDesc() {
        sortedItems.setComparator(
            Comparator.comparingDouble((ChangeLog c) ->
                Math.abs(c.newValue() - c.oldValue())
            ).reversed()
        );
    }

    @FXML
    private void handleFilterIncreasesOnly() {
        filteredItems.setPredicate(
            changeLog -> (changeLog.newValue() - changeLog.oldValue()) > 0
        );
    }

    @FXML
    private void handleFilterDecreasesOnly() {
        filteredItems.setPredicate(
            changeLog -> (changeLog.newValue() - changeLog.oldValue()) < 0
        );
    }

    @FXML
    private void handleSortByNameAsc() {
        sortedItems.setComparator(
            Comparator.comparing(ChangeLog::actorName)
        );
    }

    @FXML
    private void handleSortByNameDesc() {
        sortedItems.setComparator(
            Comparator.comparing(ChangeLog::actorName).reversed()
        );
    }

    @FXML
    private void handleClearFilters() {
        filteredItems.setPredicate(p -> true);
        sortedItems.setComparator(null);
    }
}
