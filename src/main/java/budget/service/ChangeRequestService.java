package budget.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.ChangeLog;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Status;
import budget.repository.BudgetRepository;
import budget.repository.ChangeLogRepository;
import budget.repository.ChangeRequestRepository;
import budget.constants.Limits;
import budget.constants.Message;
import budget.exceptions.ValidationException;

/**
 * Service for handling change requests to budget items.
 * Responsible for submitting, approving, and rejecting change requests.
 * Delegates validation to BudgetValidationService.
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetValidationService budgetValidationService;
    private final BudgetRepository budgetRepository;
    private final ChangeLogRepository changeLogRepository;
    private final BudgetService budgetService;
    /**
     * Constructor for ChangeRequestService.
     * @param changeRequestRepository repository for change requests
     * @param budgetValidationService service for validating budget changes
     * @param budgetRepository repository for budgets
     * @param changeLogRepository repository for change logs
     * @param budgetService service for budget calculations
     */
    public ChangeRequestService(
        ChangeRequestRepository changeRequestRepository,
        BudgetValidationService budgetValidationService,
        BudgetRepository budgetRepository,
        ChangeLogRepository changeLogRepository,
        BudgetService budgetService
    ) {
        this.changeRequestRepository = changeRequestRepository;
        this.budgetValidationService = budgetValidationService;
        this.budgetRepository = budgetRepository;
        this.changeLogRepository = changeLogRepository;
        this.budgetService = budgetService;
    }

    /**
     * Validates that a budget exists for the given year.
     * @param year the year to check for budget existence
     * @throws IllegalArgumentException if the budget does not exist
     */
    private void validateBudgetExists(int year)
    throws IllegalArgumentException {
        if (!budgetRepository.existsById(year)) {
            throw new IllegalArgumentException(
                "Budget for year " + year + " does not exist.");
        }
    }

    /**
     * Validates a budget item change using the validation service.
     * @param existingItem the current budget item before the change
     * @param updatedItem the budget item with the proposed new value
     * @throws IllegalArgumentException if the validation fails
     */
    private void validateBudgetItemChange(
        BudgetItem existingItem,
        BudgetItem updatedItem
    )
    throws IllegalArgumentException {
        try {
            budgetValidationService.validateBudgetItemUpdate(
                                                    existingItem, updatedItem);
        } catch (ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Creates a new pending change request.
     * @param item the budget item to be changed
     * @param user the user submitting the request
     * @param oldValue the current value of the budget item
     * @param newValue the proposed new value
     * @return a new PendingChange instance
     */
    private PendingChange createPendingChange(
        BudgetItem item,
        User user,
        double oldValue,
        double newValue
    ) {
        return new PendingChange(
            item.getId(),
            item.getYear(),
            item.getName(),
            user.getFullName(),
            user.getId(),
            oldValue,
            newValue
        );
    }

    /**
     * Creates a new BudgetItem with updated value for validation purposes.
     * @param existingItem the original budget item
     * @param newValue the new value to set
     * @return a new BudgetItem with the updated value
     */
    private BudgetItem createUpdatedItem(
        BudgetItem existingItem, double newValue) {
        return new BudgetItem(
            existingItem.getId(),
            existingItem.getYear(),
            existingItem.getName(),
            newValue,
            existingItem.getIsRevenue(),
            existingItem.getMinistries()
        );
    }
    /**
     * Submits a change request for a budget item.
     * Validates the request and saves it if valid.
     * Note: User authorization should be checked before calling this method.
     *
     * @param user the user submitting the request
     * @param item the budget item to be changed
     * @param newValue the proposed new value for the budget item
     * @throws IllegalArgumentException if validation fails
     *                                      or the item doesn't exist
     */
    public void submitChangeRequest(
        User user,
        BudgetItem item,
        double newValue)
        throws IllegalArgumentException {
        // Σημείωση να έχεις κάνει πρώτα τον User authorize
        // checkCanUserSubmitRequest()
        validateBudgetExists(item.getYear());

        Optional<BudgetItem> existingItemOpt = budgetRepository
                                 .findItemById(item.getId(), item.getYear());

         if (existingItemOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "Item requested for change doesn't exist");
        }
        BudgetItem existingItem = existingItemOpt.get();
        BudgetItem updatedItem = createUpdatedItem(existingItem, newValue);
        validateBudgetItemChange(existingItem, updatedItem);
        PendingChange change = createPendingChange(
            item, user, existingItem.getValue(), newValue
        );
        validatePendingChange(change);
        changeRequestRepository.save(change);
    }

    /**
     * Approves a change request.
     * @param pm the prime minister approving the request
     * @param change the change request to approve
     * @throws IllegalArgumentException if the change is null
     */
    public void approveRequest(PrimeMinister pm, PendingChange change)
    throws IllegalArgumentException {
        if (change == null) {
            throw new IllegalArgumentException(
                Message.REQUEST_DOES_NOT_EXIST_MESSAGE
            );
        }
        updateChangeStatus(pm, change, Status.APPROVED);
    }
    /**
     * Rejects a change request.
     * @param pm the prime minister rejecting the request
     * @param change the change request to reject
     * @throws IllegalArgumentException if the change is null
     */
    public void rejectRequest(PrimeMinister pm, PendingChange change)
    throws IllegalArgumentException {
        if (change == null) {
            throw new IllegalArgumentException(
                Message.REQUEST_DOES_NOT_EXIST_MESSAGE
            );
        }
        updateChangeStatus(pm, change, Status.REJECTED);
    }
    /**
     * Validates a pending change request for data integrity.
     * Checks that all required fields are present and have valid values.
     *
     * @param change the pending change to validate
     * @throws IllegalArgumentException if any validation rule is violated
     */
    private void validatePendingChange(PendingChange change)
    throws IllegalArgumentException {
        if (change.getBudgetItemYear() < Limits.MIN_BUDGET_YEAR) {
            throw new IllegalArgumentException(
                "PendingChange has invalid year");
        }
        if (!budgetRepository.existsByName(
                    change.getBudgetItemName(), change.getBudgetItemYear())) {
            throw new IllegalArgumentException(
                "PendingChange has invalid BudgetItem name");
        }
        if (change.getRequestByName() == null) {
            throw new IllegalArgumentException(
                "PendingChange has invalid requestor name");
        }
        if (change.getRequestById() == null) {
            throw new IllegalArgumentException(
                "PendingChange has invalid requestor ID");
        }
        if (change.getNewValue() <= Limits.SMALL_NUMBER) {
            throw new IllegalArgumentException(
                "PendingChange has invalid new value");
        }
    }
    /**
     * Updates the status of a change request.
     * If approved, applies the change to the budget item.
     * If rejected, simply updates the request status.
     *
     * @param pm the prime minister processing the request
     * @param change the change request to update
     * @param newStatus the new status to set (APPROVED or REJECTED)
     */
    public void updateChangeStatus(
        PrimeMinister pm,
        PendingChange change,
        Status newStatus) {
        validatePrimeMinister(pm);
        validateRequestStatus(change);

        Budget budget = findBudget(change.getBudgetItemYear());

        if (newStatus == Status.APPROVED) {
            processApprovedChange(change, budget, pm);
        } else if (newStatus == Status.REJECTED) {
            change.reject();
        }

        changeRequestRepository.save(change);
    }

    /**
     * Validates that a change request is in PENDING status.
     * @param change the change request to validate
     * @throws IllegalStateException if the request is not in PENDING status
     */
    private void validateRequestStatus(PendingChange change) {
        if (change.getStatus() != Status.PENDING) {
            throw new IllegalStateException(
                "Request is not in PENDING status. Current status: "
                + change.getStatus()
            );
        }
    }

    /**
     * Validates that the prime minister can approve requests.
     * @param pm the prime minister to validate
     * @throws IllegalArgumentException if the prime minister is null
     *                                                      or cannot approve
     */
    private void validatePrimeMinister(PrimeMinister pm)
    throws IllegalArgumentException {
        if (pm == null || !pm.canApprove()) {
            throw new IllegalArgumentException(
                "Prime Minister cannot approve requests."
            );
        }
    }

    /**
     * Finds and retrieves a budget for the given year.
     * @param year the year of the budget to find
     * @return the Budget for the specified year
     * @throws IllegalStateException if the budget is not found
     */
    private Budget findBudget(int year)
    throws IllegalStateException {
        return budgetRepository.findById(year)
            .orElseThrow(() -> new IllegalStateException(
                "Budget not found for year " + year
            ));
    }

    /**
     * Processes an approved change request by applying it to the budget.
     * Updates the budget item value and creates a change log entry.
     *
     * @param change the approved change request
     * @param budget the budget containing the item to be updated
     * @param pm the prime minister who approved the change
     * @throws IllegalArgumentException if the budget item doesn't exist
     */
    private void processApprovedChange(
        PendingChange change,
        Budget budget,
        PrimeMinister pm)
        throws IllegalArgumentException {
        Optional<BudgetItem> existingItem = findBudgetItem(
            budget,
            change.getBudgetItemId()
        );

        if (existingItem.isEmpty()) {
            throw new IllegalArgumentException(
                "Change doesn't affect existing BudgetItem");
        }

        // Store old value for potential rollback
        double oldValue = existingItem.get().getValue();

        try {
            updateExistingBudgetItem(
                existingItem.get(), change.getNewValue(), budget);
            budgetRepository.save(budget);
            change.approve();
            createChangeLog(change, pm);
        } catch (Exception e) {
            // Rollback: restore old value and recalculate totals
            existingItem.get().setValue(oldValue);
            budgetService.recalculateBudgetTotals(budget);
            throw new IllegalStateException(
                "Failed to process approved change: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the value of an existing budget item.
     * @param item the budget item to update
     * @param newValue the new value to set
     * @param budget the budget to recalculate totals for
     */
    private void updateExistingBudgetItem(
        BudgetItem item, double newValue, Budget budget) {
        item.setValue(newValue);
        budgetService.recalculateBudgetTotals(budget);
    }

    /**
     * Finds a budget item by its ID within a budget.
     * @param budget the budget to search in
     * @param itemId the ID of the budget item to find
     * @return an Optional containing the BudgetItem if found, otherwise empty
     */
    private Optional<BudgetItem> findBudgetItem(Budget budget, int itemId) {
        return budget.getItems().stream()
            .filter(item -> item.getId() == itemId)
            .findFirst();
    }

    /**
     * Creates and saves a change log entry for an approved change request.
     * @param request the approved change request
     * @param pm the prime minister who approved the change
     */
    private void createChangeLog(PendingChange request, PrimeMinister pm) {
        int newLogId = changeLogRepository.generateId();
        ChangeLog logEntry = new ChangeLog(
            newLogId,
            request.getBudgetItemId(),
            request.getOldValue(),
            request.getNewValue(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            pm.getFullName(),
            pm.getId()
        );
        changeLogRepository.save(logEntry);
    }
}
