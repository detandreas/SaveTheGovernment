package budget.frontend.controller;

import budget.backend.model.domain.user.PrimeMinister;
import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Controller for the Citizen Dashboard.
 * Handles navigation and display of various views for citizens.
 */
public class PrimeMinisterDashboardController extends DashboardController {

    @FXML
    private BorderPane mainBorderPane;

    private static final Logger LOGGER =
        Logger.getLogger(PrimeMinisterDashboardController.class.getName());

    /**
     * Initializes the dashboard by loading the TotalBudgetView
     * in the center section of the page.
     */
    @FXML
    @Override
    public void initialize() {
        loadCenterView(Constants.TOTAL_BUDGET_VIEW);
    }

    // --- Navigation Events (Clicks στο Menu) ---

    /**
     * Handles the click event for the Total Budget menu item.
     * Loads the TotalBudgetView in the center section of the dashboard.
     *
     * @param event the action event that was triggered
     */
    @FXML
    private void handleTotalBudget(ActionEvent event) {
        loadCenterView(Constants.TOTAL_BUDGET_VIEW);
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        loadCenterView(Constants.HISTORY_VIEW);
    }
    /**
     * Handles the click event for the Pending Changes menu item.
     * Loads the PendingChangesView in the center section of the dashboard.
     */
    @FXML
    public void handlePendingChanges() {
        LOGGER.log(Level.INFO, "Button clicked: handlePendingChanges started.");

        try {
            SceneLoader.ViewResult<PendingChangesController> result =
                SceneLoader.loadView(
                    "/view/PendingChangesView.fxml"
                );

            if (result == null) {
                LOGGER.log(Level.SEVERE, "Result from SceneLoader is NULL!");
                return;
            }
            LOGGER.log(Level.INFO, "View loaded successfully.");
            if (this.mainBorderPane == null) {
                LOGGER.log(
                    Level.SEVERE,
                    "CRITICAL ERROR: mainBorderPane is NULL! Check FXML fx:id."
                );
                return;
            }

            LOGGER.log(Level.INFO, "Fetching PrimeMinister instance...");
            PrimeMinister pm = PrimeMinister.getInstance();
            LOGGER.log(Level.INFO, "Setting PrimeMinister to controller...");
            result.getController().setPrimeMinister(pm);
            LOGGER.log(Level.INFO, "Setting center of BorderPane...");
            mainBorderPane.setCenter(result.getRoot());
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE, "EXCEPTION in handlePendingChanges", e
            );
            e.printStackTrace();
        }
    }

    /**
     * Handles the click event for the Statistics menu item.
     * Loads the StatisticsView in the center section of the dashboard.
     *
     * @param event the action event that was triggered
     */
    @FXML
    private void handleStatistics(ActionEvent event) {
        loadCenterView(Constants.STATISTICS_VIEW);
    }
}
