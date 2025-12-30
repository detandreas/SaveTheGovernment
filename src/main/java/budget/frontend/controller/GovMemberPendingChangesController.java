package budget.frontend.controller;

import budget.backend.model.domain.BudgetItem;
import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.User;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.ChangeRequestService;
import budget.backend.repository.ChangeRequestRepository;
import budget.backend.service.BudgetService;
import budget.backend.repository.UserRepository;
import budget.backend.service.BudgetValidationService;
import budget.backend.service.ChangeLogService;
import budget.backend.repository.ChangeLogRepository;
import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import budget.frontend.util.SceneLoader.ViewResult;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.time.Year;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private ObservableList<PendingChange> allItems;
    private FilteredList<PendingChange> filteredItems;
    private SortedList<PendingChange> sortedItems;
    private BudgetService budgetService;
    private User currentUser;

    public void setCurrentUser(User user) {
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

        itemNameColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getBudgetItemName()));

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
    private boolean openRequestWindow() {
        try {
            ViewResult<CreateChangeRequestController> result = 
                SceneLoader.loadViewWithController(Constants.CREATE_CHANGE_REQUEST_VIEW);
            
            if (result == null) return false;

            Parent root = result.getRoot(); 
            CreateChangeRequestController popupController = result.getController();

            int currentYear = Year.now().getValue();
            ObservableList<BudgetItem> items = budgetService.getBudgetItemsForTable(currentYear);
            popupController.setBudgetItems(items);

            Stage stage = new Stage();
            stage.setTitle("New Budget Change Request");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            stage.showAndWait();

            // --- ΛΟΓΙΚΗ ΑΠΟΘΗΚΕΥΣΗΣ ---
            
            // 1. Ελέγχουμε αν πατήθηκε το Submit στο παράθυρο
            if (popupController.isSubmitClicked()) {
                
                // 2. Παίρνουμε τα δεδομένα
                BudgetItem selectedItem = popupController.getSelectedBudgetItem();
                Double newValue = popupController.getNewValue();

                // 3. Έλεγχος εγκυρότητας
                if (selectedItem != null && newValue != null && currentUser != null) {
                    
                    // 4. ΚΑΛΕΣΜΑ ΤΟΥ SERVICE (Αυτό γράφει στο JSON)
                    changeRequestService.submitChangeRequest(
                        currentUser, 
                        selectedItem, 
                        newValue
                    );
                    
                    return true; 
                } else {
                    LOGGER.log(Level.WARNING, "Submission failed: Missing item, value, or user.");
                }
            }

            return false; // Αν πάτησε Cancel

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening request window", e);
            return false;
        }
    }

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

    @FXML
    private void handleMyRequests() {
        LOGGER.log(Level.INFO, "Navigating to My Requests view.");
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
