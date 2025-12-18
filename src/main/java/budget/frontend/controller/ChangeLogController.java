package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Locale;
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

    private final ChangeLogService changeLogService = 
                                    new ChangeLogService(new ChangeLogRepository());

    /**
     * Initializes the controller by setting up table columns and loading data.
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
            return new SimpleStringProperty(originalDate.replace("T", " "));
        });

        actorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().actorName()));

        itemIdColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().budgetItemId()).asObject());

        oldValueColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().oldValue()).asObject());

        newValueColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().newValue()).asObject());

        // Currency Formatter (όπως στο TotalBudgetController)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

        // Custom Cell Factory για τις στήλες τιμών
        var currencyCellFactory = createCurrencyCellFactory(currencyFormat);
        oldValueColumn.setCellFactory(currencyCellFactory);
        newValueColumn.setCellFactory(currencyCellFactory);
    }

    /**
     * Helper method to create a formatted currency cell.
     */
    private Callback<TableColumn<ChangeLog, Double>, TableCell<ChangeLog, Double>> 
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
     * Loads change log history into the table view.
     */
    private void loadData() {
        // ObservableList from service layer
        changeLogTable.setItems(changeLogService.getAllChangeLogsSortedByDate());
    }
}
