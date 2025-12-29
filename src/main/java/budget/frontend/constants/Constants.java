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
                                        "/view/GovernmentMemberDashboardView.fxml";
    public static final String FINANCE_GOV_VIEW =
                                    "/view/FinanceGovernmentMemberDashboardView.fxml";
    public static final String PRIME_MINISTER_VIEW =
                                    "/view/PrimeMinisterDashboardView.fxml";

    // Pop-up Window view
    public static final String CREATE_CHANGE_REQUEST_VIEW =
                                    "/view/CreateChangeRequestView.fxml";
    // Legacy names for backward compatibility
    public static final String CITIZEN_VIEW = CITIZEN_DASHBOARD_VIEW;
    public static final String GOV_VIEW = GOV_MEMBER_DASHBOARD_VIEW;

    // Center views (loaded inside dashboards)
    public static final String TOTAL_BUDGET_VIEW =
                                            "/view/TotalBudgetView.fxml";
    public static final String HOME_VIEW = "/view/HomeView.fxml";
    public static final String HISTORY_VIEW = "/view/ChangeLogView.fxml";
    public static final String STATISTICS_VIEW = "/view/StatisticsView.fxml";
    public static final String PENDING_CHANGES_VIEW =
                                    "/view/PendingChangesView.fxml";
    public static final String GOV_MEMBER_PENDING_CHANGES_VIEW =
                                "/view/GovMemberPendingChangesView.fxml";

    // Chart labels and budget item names
    public static final String LOANS_ITEM_NAME = "Loans";
    public static final String LOAN_KEYWORD = "loan";
    public static final String REVENUE_LABEL = "Revenue";
    public static final String EXPENSE_LABEL = "Expense";
    public static final String EXPENSES_LABEL = "Expenses";
    public static final String OTHERS_LABEL = "Others";
    public static final String NET_RESULT_LABEL = "Net result";
    public static final String BUDGET_OVERVIEW_LABEL = "Budget Overview";
    public static final String TOP_REVENUE_LABEL = "Top Revenue";
    public static final String TOP_EXPENSE_LABEL = "Top Expense";
    public static final String REVENUE_LOANS_LABEL = "Revenue Loans";
    public static final String EXPENSE_LOANS_LABEL = "Expense Loans";

    // defaults
    public static final int TOP_N_ITEMS = 5;
}
