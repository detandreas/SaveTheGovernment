package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

/**
 * Controller for the Citizen Dashboard.
 * Handles navigation and display of various views for citizens.
 */
public class CitizenDashboardController extends DashBoardController {

    /**
     * Initializes the dashboard by loading the TotalBudgetView
     * in the center section of the page.
     */
    @FXML
    @Override
    public void initialize() {
        loadCenterView("/view/TotalBudgetView.fxml");
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
        loadCenterView("/view/HomeView.fxml");
    }

    /**
     * Handles the click event for the Total Budget menu item.
     * Loads the TotalBudgetView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleTotalBudget(MouseEvent event) {
        loadCenterView("/view/TotalBudgetView.fxml");
    }

    /**
     * Handles the click event for the History menu item.
     * Loads the HistoryView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleHistory(MouseEvent event) {
        loadCenterView("/view/HistoryView.fxml");
    }

    /**
     * Handles the click event for the Statistics menu item.
     * Loads the StatisticsView in the center section of the dashboard.
     *
     * @param event the mouse event that was triggered
     */
    @FXML
    private void handleStatistics(MouseEvent event) {
        loadCenterView("/view/StatisticsView.fxml");
    }
}
