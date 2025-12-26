package budget.frontend.controller;

import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.repository.BudgetRepository;
import budget.backend.service.ChangeRequestService;
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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingChangesController {

    private static final Logger LOGGER = Logger.getLogger(PendingChangesController.class.getName());

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

    @FXML
    public void initialize() {
        setupTableColumns();
        
        initServices();
        
        LOGGER.log(Level.INFO, "Controller UI initialized. Waiting for User Data...");
    }

    /**
     * ΑΥΤΗ η μέθοδος καλείται από τον "γονιό" controller (Dashboard)
     * ΑΦΟΥ φορτωθεί το FXML. Εδώ περνάμε τον χρήστη.
     */
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

            BudgetValidationService valService = new BudgetValidationService(budgetRepo);
            BudgetService budgetService = new BudgetService(budgetRepo);
            ChangeLogService logService = new ChangeLogService(logRepo);

            this.changeRequestService = new ChangeRequestService(
                reqRepo, budgetRepo, userRepo, valService, budgetService, logService
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing services", e);
        }
    }

     private void setupTableColumns() {
        dateColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getSubmittedDate()));

        actorColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getRequestByName()));

        itemIdColumn.setCellValueFactory(cell ->  
            new SimpleObjectProperty<>(cell.getValue().getBudgetItemId()));

        oldValueColumn.setCellValueFactory(cell -> 
            new SimpleObjectProperty<>(cell.getValue().getOldValue()));

        newValueColumn.setCellValueFactory(cell -> 
            new SimpleObjectProperty<>(cell.getValue().getNewValue()));

        valueDifferenceColumn.setCellValueFactory(cell -> {
            double diff = cell.getValue().getNewValue() - cell.getValue().getOldValue();
            return new SimpleObjectProperty<>(diff);
        });

        setupActionColumnButtons();
    }

    private void setupActionColumnButtons() {
        Callback<TableColumn<PendingChange, Void>, TableCell<PendingChange, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnAccept = new Button("Accept");
            private final Button btnReject = new Button("Reject");
            private final HBox container = new HBox(10, btnAccept, btnReject);

        {            
            // 1. Ρύθμιση του Container μέσω CSS class
            container.getStyleClass().add("action-box");
            // (Το container.setAlignment(Pos.CENTER) πλέον γίνεται στο CSS με -fx-alignment: center)

            // 2. Ρύθμιση του Accept Button
            btnAccept.getStyleClass().addAll("action-button", "btn-accept");

            // 3. Ρύθμιση του Reject Button
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
    private void loadData() {
        if (changeRequestService == null) return;

        try {
            allItems = changeRequestService.getAllPendingChangesSortedByDate();
            
            if (allItems == null) {
                LOGGER.log(Level.WARNING, "No data found.");
                return;
            }

            filteredItems = new FilteredList<>(allItems, p -> true);
            sortedItems = new SortedList<>(filteredItems);
            sortedItems.comparatorProperty().bind(pendingChangesTable.comparatorProperty());

            pendingChangesTable.setItems(sortedItems);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load table data.", e);
        }
    }

    // ... (handleApprove/Reject παραμένουν ίδια) ...
     private void handleApprove(PendingChange change) {
        LOGGER.log(Level.INFO, "Attempting to approve request ID: {0}", change.getId());
        try {
            changeRequestService.approveRequest(currentUser, change);
            allItems.remove(change);
            LOGGER.log(Level.INFO, "Request approved.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Approve failed", e);
        }
    }

    private void handleReject(PendingChange change) {
        LOGGER.log(Level.INFO, "Attempting to reject request ID: {0}", change.getId());
        try {
            changeRequestService.rejectRequest(currentUser, change);
            allItems.remove(change);
            LOGGER.log(Level.INFO, "Request rejected.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Reject failed", e);
        }
    }
}