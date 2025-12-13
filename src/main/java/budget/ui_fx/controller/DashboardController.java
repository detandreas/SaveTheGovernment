package budget.ui_fx.controller;
import budget.ui_fx.util.SceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Base controller class for dashboard views in the application.
 * Provides common functionality for displaying user information,
 * loading views in the center section, and handling logout actions.
 * This class is intended to be extended by specific dashboard controllers
 * such as CitizenDashboardController.
 */
public class DashboardController {
    @FXML private BorderPane mainBorderPane;
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;

    /**
     * Initializes the dashboard controller.
     * Called automatically by JavaFX after the FXML is loaded.
     * Loads the default TotalBudgetView in the center section.
     */
    @FXML
    public void initialize() {
        loadCenterView("/view/TotalBudgetView.fxml");
    }

    /**
     * Sets the user information displayed in the dashboard.
     *
     * @param username the full name of the user to display
     * @param role the role of the user to display
     */
    public void setUserInfo(String username, String role) {
        getFullNameLabel().setText(username);
        getRoleLabel().setText(role);
    }

    /**
     * Loads an FXML view and displays it in the center
     *                                  section of the dashboard.
     *
     * @param fxmlPath the path to the FXML file to load
     */
    protected void loadCenterView(String fxmlPath) {
        Parent view = SceneLoader.loadNode(fxmlPath);

        if (view != null) {
            getMainBorderPane().setCenter(view);
        }
    }

    /**
     * Handles the logout action event.
     * Navigates the user back to the login view.
     *
     * @param event the action event that triggered the logout
     */
    @FXML
    protected void handleLogout(ActionEvent event) {
        // Για το Logout χρειαζόμαστε το Stage.
        // Το παίρνουμε από το mainBorderPane που είναι ήδη στη σκηνή.
        Stage currentStage = (Stage) getMainBorderPane().getScene().getWindow();

        SceneLoader.load(
            currentStage,
            "/view/LoginView.fxml",
            "Login - Save The Government"
        );
    }

    /**
     * Returns the main border pane of the dashboard.
     *
     * @return the main border pane
     */
    protected BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    /**
     * Returns the label displaying the full name.
     *
     * @return the full name label
     */
    protected Label getFullNameLabel() {
        return fullNameLabel;
    }

    /**
     * Returns the label displaying the role.
     *
     * @return the role label
     */
    protected Label getRoleLabel() {
        return roleLabel;
    }
}
