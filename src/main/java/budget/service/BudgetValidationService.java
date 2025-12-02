package budget.service;

import budget.constants.Limits;
import budget.constants.Message;
import budget.exceptions.ValidationException;
import budget.model.domain.BudgetItem;
import budget.repository.BudgetRepository;
import budget.model.domain.Budget;
import budget.model.enums.Ministry;
import java.util.List;
import java.util.Set;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Service class responsible for validating budget-related operations.
 * Provides validation methods for budget item creation, updates, and deletions
 * to ensure data integrity and business rule compliance.
 */
public class BudgetValidationService {

    private final BudgetRepository budgetRepository;

    // budgetItems that cannot be deleted
    private static final Set<String> PROTECTED_BUDGET_NAMES = Set.of(
        "Defense", "Education", "Health"
    );
    /**
     * Constructs a new BudgetValidationService with the specified repository.
     *
     * @param budgetRepository the repository used for budget item data access
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification
        = "This allows testability and shared state across service instances."
    )
    public BudgetValidationService(
        BudgetRepository budgetRepository
    ) {
        this.budgetRepository = budgetRepository;
    }

    // Δημιουργία BudgetItem

    /**
     * Validates all requirements for creating a new budget item.
     * Checks for unique ID, unique name, non-negative amount, valid ministries,
     * and balance change limits.
     *
     * @param newItem the budget item to be created
     * @param budget the current budget to validate against
     * @throws ValidationException if any validation rule is violated
     */
    public void validateBudgetItemCreation(BudgetItem newItem, Budget budget)
    throws ValidationException {
        if (newItem == null) {
            throw new ValidationException("Budget item cannot be null");
        }
        if (budget == null) {
            throw new ValidationException("Budget cannot be null");
        }
        validateUniqueId(newItem.getId(), newItem.getYear());
        validateUniqueName(newItem.getName(), newItem.getYear());
        validateNonNegativeAmount(newItem.getValue());
        validateMinistry(newItem.getMinistries());
        validateBalanceChangeLimit(newItem, budget);
    }
    /**
     * Validates that the specified budget item ID is unique in the system.
     *
     * @param id the ID to check for uniqueness.
     * @param year the year of the Budget in which we search for uniqueness.
     * @throws ValidationException if the ID already exists
     */
    private void validateUniqueId(int id, int year)
    throws ValidationException {
        boolean idExists = budgetRepository.existsByItemId(id, year);

        if (idExists) {
            throw new ValidationException(
                Message.DUPLICATE_BUDGET_ITEM_ERROR
            );
        }
    }
    /**
     * Validates that the specified budget item name is unique in the system.
     *
     * @param budgetItemName the name to check for uniqueness
     * @param year the year of the budgetItem we are searching
     * @throws ValidationException if the name already exists
     */
    private void validateUniqueName(String budgetItemName, int year)
    throws ValidationException {
        boolean nameExists = budgetRepository
                                        .existsByName(budgetItemName, year);

        if (nameExists) {
            throw new ValidationException(
                Message.DUPLICATE_BUDGET_ITEM_ERROR
            );
        }
    }
    /**
     * Validates that the specified amount is non-negative.
     *
     * @param value the amount to validate
     * @throws ValidationException if the amount is negative
     */
    private void validateNonNegativeAmount(double value)
    throws ValidationException {
        if (value < Limits.MIN_BUDGET_ITEM_AMOUNT) {
            throw new ValidationException(Message.NON_NEGATIVE_AMOUNT_ERROR);
        }
    }
    /**
     * Validates that the ministry list contains valid Ministry enum values
     * and is not null or empty.
     *
     * @param ministries the list of ministries to validate
     * @throws ValidationException if the ministry list is invalid
     */
    private void validateMinistry(List<Ministry> ministries) {
        if (ministries == null || ministries.isEmpty()) {
            throw new ValidationException("ministry field can't be empty");
        }

        for (Ministry ministry : ministries) {
            if (ministry == null) {
                throw new ValidationException(
                    "Cannot belong to null ministry");
            }
        }
    }
    /**
     * Validates that adding the new budget item does not exceed
     * the allowed balance change limits.
     *
     * @param newItem the budget item to be added
     * @param budget the current budget to check against
     * @throws ValidationException if balance limits are exceeded
     */
    private void validateBalanceChangeLimit(BudgetItem newItem, Budget budget)
    throws ValidationException {
        double currentNetResult = Math.abs(budget.getNetResult());
        // Αν το ισοζύγιο ειναι 0 επιτρέπεται οποιαδήποτε αλλαγη
        if (currentNetResult <= Limits.SMALL_NUMBER) {
            return;
        }
        double newNetResult = calculateNewNetResult(budget, newItem);
        double changePercent = calculateChangePercent(
                                        newNetResult, budget.getNetResult());

        if (changePercent > Limits.BALANCE_CHANGE_LIMIT_PERCENT) {
            throw new ValidationException(
                String.format(
                    "Introducing this account will change the "
                    + "budget balance by %.2f%%, "
                    + "which exceeds the allowed limit ±%.2f%%",
                    changePercent * Limits.NUMBER_ONE_HUNDRED,
                    Limits.BALANCE_CHANGE_LIMIT_PERCENT
                                                * Limits.NUMBER_ONE_HUNDRED
                )
            );
        }
    }
    /**
    * Calculates the new net result that would occur
    *               after adding the specified budget item.
    * If the item is revenue, it adds to the current net result,
    *                                                   if it's an expense,
    * it subtracts from the current net result.
    *
    * @param budget the current budget
    * @param newItem the budget item to be added
    * @return the projected net result after adding the new item
    */
    private double calculateNewNetResult(Budget budget, BudgetItem newItem) {
        double budgetNetResult = budget.getNetResult();
        if (newItem.getIsRevenue()) {
            return budgetNetResult + newItem.getValue();
        } else {
            return budgetNetResult - newItem.getValue();
        }
    }
    /**
    * Calculates the percentage change between two values.
    * Returns the absolute percentage difference
    *                               from the start value to the final value.
    *
    * @param finalValue the ending value
    * @param startValue the starting value (must not be zero)
    * @return the absolute percentage change as a decimal (e.g., 0.15 for 15%)
    * @throws IllegalArgumentException if startValue is zero
    */
    private double calculateChangePercent(
        double finalValue,
        double startValue
    ) {
        if (startValue == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        double changePercent = (finalValue - startValue) / startValue;
        return  Math.abs(changePercent);
    }

    // Διαγραφή BudgetItem

    /**
    * Validates all requirements for deleting a budget item.
    * Checks that the item and budget are not null, that existing budget items
    * can be loaded, and that the item is not protected from deletion.
    *
    * @param itemToDelete the budget item to be deleted
    * @param budget the current budget containing the item
    * @throws ValidationException if any validation rule is violated
    */
    public void validateBudgetItemDeletion(
                            BudgetItem itemToDelete, Budget budget)
    throws ValidationException {
        if (itemToDelete == null) {
            throw new ValidationException("item to delete cannot be null");
        }
        if (budget == null) {
            throw new ValidationException("Budget cannot be null");
        }
        List<BudgetItem> existingBudgetItems = budget.getItems();
        if (existingBudgetItems == null) {
            throw new ValidationException("Cannot delete:"
                                    + "Existing items cannot be null");
        }

        validateNotProtectedItem(itemToDelete);
    }
    /**
    * Validates that the specified budget item is not protected from deletion.
    * Protected items include critical budget categories such as
    *                                            Defense, Education, and Health
    * that cannot be removed from the budget system.
    *
    * @param item the budget item to check for protection status
    * @throws ValidationException if the item is protected
    *                                               and cannot be deleted
    */
    private void validateNotProtectedItem(BudgetItem item)
    throws ValidationException {
        String itemName = item.getName();
        boolean isProtected = PROTECTED_BUDGET_NAMES
                                .stream()
                                .anyMatch(protectedName ->
                                    itemName.equalsIgnoreCase(protectedName));
        if (isProtected) {
            throw new ValidationException(
                "protected BudgetItem cannot be deleted");
        }
    }

    // Επεξεργασία BudgetItem

    /**
    * Validates all requirements for updating an existing budget item.
    * Checks that both the original and updated items are not null,
    * and that the change amount does not exceed the allowed edit limits.
    *
    * @param originalItem the current budget item before modification
    * @param updatedItem the proposed budget item after modification
    * @throws ValidationException if any validation rule is violated
    */
    public void validateBudgetItemUpdate(
        BudgetItem originalItem,
        BudgetItem updatedItem
    )
    throws ValidationException {
        if (originalItem == null) {
            throw new ValidationException("Original BudgetItem can't be null");
        }
        if (updatedItem == null) {
            throw new ValidationException("updated BudgetItem can't be null");
        }
        if (originalItem.getValue() == updatedItem.getValue()) {
            throw
            new ValidationException("update doesn't change BudgetItem value");
        }
        validateNonNegativeAmount(updatedItem.getValue());
        validateEditChangeLimit(originalItem, updatedItem);
    }
    /**
    * Validates that the change between original and updated budget item values
    * does not exceed the allowed edit change limit percentage.
    * If the original value is zero, any change is permitted.
    *
    * @param original the original budget item with current values
    * @param updated the updated budget item with new values
    * @throws ValidationException if the percentage change
    *                                           exceeds the allowed limit
    */
    private void validateEditChangeLimit(
        BudgetItem original,
        BudgetItem updated
    ) throws ValidationException {
        double originValue = original.getValue();
        double updatedValue = updated.getValue();

        if (originValue == 0) {
            // οποιαδήποτε αλλαγή αν η αρχική τιμη ειναι μηδέν
            return;
        }

        double changePercent = calculateChangePercent(
                                        updatedValue, originValue);

        if (changePercent > Limits.EDIT_CHANGE_LIMIT_PERCENT) {
            throw new ValidationException(
                String.format(
                    "The change in amount (%.2f%%) exceeds "
                    + "the allowed limit ±%.2f%%. "
                    + "Additional approval required.",
                    changePercent * Limits.NUMBER_ONE_HUNDRED,
                    Limits.EDIT_CHANGE_LIMIT_PERCENT
                                                * Limits.NUMBER_ONE_HUNDRED
                )
            );
        }
    }

    // Γενικοί περιορισμοί για BudgetItem

    /**
    * Validates the general data integrity requirements for a budget item.
    * Checks that all required fields are present and have valid values,
    * including positive ID, non-empty name, valid year, ministries list,
    * and revenue/expense classification.
    *
    * @param item the budget item to validate for data integrity
    * @throws ValidationException if any data integrity rule is violated
    */
    public void validateDataIntegrity(BudgetItem item)
    throws ValidationException {
        if (item == null) {
            throw new ValidationException("BudgetItem can't be null");
        }

        if (item.getId() <= 0) {
            throw new ValidationException("BudgetItem id must be positive");
        }

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new ValidationException(
                "BudgetItem name can't be null or empty");
        }

        if (item.getYear()  < Limits.MIN_BUDGET_YEAR) {
            throw new ValidationException(
                "BudgetItem year can't be lower than 2000");
        }

        if (item.getMinistries() == null || item.getMinistries().isEmpty()) {
            throw new ValidationException(
                "BudgetItem ministries can't be null or empty");
        }
    }
}
