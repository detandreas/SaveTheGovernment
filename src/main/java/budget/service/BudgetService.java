package budget.service;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.Ministry;
import budget.model.user.User;
import budget.repository.BudgetRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing budgets.
 */
public class BudgetService {

    private static final Logger LOGGER =
            Logger.getLogger(BudgetService.class.getName());
    private static final Object LOCK = new Object();

    private final BudgetRepository budgetRepository;
    private final UserAuthorizationService userAuthorizationService;
    private final ChangeLogService changeLogService;
    private final ChangeRequestService changeRequestService;

    public BudgetService(BudgetRepository budgetRepository,
                        UserAuthorizationService userAuthorizationService,
                        ChangeLogService changeLogService,
                        ChangeRequestService changeRequestService) {
        this.budgetRepository = budgetRepository;
        this.userAuthorizationService = userAuthorizationService;
        this.changeLogService = changeLogService;
        this.changeRequestService = changeRequestService;
                        }
}
