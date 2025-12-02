package budget.service;

import budget.exceptions.UserNotAuthorizedException;
import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.GovernmentMember;
import budget.repository.BudgetRepository;


import java.util.List;
import java.util.Collections;
import java.util.Optional;
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

    /**
     * Constructs a new BudgetService with the necessary dependencies.
     * @param budgetRepository the repository for budget data operations
     * @param authorizationService the service used to check user permissions
     * @param changeLogService the service used to track history of changes
     * @param changeRequestService the service used to handle pending requests
     */
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
     * @param newAmount the new value
     */
    public void updateItem(User user, Budget budget,
            BudgetItem item, double newAmount) {
        synchronized (LOCK) {
            if (user == null || budget == null || item == null) {
                throw new
                    IllegalArgumentException("User, Budget or"
                        + "BudgetItem cannot be null");
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
                throw new UserNotAuthorizedException("Prime Minister cannot edit items directly.");
            }
            if (authorizationService.canUserEditBudgetItem(user, item)) {
                item.setValue(newAmount);
                recalculateBudgetTotals(budget);
                budgetRepository.save(budget);
                LOGGER.info(String.format("Item %d updated directly by %s",
                    item.getId(), user.getFullName()));
            } else if (authorizationService.canUserSubmitRequest(user, item)) {
                LOGGER.
                    info(String.
                    format("Pending change request created for item id = %d",
                    item.getId()));
            } else {
                throw new UserNotAuthorizedException(
                    String.
                    format("%s is not authorized to edit or"
                    + "submit change request for item id = %d",
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
    public void createNewBudgetItem(
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
                throw new
                IllegalArgumentException("Ministries list cannot be empty");
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
                "Only members of the Finance Ministry"
                + " can directly edit this budget item."
                );
            }
            if (budgetRepository.existsByItemId(id, budget.getYear())) {
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
     * Deletes a budget item based on the provided ID.
     * Only government members of the Finance Ministry can delete items.
     * @param user the user performing the deletion
     * @param id unique identifier of the budget item to be deleted
     * @throws IllegalArgumentException if the user is null
     * or the item is not found
     * @throws UserNotAuthorizedException if the user is not authorized
     * to delete items
     */
    public void deleteBudgetItem(User user, int id) {
        synchronized (LOCK) {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (!(user instanceof GovernmentMember gm)
                || gm.getMinistry() != Ministry.FINANCE) {
                throw new
                    UserNotAuthorizedException("Only Finance Ministry"
                    + "can delete items.");
            }
            Optional<Budget> budgetOptional = findBudgetByItemId(id);
            if (budgetOptional.isEmpty()) {
                throw new
                    IllegalArgumentException("Item with ID "
                    + id + " not found.");
            }
            Budget budget = budgetOptional.get();
            boolean remove = budget.getItems().
                removeIf(item -> item.getId() == id);
            if (remove) {
                recalculateBudgetTotals(budget);
                budgetRepository.save(budget);
                LOGGER.info(String.format("Item %d deleted by %s",
                    id, user.getFullName()));
            }
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
    /**
     * Retrieves the Budget entity that contains the specified item ID.
     * Searches across all loaded budgets to identify which one holds
     * the item with the provided unique identifier.
     * @param itemId the unique identifier of the budget item to search for
     * @return an Optional containing the Budget that holds the item,
     * or empty if the item is not found in any budget
     */
    public Optional<Budget> findBudgetByItemId(int itemId) {
        synchronized (LOCK) {
           return budgetRepository.load().stream()
                .filter(b -> b.getItems()
                .stream().anyMatch(i -> i.getId() == itemId))
                .findFirst();
       }
   }
   /**
     * Retrieves a list of budget items associated with a specific ministry.
     * Searches across all loaded budgets and filters items that are linked
     * to the provided ministry. If the input ministry is null, a warning is
     * logged and an empty list is returned.
     * @param ministry the ministry to filter by;if null, an empty list
     * is returned
     * @return a list of {@link BudgetItem} objects associated with the
     * given ministry
     */
   public List<BudgetItem> getItemsByMinistry(Ministry ministry) {
    synchronized (LOCK) {
        if (ministry == null) {
            LOGGER.warning("Ministry cannot be null");
            return Collections.emptyList();
        }
        return budgetRepository.load().stream()
            .flatMap(budget -> budget.getItems().stream())
            .filter(item -> item.getMinistries().contains(ministry))
            .toList();
    }
   }
}
