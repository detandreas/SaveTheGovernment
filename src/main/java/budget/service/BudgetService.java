package budget.service;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.GovernmentMember;
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
    private final UserAuthorizationService authorizationService;
    private final ChangeLogService changeLogService;
    private final ChangeRequestService changeRequestService;

    public BudgetService(BudgetRepository budgetRepository,
                        UserAuthorizationService authorizationService,
                        ChangeLogService changeLogService,
                        ChangeRequestService changeRequestService) {
        this.budgetRepository = budgetRepository;
        this.authorizationService = authorizationService;
        this.changeLogService = changeLogService;
        this.changeRequestService = changeRequestService;
    }
    /**
     * Updates the amount of an existing BudgetItem.
     * If user has full rights (PrimeMinister) -> update directly.
     * Otherwise -> create PendingChange.
     */
    public void updateItem(User user, Budget budget, BudgetItem item, double newAmount) {
        synchronized (LOCK) {
            if (budget == null || item == null) {
                throw new IllegalArgumentException("Budget or BudgetItem cannot be null");
            }

            double oldValue = item.getValue();

            if (user instanceof PrimeMinister) {
                LOGGER.
                    warning("Prime Minister cannot edit items directly");
                return;
            }
            
            if(authorizationService.canUserEditBudgetItem(user, item)) {
                if (newAmount < 0) {
                    throw new IllegalArgumentException("Value cannot be negative");
                }
                if (newAmount == oldValue) {
                    LOGGER.
                        info(String.
                            format("No change in value for BudgetItem id = %d",
                            item.getId()));
                    return;
                }
                item.setValue(newAmount);
                budgetRepository.save(budget);
                changeLogService.recordChange(item, oldValue, newAmount);
            } else if (authorizationService.canUserSubmitRequest(user, item)) {
                changeRequestService.submitChangeRequest(user, item, newAmount);
                LOGGER.
                    info(String.
                            format("Pending change request created for item id = %d",
                            item.getId()));
            } else {
                LOGGER.
                    warning(String.
                            format("%s is not authorized to edit or submit change request for item id = %d",
                            user.getFullName(),
                            item.getId()));
            }
            
            
        }
        
    }
}
