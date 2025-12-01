package budget.service;

import budget.exceptions.UserNotAuthorizedException;
import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.GovernmentMember;
import budget.repository.BudgetRepository;


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
     * Updates a budget item's value. 
     * If the user can edit directly -> update immediately.
     * Otherwise, create a pending change request.
     * @param user the user performing the action
     * @param budget the budget containing the item
     * @param item the budget item to update
     * @param newValue the new value
     */
    public void updateItem(User user, Budget budget, BudgetItem item, double newAmount) {
        synchronized (LOCK) {
            if (user == null || budget == null || item == null) {
                throw new IllegalArgumentException("User, Budget or BudgetItem cannot be null");
            }
            if (newAmount < 0) {
                throw new IllegalArgumentException("Value cannot be negative");
            }

            double oldValue = item.getValue();
            if (newAmount == oldValue) {
                LOGGER.
                    info(String.
                        format("No change in value for BudgetItem id = %d",
                        item.getId()));
                return;
            }

            if (user instanceof PrimeMinister) {
                LOGGER.
                    warning("Prime Minister cannot edit items directly");
                return;
            }
            
            if(authorizationService.canUserEditBudgetItem(user, item)) {
                item.setValue(newAmount);
                recalculateBudgetTotals(budget);
                budgetRepository.save(budget);
                changeLogService.recordChange(item, oldValue, newAmount);
                LOGGER.info(String.format("Item %d updated directly by %s", 
                    item.getId(), user.getFullName()));
            } else if (authorizationService.canUserSubmitRequest(user, item)) {
                changeRequestService.submitChangeRequest(user, item, newAmount);
                LOGGER.
                    info(String.
                            format("Pending change request created for item id = %d",
                            item.getId()));
            } else {
                throw new UserNotAuthorizedException(
                    String.
                    format("%s is not authorized to edit or submit change request for item id = %d",
                    user.getFullName(),
                    item.getId()));
            }
        }
    }
    /**
     * Finds a BudgetItem by id across all budgets.
     * @param id the item id
     * @return an Optional containing the BudgetItem if found, or empty if not
     */
    public Optional<BudgetItem> findItemById(int id) {
        synchronized (LOCK) {
            return budgetRepository.load().stream()
            .flatMap(budget -> budget.getItems().stream())
            .filter(item -> item.getId() == id)
            .findFirst();
        }
    }
    /**
    * Creates a new budget item and adds it to the given budget.
    * Only government members of the Finance Ministry can create new items.
    * @param user the user performing the creation
    * @param budget the budget to which the item will be added
    * @param id unique identifier for the new budget item
    * @param name name of the new budget item
    * @param value monetary value of the new budget item
    * @param isRevenue true if the item is revenue, false if expense
     * @param ministries list of ministries associated with the item
    */
    public void creatNewBudgetItem(
        User user,
        Budget budget,
        int id,
        String name,
        double value,
        boolean isRevenue,
        List<Ministry> ministries
    ) {
        synchronized (LOCK) {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (budget == null) {
                throw new IllegalArgumentException("Budget cannot be null");
            }
            if (ministries == null || ministries.isEmpty()) {
                throw new IllegalArgumentException("Ministries list cannot be empty");
            }
            if (value < 0) {
                throw new IllegalArgumentException("Value cannot be negative");
            }
            
            if (!(user instanceof GovernmentMember gm)) {
                throw new UserNotAuthorizedException(
                "Only government members can edit budget items."
                );
            }
            if (gm.getMinistry() != Ministry.FINANCE) {
                throw new UserNotAuthorizedException(
                "Only members of the Finance Ministry "
                + "can directly edit this budget item."
                );
            }
            if (budgetRepository.existsByItemId(id)) {
                throw new IllegalArgumentException(
                    "A BudgetItem with id " + id + " already exists."
                );
            }

            BudgetItem newItem = new BudgetItem(
                id,
                budget.getYear(),
                name,
                value,
                isRevenue,
                ministries);
            
            List<BudgetItem> items = budget.getItems();
            items.add(newItem);
            budget.setItems(items);

            recalculateBudgetTotals(budget);

            budgetRepository.save(budget);

            changeLogService.recordChange(newItem, 0.0, value);

            LOGGER.info(String.format(
                "Created new BudgetItem (id=%d, name=%s) in budget %d by %s",
                id, name, budget.getYear(), user.getFullName()
            ));
        }
    }
    /**
     * Recalculates the financial totals for the specified budget.
     * Iterates through all budget items to sum up revenues and expenses,
     * then updates the total revenue, total expense, and net result fields
     * of the budget object to ensure data consistency.
     *
     * @param budget the budget entity whose totals need to be updated
     */
    private void recalculateBudgetTotals(Budget budget) {
        double totalRevenue = 0;
        double totalExpense = 0;

        for (BudgetItem i : budget.getItems()) {
            if (i.getIsRevenue()) {
                totalRevenue += i.getValue();
            } else {
                totalExpense += i.getValue();
            }
        }
        budget.setTotalRevenue(totalRevenue);
        budget.setTotalExpense(totalExpense);
        budget.setNetResult(totalRevenue - totalExpense);
    }
    /**
     * Retrieves all budget entities from the repository.
     * @return a list containing all Budget objects available in the system
     */
    public List<Budget> getAllBudgets() {
        synchronized (LOCK) {
            return budgetRepository.load();
        }
    }
}
