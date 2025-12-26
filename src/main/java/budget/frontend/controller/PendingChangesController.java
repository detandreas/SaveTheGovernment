package budget.frontend.controller;

import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.service.ChangeRequestService;
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
        LOGGER.log(Level.INFO, "PendingChangesController initialized (Waiting for data injection).");
    }

    public void initData(ChangeRequestService service, PrimeMinister pm) {
        this.changeRequestService = service;
        this.currentUser = pm;
        
        LOGGER.log(Level.INFO, "Data injected into controller. Loading pending requests...");
        loadData();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getSubmittedDate()));

        actorColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getRequestByName()));

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
                container.setAlignment(Pos.CENTER);

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
        if (changeRequestService == null) {
            LOGGER.log(Level.WARNING, "ChangeRequestService is null! Cannot load data.");
            return;
        }

        try {
            allItems = changeRequestService.getAllPendingChangesSortedByDate();
            
            LOGGER.log(Level.INFO, "Loaded {0} pending changes from service.", allItems.size());

            filteredItems = new FilteredList<>(allItems, p -> true);
            sortedItems = new SortedList<>(filteredItems);
            sortedItems.comparatorProperty().bind(pendingChangesTable.comparatorProperty());

            pendingChangesTable.setItems(sortedItems);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load pending changes.", e);
        }
    }

    private void handleApprove(PendingChange change) {
        LOGGER.log(Level.INFO, "Attempting to approve request ID: {0}", change.getId());
        
        try {
            changeRequestService.approveRequest(currentUser, change);
            allItems.remove(change);
            
            LOGGER.log(Level.INFO, "SUCCESS: Request {0} approved and removed from table.", change.getId());
            
        } catch (Exception e) {
            // Το Level.SEVERE καταγράφει και το stack trace του exception
            LOGGER.log(Level.SEVERE, "FAILURE: Could not approve request " + change.getId(), e);
        }
    }

    private void handleReject(PendingChange change) {
        LOGGER.log(Level.INFO, "Attempting to reject request ID: {0}", change.getId());

        try {
            changeRequestService.rejectRequest(currentUser, change);
            allItems.remove(change);
            
            LOGGER.log(Level.INFO, "SUCCESS: Request {0} rejected and removed from table.", change.getId());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FAILURE: Could not reject request " + change.getId(), e);
        }
    }
}