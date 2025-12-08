package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;

import budget.ui_fx.util.SceneLoader;
import budget.model.domain.user.User;
import budget.service.UserAuthenticationService;


public class CitizenDashboardController {
    @FXML private BorderPane mainBorderPane;
    @FXML private Label fullNameLabel;

    private final SceneLoader sceneLoader = new SceneLoader(null);

    @FXML
    public void initialize() {
        User user = UserAuthenticationService.getCurrentUser();
        if (user != null) {
            fullNameLabel.setText(user.getFullName()); 
        }
        loadCenterView("/view/TotalBudgetView.fxml");
    }

    public void setUserInfo(String fullName, String role) {
        fullNameLabel.setText(fullName);
    }

    // --- Navigation Events (Button Actions) ---

    @FXML
    private void handleHome(ActionEvent event) {
        loadCenterView("/view/HomeView.fxml");
    }

    @FXML
    private void handleTotalBudget(ActionEvent event) {
        loadCenterView("/view/TotalBudgetView.fxml");
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        loadCenterView("/view/HistoryView.fxml");
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        loadCenterView("/view/StatisticsView.fxml");
    }

    // --- Helper Method ---

    private void loadCenterView(String fxmlPath) {
        try {
            Parent view = sceneLoader.loadNode(fxmlPath);
            if (view != null) {
                mainBorderPane.setCenter(view);
            } else {
                System.err.println("Δεν ήταν δυνατή η φόρτωση του View: " + fxmlPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) mainBorderPane.getScene().getWindow();
            SceneLoader loginLoader = new SceneLoader(currentStage);
            loginLoader.load("/view/LoginView.fxml", "Login - Save The Government");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
