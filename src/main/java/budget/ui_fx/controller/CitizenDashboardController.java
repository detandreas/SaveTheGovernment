package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;

import budget.ui_fx.util.SceneLoader;


public class CitizenDashboardController {
    @FXML private BorderPane mainBorderPane;
    @FXML private Label usernameLabel;
    @FXML private Label userRoleLabel;

    private final SceneLoader sceneLoader = new SceneLoader(null);

    @FXML
    public void initialize() {
        loadCenterView("/view/HomeView.fxml");
    }

    public void setUserInfo(String username, String role) {
        usernameLabel.setText(username);
        userRoleLabel.setText(role);
    }

    // --- Navigation Events (Clicks στο Menu) ---

    @FXML
    private void handleHome(MouseEvent event) {
        loadCenterView("/view/HomeView.fxml");
    }

    @FXML
    private void handleViewTotalBudget(MouseEvent event) {
        // Πρέπει να έχεις φτιάξει το αντίστοιχο FXML αρχείο
        loadCenterView("/view/TotalBudgetView.fxml");
    }

    @FXML
    private void handleViewChangeHistory(MouseEvent event) {
        loadCenterView("/view/HistoryView.fxml");
    }

    @FXML
    private void handleViewStatistics(MouseEvent event) {
        loadCenterView("/view/StatisticsView.fxml");
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
        loginLoader.load("/view/Login.fxml", "Login - Save The Government");
    }

}
