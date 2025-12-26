package budget.frontend.controller;

import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.service.ChangeRequestService;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class PendingChangesController {
    @FXML private TableView<PendingChange> PendingChangesTable;
    @FXML private TableColumn<PendingChange, String> dateColumn; 
    @FXML private TableColumn<PendingChange, String> actorColumn;
    @FXML private TableColumn<PendingChange, Double> oldValueColumn;
    @FXML private TableColumn<PendingChange, Double> newValueColumn;
    @FXML private TableColumn<PendingChange, Double> valueDifferenceColumn;
    @FXML private TableColumn<PendingChange, Void> actionColumn;

    private ChangeRequestService changeRequestService;
    private PrimeMinister currentUser;
}