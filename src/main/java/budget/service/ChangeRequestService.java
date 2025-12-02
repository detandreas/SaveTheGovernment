package budget.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.enums.Ministry;
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

    private static final Logger LOGGER = Logger.getLogger(ChangeRequestService.class.getName());

    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetService budgetService;
    private final BudgetValidationService budgetValidationService;
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
        BudgetValidationService budgetValidationService,
        BudgetRepository budgetRepository,
        ChangeLogRepository changeLogRepository
    ) {
        this.changeRequestRepository = changeRequestRepository;
        this.budgetValidationService = budgetValidationService;
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
        if (budgetValidationService.validateBudgetItemDeletion(item, budgetRepository.load())) {
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

        updateChangeStatus(pm, request.getId(), Status.APPROVED);
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

        updateChangeStatus(pm,request.getId(), Status.REJECTED);

    }
    /**
     * Updates the status of a change request.
     *
     * @param pm the Prime Minister performing the update
     * @param id the ID of the change request
     * @param status the new status to set
     */
    public void updateChangeStatus(PrimeMinister pm, int id, Status status) {
        Optional<PendingChange> requestOpt = validateRequest(id, status);
        if (requestOpt.isEmpty()) {
            return;
        }

        PendingChange request = requestOpt.get();

        if (status == Status.APPROVED) {
            if (status == Status.APPROVED) {
            List<Budget> allBudgets = budgetRepository.load();
            boolean itemFound = false;

            // Ενημέρωση υπάρχοντος BudgetItem
            for (Budget budget : allBudgets) {
                Optional<BudgetItem> itemOpt = budget.getItems().stream()
                    .filter(i -> i.getId() == request.getBudgetItemId())
                    .findFirst();

                if (itemOpt.isPresent()) {
                    BudgetItem item = itemOpt.get();
                    item.setValue(request.getNewValue());
                    budgetRepository.save(budget);
                    itemFound = true;
                    break;
                }
            }

            // Αν δεν υπάρχει, δημιουργία νέου
            if (!itemFound) {
                LOGGER.info("Item not found. Creating auto-generated BudgetItem.");

                int currentYear = java.time.LocalDate.now().getYear();

                User submitter = userRepository.findById(request.getRequestById()).orElse(null);
                List<Ministry> assignedMinistries = new ArrayList<>();

                if (submitter != null) {
                     GovernmentMember member = (GovernmentMember) submitter;
                     // το κονδύλι που θα δημιουργηθεί θα ανατεθεί στο υπουργείο
                     // αυτού που υπέβαλε το αίτημα
                     if (member.getMinistry() != null) {
                         assignedMinistries.add(member.getMinistry());
                     } else {
                         LOGGER.warning("Member has no ministry. Cannot auto-create item.");
                         return;
                     }
                } else {
                     LOGGER.warning("Submitter user not found. Cannot auto-create item.");
                     return;
                }

                Budget targetBudget = budgetRepository.findById(currentYear)
                        .orElseGet(() -> new Budget(new ArrayList<>(), currentYear, 0.0, 0.0, 0.0));

                BudgetItem newItem = new BudgetItem(
                    request.getBudgetItemId(),
                    currentYear,
                    !!!!request.getBudgetItem(),
                    request.getNewValue(),
                    false, 
                    assignedMinistries // Η λίστα περιέχει μόνο το 1 υπουργείο του χρήστη
                );

                if (targetBudget.getItems() == null) {
                    targetBudget.setItems(new ArrayList<>());
                }
                
                targetBudget.getItems().add(newItem);
                budgetRepository.save(targetBudget);
            }

            request.approve();

            // Καταγραφή στο Log ΜΟΝΟ αν εγκριθεί
            if (pm != null) {
                changeLogRepository.insert(
                        pm.getId(),
                        pm.getFullName(),
                        request.getBudgetItemId(),
                        request.getOldValue(),
                        request.getNewValue(),
                        LocalDateTime.now()
                );
            }
        } else if (status == Status.REJECTED) {
            request.reject();
        } else {
            LOGGER.warning("Unsupported status: " + status);
            return;
        }

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

    /**
     * Validates the request ID and status, and ensures the request exists and is pending.
     * Logs warnings if validation fails.
     *
     * @param id the ID of the request
     * @param status the target status
     * @return Optional containing the request if valid, or empty if invalid
     */
    private Optional<PendingChange> validateRequest(int id, Status status) {
        if (id <= 0) {
            LOGGER.warning("Cannot update change with invalid id: " + id);
            return Optional.empty();
        }
        if (status == null) {
            LOGGER.warning("Cannot update change with null status for id: " + id);
            return Optional.empty();
        }
        Optional<PendingChange> requestOpt = changeRequestRepository.findById(id);

        if (requestOpt.isEmpty()) {
            LOGGER.warning("No pending change found with id: " + id);
            return Optional.empty();
        }

        PendingChange request = requestOpt.get();

        if (request.getStatus() != Status.PENDING) {
            LOGGER.warning("Change request already resolved: id=" + id);
            return Optional.empty();
        }

        return Optional.of(request);
    }
}