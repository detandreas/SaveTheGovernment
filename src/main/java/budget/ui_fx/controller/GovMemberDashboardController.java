package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import java.io.IOException;

import budget.ui_fx.util.SceneLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GovMemberDashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label usernameLabel;
    @FXML private Label userRoleLabel;

    private final SceneLoader sceneLoader = new SceneLoader(null);

    @FXML
    public void initialize() {
        loadCenterView("/views/GovMemberDashboard.fxml");
    }

    public void setUserInfo(String username, String role) {
        usernameLabel.setText(username);
        userRoleLabel.setText(role);
    }

    // --- Navigation Events (Clicks στο Menu) ---

    @FXML
    private void handleHome(MouseEvent event) {
        loadCenterView("/views/HomeView.fxml");
    }

    @FXML
    private void handleViewTotalBudget(MouseEvent event) {
        // Πρέπει να έχεις φτιάξει το αντίστοιχο FXML αρχείο
        loadCenterView("/views/TotalBudgetView.fxml");
    }

    @FXML
    private void handleViewChangeHistory(MouseEvent event) {
        loadCenterView("/views/HistoryView.fxml");
    }

    @FXML
    private void handleViewStatistics(MouseEvent event) {
        loadCenterView("/views/StatisticsView.fxml");
    }

    @FXML
    private void handleCreateRequest(MouseEvent event) {
        loadCenterView("/views/CreateRequestView.fxml");
    }

    @FXML
    private void handleRequestHistory(MouseEvent event) {
        loadCenterView("/views/RequestHistoryView.fxml");
    }

    private void loadCenterView(String fxmlPath) {
        // Καλούμε τη ΝΕΑ μέθοδο loadNode
        Parent view = sceneLoader.loadNode(fxmlPath);
        
        if (view != null) {
            mainBorderPane.setCenter(view);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Για το Logout χρειαζόμαστε το Stage. 
        // Το παίρνουμε από το mainBorderPane που είναι ήδη στη σκηνή.
        javafx.stage.Stage currentStage = (javafx.stage.Stage) mainBorderPane.getScene().getWindow();
        
        SceneLoader loginLoader = new SceneLoader(currentStage);
        loginLoader.load("/views/Login.fxml", "Login - Save The Government");
    }

}
