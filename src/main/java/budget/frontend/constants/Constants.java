package budget.frontend.constants;

/**
 * Constants class for FXML view paths.
 * All FXML files are located in src/main/resources/view/
 */
public final class Constants {
    /**
     * Private constructor to prevent instantiation.
     */
    private Constants() {
        // prevents initialization
    }

    // Main views
    public static final String WELCOME_VIEW = "/view/WelcomeView.fxml";
    public static final String LOGIN_VIEW = "/view/LoginView.fxml";
    public static final String ACCOUNT_CREATION_VIEW =
                                            "/view/AccountCreationView.fxml";

    // Dashboard views
    public static final String CITIZEN_DASHBOARD_VIEW =
                                            "/view/CitizenDashboardView.fxml";
    public static final String GOV_MEMBER_DASHBOARD_VIEW =
                                        "/view/GovMemberDashboardView.fxml";
    public static final String FINANCE_GOV_VIEW =
                                    "/view/FinanceGovMemberDashboardView.fxml";
    public static final String PRIME_MINISTER_VIEW =
                                    "/view/PrimeMinisterDashboardView.fxml";

    // Legacy names for backward compatibility
    public static final String CITIZEN_VIEW = CITIZEN_DASHBOARD_VIEW;
    public static final String GOV_VIEW = GOV_MEMBER_DASHBOARD_VIEW;

    // Center views (loaded inside dashboards)
    public static final String TOTAL_BUDGET_VIEW =
                                            "/view/TotalBudgetView.fxml";
    public static final String HOME_VIEW = "/view/HomeView.fxml";
    public static final String HISTORY_VIEW = "/view/HistoryView.fxml";
    public static final String STATISTICS_VIEW = "/view/StatisticsView.fxml";
}
