package budget.frontend.controller;
import java.util.function.Consumer;

import budget.backend.model.domain.user.User;
import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    private User currentUser;

    /**
     * Sets the currently authenticated user.
     * This method is called by LoginController after successful authentication.
     *
     * @param user the authenticated user object
     */
        @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
            "Controller needs a reference to the"
            + "external mutable User object by design."
    )
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the current user, or null if not set
     */
    protected User getCurrentUser() {
        return currentUser;
    }

    /**
     * Initializes the dashboard controller.
     * Called automatically by JavaFX after the FXML is loaded.
     * Loads the default TotalBudgetView in the center section.
     */
    @FXML
    public void initialize() {
        loadCenterView(Constants.TOTAL_BUDGET_VIEW);
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
     * Loads an FXML view and displays it in the center section
     *                                               of the dashboard.
     * Allows for controller configuration after loading.
     *
     * @param <T> The type of the controller
     * @param fxmlPath the path to the FXML file to load
     * @param controllerType the class type of the controller
     * @param controllerConfigurator a consumer to configure
     *                                          the controller after loading
     */
    protected <T> void loadCenterView(
        String fxmlPath,
        Class<T> controllerType,
        Consumer<T> controllerConfigurator
    ) {
        SceneLoader.ViewResult<T> result = SceneLoader.loadView(fxmlPath);

        if (result != null && result.getRoot() != null) {
            if (controllerConfigurator != null
                                    && result.getController() != null) {
                controllerConfigurator.accept(result.getController());
            }
            getMainBorderPane().setCenter(result.getRoot());
        }
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
            Constants.LOGIN_VIEW,
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
