package budget.service;

import java.time.LocalDateTime;
import java.util.UUID;

import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.domain.user.PrimeMinister;
import budget.model.enums.Status;
import budget.repository.ChangeRequestRepository;
import budget.repository.BudgetRepository;
import budget.repository.ChangeLogRepository;
import budget.constants.Menu;
import budget.constants.Message;
import budget.constants.Limits;

/**
 * Service class to handle change requests for budget items.
 * 
 * Provides functionality for submitting change requests by Government Members
 * and approving/rejecting these requests by the Prime Minister.
 */

public class ChangeRequestService {
    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;
    private final ChangeLogRepository changeLogRepository;
    /**
     * Constructor to initialize repositories and services.
     *
     * @param changeRequestRepository repository for change requests
     * @param budgetService service for budget item operations
     * @param budgetRepository repository for budget items
     * @param changeLogRepository repository for logging changes
     */
    public ChangeRequestService(
        ChangeRequestRepository changeRequestRepository,
        BudgetService budgetService,
        BudgetRepository budgetRepository,
        ChangeLogRepository changeLogRepository
    ) {
        this.changeRequestRepository = changeRequestRepository;
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
        this.changeLogRepository = changeLogRepository;
    }
    /**
     * Submits a change request from a GovernmentMember for a budget item.
     *
     * @param user the user submitting the request
     * @param item the budget item to be changed
     * @param newValue the proposed new value
     * @throws IllegalArgumentException if the user is not authorized or if the item does not exist
     * @throws IllegalStateException if the user has too many pending requests or if deletion is prohibited
     * @throws IllegalArgumentException if the new value is unchanged
     */
    public void submitChangeRequest(User user, BudgetItem item, double newValue) {
        if (!user.canEdit(item)) {
            throw new IllegalArgumentException(Message.CHANGE_REQUEST_PERMISSION);
        }

        BudgetItem existingItem = budgetService.findItemById(item.getId());
        
        if (existingItem == null) {
            existingItem = budgetService.createNewBudgetItem(item);
            System.out.println(Message.BUDGET_ITEM_CREATION_SUCCESS);
        }
        if (newValue < 0) {
            throw new IllegalArgumentException(Message.NON_NEGATIVE_AMOUNT_ERROR);
        }
        if (countPendingRequestsByUser(user.getId()) >= Limits.MAX_PENDING_REQUESTS_PER_USER) {
            throw new IllegalStateException(Message.MAX_PENDING_REQUESTS_MESSAGE);
        }
        if (item.isDeletionProhibited()) {
            throw new IllegalStateException(Message.DELETE_NOT_ALLOWED_MESSAGE);
        }
        if (newValue == existingItem.getValue()) {
            throw new IllegalArgumentException(Message.NO_AMOUNT_CHANGE_MESSAGE);
        }

        PendingChange changeRequest = new PendingChange(
            existingItem.getId(),
            user.getFullName(),
            user.getId(),
            existingItem.getValue(),
            newValue
        );
        changeRequestRepository.save(changeRequest);
    }
    /**
     * Approves a pending change request by the Prime Minister.
     *
     * @param pm the Prime Minister approving the request
     * @param request the pending change request to approve
     * @throws SecurityException if the user is not authorized to approve
     * @throws IllegalArgumentException if the request does not exist
     * @throws IllegalStateException if the request has already been resolved
     */
    public void approveRequest(PrimeMinister pm, PendingChange request) {
        if (pm == null || !pm.canApprove()) {
            throw new SecurityException(Message.REQUEST_AUTHORITY_MESSAGE);
        }
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        if (request.getStatus() != Status.PENDING) {
            throw new IllegalStateException(Message.REQUEST_ALREADY_RESOLVED_MESSAGE);
        }

        BudgetItem item = budgetRepository.findById(request.getBudgetItemId());

        if (item != null) {
            item.setValue(request.getNewValue());
            budgetRepository.save();
        }

        request.approve();
        changeRequestRepository.save(request);

        changeLogRepository.insert(
            pm.getId(),
            pm.getFullName(),
            request.getBudgetItemId(),
            request.getOldValue(),
            request.getNewValue(),
            LocalDateTime.now()
        );
    }
    /**
     * Rejects a pending change request by the Prime Minister.
     *
     * @param pm the Prime Minister rejecting the request
     * @param request the pending change request to reject
     * @throws SecurityException if the user is not authorized to reject
     * @throws IllegalArgumentException if the request does not exist
     * @throws IllegalStateException if the request has already been resolved
     */
    public void rejectRequest(PrimeMinister pm, PendingChange request) {
        if (pm == null || !pm.canApprove()) {
            throw new SecurityException(Message.REQUEST_AUTHORITY_MESSAGE);
        }
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        if (request.getStatus() != Status.PENDING) {
            throw new IllegalStateException(Message.REQUEST_ALREADY_RESOLVED_MESSAGE);
        }

        request.reject();
        changeRequestRepository.save(request);
    }
    /**
     * Counts the number of pending change requests submitted by a specific user.
     *
     * @param userId the ID of the user
     * @return the count of pending requests
     */
    private long countPendingRequestsByUser(UUID userId) {
        if (changeRequestRepository == null) {
            throw new IllegalStateException("ChangeRequestRepository not initialized");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        Iterable<PendingChange> all = changeRequestRepository.load();
        if (all == null) {
            throw new IllegalStateException("ChangeRequestRepository.load() returned null");
        }
        return java.util.stream.StreamSupport.stream(all.spliterator(), false)
                .filter(change -> change != null
                        && userId.equals(change.getRequestById())
                        && change.getStatus() == Status.PENDING)
                .count();
    }
}