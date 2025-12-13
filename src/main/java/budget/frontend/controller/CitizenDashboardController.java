package budget.frontend.controller;

import budget.frontend.constants.Constants;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

/**
 * Controller for the Citizen Dashboard.
 * Handles navigation and display of various views for citizens.
 */
public class CitizenDashboardController extends DashboardController {

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
     * Handles the click event for the Home menu item.
     * Loads the HomeView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleHome(MouseEvent event) {
        loadCenterView(Constants.HOME_VIEW);
    }

    /**
     * Handles the click event for the Total Budget menu item.
     * Loads the TotalBudgetView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleTotalBudget(MouseEvent event) {
        loadCenterView(Constants.TOTAL_BUDGET_VIEW);
    }

    /**
     * Handles the click event for the History menu item.
     * Loads the HistoryView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleHistory(MouseEvent event) {
        loadCenterView(Constants.HISTORY_VIEW);
    }

    /**
     * Handles the click event for the Statistics menu item.
     * Loads the StatisticsView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleStatistics(MouseEvent event) {
        loadCenterView(Constants.STATISTICS_VIEW);
    }
}
