package budget.frontend.controller;

import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.ChangeRequestService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import budget.backend.repository.ChangeRequestRepository;
import budget.backend.service.BudgetService;
import budget.backend.repository.UserRepository;
import budget.backend.service.BudgetValidationService;
import budget.backend.service.ChangeLogService;
import budget.backend.repository.ChangeLogRepository;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Controller class for managing the pending changes view.
 * It follows the same architecture as ChangeLogController for consistency.
 */
public class PendingChangesController {

    private static final Logger LOGGER =
        Logger.getLogger(PendingChangesController.class.getName());

    @FXML private TableView<PendingChange> pendingChangesTable;
    @FXML private TableColumn<PendingChange, String> dateColumn;
    @FXML private TableColumn<PendingChange, String> actorColumn;
    @FXML private TableColumn<PendingChange, Integer> itemIdColumn;
    @FXML private TableColumn<PendingChange, Double> oldValueColumn;
    @FXML private TableColumn<PendingChange, Double> newValueColumn;
    @FXML private TableColumn<PendingChange, Double> valueDifferenceColumn;
    @FXML private TableColumn<PendingChange, Void> actionColumn;

    private ChangeRequestService changeRequestService;
    private PrimeMinister currentUser;
    private ObservableList<PendingChange> allItems;
    private FilteredList<PendingChange> filteredItems;
    private SortedList<PendingChange> sortedItems;

    /**
     * Initializes the controller by setting up
     * table columns and loading data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        initServices();
        LOGGER.log(
            Level.INFO,
            "Controller UI initialized. Waiting for User Data..."
        );
    }
    /**
     * Sets the Prime Minister user for
     * this controller and loads the data.
     *
     * @param pm the Prime Minister user
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
            "Controller needs a reference to the"
            + "external mutable User object by design."
    )
    public void setPrimeMinister(PrimeMinister pm) {
        this.currentUser = pm;
        LOGGER.log(Level.INFO, "PrimeMinister set: " + pm.getFullName());
        loadData();
    }
    private void initServices() {
        try {
            ChangeRequestRepository reqRepo = new ChangeRequestRepository();
            BudgetRepository budgetRepo = new BudgetRepository();
            UserRepository userRepo = new UserRepository();
            ChangeLogRepository logRepo = new ChangeLogRepository();

            BudgetValidationService valService =
                new BudgetValidationService(budgetRepo);
            BudgetService budgetService = new BudgetService(budgetRepo);
            ChangeLogService logService = new ChangeLogService(logRepo);

            this.changeRequestService = new ChangeRequestService(
                reqRepo,
                budgetRepo,
                userRepo,
                valService,
                budgetService,
                logService
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing services", e);
        }
    }

     private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            String originalDate = cellData.getValue().getSubmittedDate();
            return new SimpleStringProperty(
                originalDate.replace("T", " ")
            );
        });

        actorColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getRequestByName()));

        itemIdColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getBudgetItemId()));

        oldValueColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getOldValue()));

        newValueColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getNewValue()));

        valueDifferenceColumn.setCellValueFactory(cell -> {
            double diff =
            cell.getValue().getNewValue() - cell.getValue().getOldValue();
            return new SimpleObjectProperty<>(diff);
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

        setupActionColumnButtons();
    }

    private void setupActionColumnButtons() {
        Callback<TableColumn<PendingChange, Void>,
        TableCell<PendingChange, Void>> cellFactory =
            param -> new TableCell<>() {
            private final Button btnAccept = new Button("Accept");
            private final Button btnReject = new Button("Reject");
            private final HBox container = new HBox(10, btnAccept, btnReject);

        {
            container.getStyleClass().add("action-box");
            btnAccept.getStyleClass().addAll("action-button", "btn-accept");
            btnReject.getStyleClass().addAll("action-button", "btn-reject");
            btnAccept.setOnAction(event -> {
                PendingChange item = getTableView().getItems().get(getIndex());
                handleApprove(item);
            });
            btnReject.setOnAction(event -> {
                PendingChange item = getTableView().getItems().get(getIndex());
                handleReject(item);
            });
        }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }
    /**
     * Helper method to create a formatted currency cell
     * with conditional styling.
     * @param format the NumberFormat instance for currency formatting
     * @return a Callback for TableCell creation
     */
    private Callback<TableColumn<PendingChange, Double>,
        TableCell<PendingChange, Double>>
        createStyledCurrencyCellFactory(NumberFormat format) {
        return column -> new TableCell<PendingChange, Double>() {
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
     * Helper method to create a formatted currency cell.
     * @param format the NumberFormat instance for currency formatting
     * @return a Callback for TableCell creation
     */
    private Callback<TableColumn<PendingChange, Double>,
        TableCell<PendingChange, Double>>
        createCurrencyCellFactory(NumberFormat format) {
        return column -> new TableCell<PendingChange, Double>() {
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
     * Loads data into the table from the service.
     */
    private void loadData() {
        if (changeRequestService == null) {
            return;
        }

        try {
            allItems = changeRequestService.getAllPendingChangesSortedByDate();

            if (allItems == null) {
                LOGGER.log(Level.WARNING, "No data found.");
                return;
            }

            filteredItems = new FilteredList<>(allItems, p -> true);
            sortedItems = new SortedList<>(filteredItems);
            sortedItems.comparatorProperty().bind(
                pendingChangesTable.comparatorProperty()
            );
            pendingChangesTable.setItems(sortedItems);
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE, "Failed to load table data.", e
            );
        }
    }
    /**
     * Handles the approval of a pending change.
     * @param change the PendingChange to approve
     */
    private void handleApprove(PendingChange change) {
        LOGGER.log(
            Level.INFO,
            "Attempting to approve request ID: {0}", change.getId()
        );
        try {
            changeRequestService.approveRequest(currentUser, change);
            allItems.remove(change);
            LOGGER.log(Level.INFO, "Request approved.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Approve failed", e);
        }
    }
    /**
     * Handles the rejection of a pending change.
     * @param change the PendingChange to reject
     */
    private void handleReject(PendingChange change) {
        LOGGER.log(
            Level.INFO, "Attempting to reject request ID: {0}",
            change.getId()
        );
        try {
            changeRequestService.rejectRequest(currentUser, change);
            allItems.remove(change);
            LOGGER.log(Level.INFO, "Request rejected.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Reject failed", e);
        }
    }
}
