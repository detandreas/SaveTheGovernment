package budget.frontend.controller;

import java.text.NumberFormat;
import java.time.Year;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import budget.backend.exceptions.UserNotAuthorizedException;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.backend.repository.ChangeLogRepository;
import budget.backend.repository.ChangeRequestRepository;
import budget.backend.repository.UserRepository;
import budget.backend.service.BudgetService;
import budget.backend.service.BudgetValidationService;
import budget.backend.service.ChangeLogService;
import budget.backend.service.ChangeRequestService;
import budget.backend.service.UserAuthorizationService;
import budget.frontend.constants.Constants;
import budget.frontend.util.AlertUtils;
import budget.frontend.util.DateUtils;
import budget.frontend.util.TableUtils;
import budget.frontend.util.WindowUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
/**
 * Controller class for managing the pending changes view.
 * It follows the same architecture as ChangeLogController for consistency.
 */
public class GovMemberPendingChangesController {

    private static final Logger LOGGER =
        Logger.getLogger(GovMemberPendingChangesController.class.getName());

    @FXML private Button createNewRequestButton;
    @FXML private Button myRequestsButton;

    @FXML private TableView<PendingChange> pendingChangesTable;
    @FXML private TableColumn<PendingChange, String> dateColumn;
    @FXML private TableColumn<PendingChange, String> actorColumn;
    @FXML private TableColumn<PendingChange, String> itemNameColumn;
    @FXML private TableColumn<PendingChange, Integer> itemIdColumn;
    @FXML private TableColumn<PendingChange, Double> oldValueColumn;
    @FXML private TableColumn<PendingChange, Double> newValueColumn;
    @FXML private TableColumn<PendingChange, Double> valueDifferenceColumn;

    private ChangeRequestService changeRequestService;
    private UserAuthorizationService userAuthService;
    private ObservableList<PendingChange> allItems;
    private FilteredList<PendingChange> filteredItems;
    private SortedList<PendingChange> sortedItems;
    private BudgetService budgetService;
    private User currentUser;

    /**
     * Sets the current user for the controller.
     *
     * @param user the current user
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
        "Controller needs a reference to the"
        + " external mutable User object by design."
    )
    public final void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Initializes the controller by setting up
     * table columns and loading data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        initServices();
        loadData();
        LOGGER.log(
            Level.INFO,
            "Controller UI initialized. Waiting for User Data..."
        );
    }

    private void initServices() {
        try {
            ChangeRequestRepository reqRepo = new ChangeRequestRepository();
            BudgetRepository budgetRepo = new BudgetRepository();
            UserRepository userRepo = new UserRepository();
            ChangeLogRepository logRepo = new ChangeLogRepository();

            BudgetValidationService valService =
                new BudgetValidationService(budgetRepo);
            this.budgetService = new BudgetService(budgetRepo);
            ChangeLogService logService = new ChangeLogService(logRepo);

            this.userAuthService = new UserAuthorizationService();

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
    /**
     * Sets up the table columns with appropriate
     * cell value factories and formatting.
     */
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

    @FXML
    private void handleCreateNewRequest() {
        LOGGER.log(Level.INFO, "Navigating to Create New Request view.");
        boolean success = openRequestWindow();
        if (success) {
            refreshTable();
            LOGGER.log(Level.INFO,
                "New request created successfully. Table refreshed."
            );
        }
    }
    /**
     * Opens the Create Change Request window
     * and handles the submission process.
     *
     * @return true if a request was successfully submitted, false otherwise
     */
    private boolean openRequestWindow() {
        try {
            int currentYear = Year.now().getValue();
            ObservableList<BudgetItem> allBudgetItems =
                budgetService.getBudgetItemsForTable(currentYear);
            ObservableList<BudgetItem> allowedItems;

            if (currentUser instanceof GovernmentMember gm) {
                Ministry myMinistry = gm.getMinistry();

                allowedItems = allBudgetItems.stream()
                    .filter(item -> item.getMinistries() != null
                            && item.getMinistries().contains(myMinistry))
                    .collect(
                        Collectors.toCollection(
                            FXCollections::observableArrayList
                        )
                    );
            } else {
                LOGGER.log(
                    Level.WARNING,
                    "Current user is not a Government Member."
                    + " No items allowed.");
                allowedItems = FXCollections.observableArrayList();
            }

            CreateChangeRequestController controller = WindowUtils.openModal(
                Constants.CREATE_CHANGE_REQUEST_VIEW,
                "New Budget Change Request",
                pendingChangesTable.getScene().getWindow(),
                (popupController, stage) -> {
                    popupController.setBudgetItems(allowedItems);
                }
            );

            if (controller == null) {
                return false;
            }

            if (controller.isSubmitClicked()) {
                BudgetItem selectedItem = controller.getSelectedBudgetItem();
                Double newValue = controller.getNewValue();

                if (selectedItem != null
                    && newValue != null
                    && currentUser != null
                ) {
                    try {
                        userAuthService.checkCanUserSubmitRequest(
                            currentUser, selectedItem
                        );
                        changeRequestService.submitChangeRequest(
                            currentUser, selectedItem, newValue
                        );

                        LOGGER.log(
                            Level.INFO,
                            "Request submitted successfully."
                        );
                        AlertUtils.showSuccess(
                            "Success",
                            "Request submitted successfully."
                        );
                        return true;

                    } catch (
                        UserNotAuthorizedException | IllegalArgumentException e
                    ) {
                        LOGGER.log(
                            Level.WARNING,
                            "Submission denied: {0}",
                            e.getMessage()
                        );

                        AlertUtils.showError(
                            "Submission Denied",
                            "Authorization Error",
                            "You are not authorized to submit"
                            + " a change request for this budget item."
                        );
                        return false;
                    }
                } else {
                    LOGGER.log(
                        Level.WARNING,
                        "Submission failed: Missing item, value, or user."
                    );
                    AlertUtils.showError(
                        "Invalid Input",
                        null,
                        "Please select an item and enter a valid value."
                    );
                }
            }
            return false;

        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "Unexpected error in openRequestWindow", e
            );
            AlertUtils.showError(
                "System Error",
                null,
                "An unexpected error occurred."
            );
            return false;
        }
    }
    /**
     * Refreshes the table data from the service.
     */
    private void refreshTable() {
        try {
            allItems = changeRequestService.getAllPendingChangesSortedByDate();
            filteredItems = new FilteredList<>(allItems, p -> true);
            sortedItems = new SortedList<>(filteredItems);
            pendingChangesTable.setItems(sortedItems);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to refresh table data.", e);
        }
    }

    private boolean isMyRequestsActive = false;

    @FXML
    private void handleMyRequests() {
        LOGGER.log(Level.INFO, "Navigating to My Requests view.");
        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "Cannot filter: Current user is null.");
            return;
        }
        if (!isMyRequestsActive) {

            LOGGER.log(Level.INFO, "Filtering for My Requests only.");
            filteredItems.setPredicate(change -> {
                String requestUser = change.getRequestByName();
                String myUser = currentUser.getFullName();
                return requestUser != null && requestUser.equals(myUser);
            });

            myRequestsButton.setText("Show All Requests");
            myRequestsButton.getStyleClass().add("btn-reject");
            isMyRequestsActive = true;
        } else {
            LOGGER.log(Level.INFO, "Showing All Requests.");
            filteredItems.setPredicate(p -> true);

            // Επαναφορά κουμπιού
            myRequestsButton.setText("My Requests");
            myRequestsButton.getStyleClass().remove("btn-reject");
            isMyRequestsActive = false;
        }
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
