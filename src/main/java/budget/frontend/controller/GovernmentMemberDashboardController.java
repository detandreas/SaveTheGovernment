package budget.frontend.controller;

import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.User;
import budget.frontend.constants.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
/**
 * Controller for the Government Member Dashboard.
 * Handles navigation and display of various views for government members.
 */
public class GovernmentMemberDashboardController extends DashboardController {
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

    @FXML
    private void handleTotalBudget(ActionEvent event) {
        loadCenterView(Constants.TOTAL_BUDGET_VIEW);
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        loadCenterView(Constants.HISTORY_VIEW);
    }

    /**
     * Handles the Pending Changes menu item click.
     * Loads the GovMemberPendingChangesView into the center section of the dashboard.
     * @param event the action event that was triggered
     */
    @FXML
    public void handlePendingChanges(ActionEvent event) {
        User currentUser = super.getCurrentUser();

        if (currentUser instanceof GovernmentMember) {
            loadCenterView(
                Constants.GOV_MEMBER_PENDING_CHANGES_VIEW
            );
        } else {
            throw new IllegalStateException(
                "Current User is not a GovernmentMember"
            );
        }
    }

    /**
     * Handles the Statistics menu item click.
     * Loads the Statistics view into the center section of the dashboard.
     * @param event the action event that was triggered
     */
    @FXML
    private void handleStatistics(ActionEvent event) {
        loadCenterView(Constants.STATISTICS_VIEW);
    }
}
