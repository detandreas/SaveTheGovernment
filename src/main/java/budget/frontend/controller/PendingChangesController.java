package budget.frontend.controller;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.repository.BudgetRepository;
import budget.backend.repository.ChangeLogRepository;
import budget.backend.repository.ChangeRequestRepository;
import budget.backend.repository.UserRepository;
import budget.backend.service.BudgetService;
import budget.backend.service.BudgetValidationService;
import budget.backend.service.ChangeLogService;
import budget.backend.service.ChangeRequestService;
import budget.frontend.util.AlertUtils;
import budget.frontend.util.DateUtils;
import budget.frontend.util.TableUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    @FXML private TableColumn<PendingChange, String> itemNameColumn;
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
        LOGGER.log(Level.INFO, "PrimeMinister set: {0}", pm.getFullName());
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
        dateColumn.setCellValueFactory(cellData ->
            DateUtils.formatIsoDate(cellData.getValue().getSubmittedDate())
        );

        actorColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getRequestByName()));

        itemNameColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getBudgetItemName()));

        itemIdColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getBudgetItemId()));

        NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.GERMANY);

        TableUtils.setupCurrencyColumn(
            oldValueColumn,
            PendingChange::getOldValue,
            currencyFormat
        );

        TableUtils.setupCurrencyColumn(
            newValueColumn,
            PendingChange::getNewValue,
            currencyFormat
        );

        TableUtils.setupStyledCurrencyColumn(
            valueDifferenceColumn,
            item -> item.getNewValue() - item.getOldValue(),
            currencyFormat
        );

        setupActionColumnButtons();
    }

    private void setupActionColumnButtons() {
        Callback<TableColumn<PendingChange, Void>,
        TableCell<PendingChange, Void>> cellFactory =
            param -> new TableCell<>() {
            private final Button btnAccept = new Button("Approve");
            private final Button btnReject = new Button("Reject");
            private final HBox container = new HBox(10, btnAccept, btnReject);

        {
            container.getStyleClass().add("action-box");
            btnAccept.getStyleClass().addAll("action-button", "btn-approve");
            btnReject.getStyleClass().addAll("action-button", "btn-reject");
            btnAccept.setOnAction(event -> {
                PendingChange item = getTableView().getItems().get(getIndex());

                boolean confirmed = AlertUtils.showConfirmation(
                    "Approve Confirmation",
                    "Approve Pending Change ID: " + item.getId(),
                    "Are you sure you want to approve it?"
                );

                if (confirmed) {
                    handleApprove(item);
                } else {
                    LOGGER.log(Level.INFO, "Accept cancelled by user.");
                }
            });
            btnReject.setOnAction(event -> {
                PendingChange item = getTableView().getItems().get(getIndex());

                boolean confirmed = AlertUtils.showRejectConfirmation(
                    "Reject Confirmation",
                    "Reject Pending Change ID: " + item.getId(),
                    "Are you sure you want to reject it?"
                );

                if (confirmed) {
                    handleReject(item);
                } else {
                    LOGGER.log(Level.INFO, "Reject cancelled by user.");
                }
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
        setupFilters();
    }
    /**
     * Sets up filtering and sorting for the table.
     */
    private void setupFilters() {
        allItems = changeRequestService.getAllPendingChangesSortedByDate();
        filteredItems = new FilteredList<>(allItems, p -> true);
        sortedItems = new SortedList<>(filteredItems);
        pendingChangesTable.setItems(sortedItems);
    }
    /**
     * Handles the approval of a pending change.
     * @param change the PendingChange to approve
     */
    private void handleApprove(PendingChange change) {
        LOGGER.log(
            Level.INFO,
            "Attempting to approve request ID: {0}",
            change.getId()
        );

        try {
            if (change.getBudgetItemYear() == 0) {
                throw new IllegalArgumentException(
                    "Cannot approve request: Year is invalid (0)"
                );
            }
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

    @FXML
    private void handleSortAmountAsc() {
        sortedItems.setComparator(
            Comparator.comparingDouble(
                change -> change.getNewValue() - change.getOldValue()
            )
        );
    }

    @FXML
    private void handleSortAmountDesc() {
        sortedItems.setComparator(
            Comparator.comparingDouble(
                (PendingChange change) ->
                change.getNewValue() - change.getOldValue()
            ).reversed()
        );
    }

    @FXML
    private void handleFilterIncreasesOnly() {
        filteredItems.setPredicate(
            change -> change.getNewValue() > change.getOldValue()
        );
    }

    @FXML
    private void handleFilterDecreasesOnly() {
        filteredItems.setPredicate(
            change -> change.getNewValue() < change.getOldValue()
        );
    }

    @FXML
    private void handleSortByNameAsc() {
        sortedItems.setComparator(
            Comparator.comparing(PendingChange::getRequestByName)
        );
    }

    @FXML
    private void handleSortByNameDesc() {
        sortedItems.setComparator(
            Comparator.comparing(PendingChange::getRequestByName).reversed()
        );
    }

    @FXML
    private void handleClearFilters() {
        filteredItems.setPredicate(p -> true);
        sortedItems.setComparator(null);
    }
}
