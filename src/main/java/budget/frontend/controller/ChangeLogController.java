package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import budget.backend.model.domain.ChangeLog;
import budget.backend.repository.ChangeLogRepository;
import budget.backend.service.ChangeLogService;
import budget.frontend.util.DateUtils;
import budget.frontend.util.TableUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
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
        
        dateColumn.setCellValueFactory(cellData -> 
            DateUtils.formatIsoDate(cellData.getValue().submittedDate())
        );

        actorColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().actorName()));

        itemIdColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(
                cellData.getValue().budgetItemId()).asObject()
            );

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

        TableUtils.setupCurrencyColumn(
            oldValueColumn, 
            ChangeLog::oldValue, // Method Reference (επειδή είναι Record: .oldValue())
            currencyFormat
        );

        TableUtils.setupCurrencyColumn(
            newValueColumn, 
            ChangeLog::newValue, 
            currencyFormat
        );

        TableUtils.setupStyledCurrencyColumn(
            valueDifferenceColumn,
            log -> log.newValue() - log.oldValue(),
            currencyFormat
        );
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
