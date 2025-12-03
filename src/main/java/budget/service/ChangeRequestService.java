package budget.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.ChangeLog;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.model.enums.Status;
import budget.repository.BudgetRepository;
import budget.repository.ChangeLogRepository;
import budget.repository.ChangeRequestRepository;
import budget.repository.UserRepository;
import budget.constants.Limits;
import budget.constants.Message;
import budget.exceptions.ValidationException;

/**
 * Service for handling change requests to budget items.
 * Responsible for submitting, approving, and rejecting change requests.
 * Delegates validation to BudgetValidationService.
 */
public class ChangeRequestService {

    private static final Logger LOGGER = Logger.getLogger(ChangeRequestService.class.getName());

    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetService budgetService;
    private final BudgetValidationService budgetValidationService;
    private final BudgetRepository budgetRepository;
    private final ChangeLogRepository changeLogRepository;
    private final UserRepository userRepository;
    @SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP"},
        justification = "Injected dependencies are final and expected to be stateless singletons (DI pattern)."
    )
    /**
     * Constructor for ChangeRequestService.
     * @param changeRequestRepository repository for change requests
     * @param budgetValidationService service for validating budget changes
     * @param budgetRepository repository for budgets
     * @param changeLogRepository repository for change logs
     * @param budgetService service for budget operations
     * @param userRepository repository for users
     */
    public ChangeRequestService(
        ChangeRequestRepository changeRequestRepository,
        BudgetValidationService budgetValidationService,
        BudgetRepository budgetRepository,
        ChangeLogRepository changeLogRepository,
        BudgetService budgetService,
        UserRepository userRepository
    ) {
        this.changeRequestRepository = changeRequestRepository;
        this.budgetValidationService = budgetValidationService;
        this.budgetRepository = budgetRepository;
        this.changeLogRepository = changeLogRepository;
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }
    /**
     * Submits a change request for a budget item.
     * Validates the request and saves it if valid.
     *
     * @param user the user submitting the request
     * @param item the budget item to be changed
     * @param newValue the proposed new value for the budget item
     * @throws IllegalArgumentException if validation fails
     */
    public void submitChangeRequest(User user, BudgetItem item, double newValue) {

        Ministry userMinistry = null;
        if (user instanceof GovernmentMember) {
            userMinistry = ((GovernmentMember) user).getMinistry();
        } else {
             throw new IllegalArgumentException("User must be a Government Member.");
        }
        
        if (userMinistry == null) {
            throw new IllegalArgumentException("User does not belong to any ministry.");
        }

        if (!budgetRepository.existsById(item.getYear())) {
             throw new IllegalArgumentException("Budget for year " + item.getYear() + " does not exist.");
        }

        BudgetItem existingItem = budgetRepository.findItemById(item.getId(), item.getYear())
                                                  .orElse(null);
        
        String nameToStoreInRequest = item.getName();
        BudgetItem itemForValidation; 

        if (existingItem != null) {
            // ΥΠΑΡΧΕΙ ΗΔΗ
            if (!user.canEdit(existingItem)) {
                throw new IllegalArgumentException(Message.CHANGE_REQUEST_PERMISSION);
            }
            itemForValidation = existingItem;
        } else {
            // ΝΕΟ: (Δεν υπάρχει στο budget)
            List<Ministry> mockMinistries = new ArrayList<>();
            mockMinistries.add(userMinistry);
            
            itemForValidation = new BudgetItem(
                item.getId(),
                item.getYear(),
                item.getName(),
                0.0, 
                item.getIsRevenue(), 
                mockMinistries
            );
        }
        // CHECK PENDING REQUEST LIMIT
        if (countPendingRequestsByUser(user.getId()) >= Limits.MAX_PENDING_REQUESTS_PER_USER) {
            throw new IllegalStateException(Message.MAX_PENDING_REQUESTS_MESSAGE);
        }

        // VALIDATION SERVICE (Εδώ γίνεται ο κεντρικός έλεγχος)
        BudgetItem itemForValidationUpdated = new BudgetItem(
            itemForValidation.getId(),
            itemForValidation.getYear(),
            itemForValidation.getName(),
            newValue, itemForValidation.getIsRevenue(),
            itemForValidation.getMinistries()
        );

        try {
            budgetValidationService.validateBudgetItemUpdate(
                itemForValidation,
                itemForValidationUpdated
            );
        } catch (ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Δημιουργία Request
        PendingChange changeRequest = new PendingChange(
            item.getId(),
            item.getYear(),
            nameToStoreInRequest,
            user.getFullName(),
            user.getId(),
            itemForValidation.getValue(), 
            newValue
        );
        
        changeRequestRepository.save(changeRequest);
    }
    /**
     * Approves a change request.
     * @param pm the prime minister approving the request
     * @param request the change request to approve
     */
    public void approveRequest(PrimeMinister pm, PendingChange request) {
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        updateChangeStatus(pm, request.getId(), Status.APPROVED);
    }
    /**
     * Rejects a change request.
     * @param pm the prime minister rejecting the request
     * @param request the change request to reject
     */
    public void rejectRequest(PrimeMinister pm, PendingChange request) {
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        updateChangeStatus(pm, request.getId(), Status.REJECTED);
    }
    /**
     * Validates the change request before processing.
     */
    private Optional<PendingChange> validateRequest(int id, Status status) {
        if (id <= 0) return Optional.empty();
        Optional<PendingChange> requestOpt = changeRequestRepository.findById(id);
        if (requestOpt.isEmpty()) return Optional.empty();
        
        PendingChange request = requestOpt.get();
        if (request.getStatus() != Status.PENDING) return Optional.empty();
        
        return Optional.of(request);
    }
    /**
     * Updates the status of a change request.
     * If approved, applies the change to the budget item.
     * If rejected, simply updates the request status.
     *
     * @param pm the prime minister processing the request
     * @param id the ID of the change request
     * @param status the new status (APPROVED or REJECTED)
     */
    public void updateChangeStatus(PrimeMinister pm, int id, Status status) {
        if (pm == null || !pm.canApprove()) return;

        Optional<PendingChange> requestOpt = validateRequest(id, status);
        if (requestOpt.isEmpty()) return;
        PendingChange request = requestOpt.get();

        Budget budget = budgetRepository.findById(request.getBudgetItemYear())
                .orElseThrow(() -> new IllegalStateException("Budget not found for year " + request.getBudgetItemYear()));

        if (status == Status.APPROVED) {
            
            Optional<BudgetItem> itemOpt = budget.getItems().stream()
                .filter(i -> i.getId() == request.getBudgetItemId())
                .findFirst();

            if (itemOpt.isPresent()) {
                // ΥΠΑΡΧΕΙ: Update
                BudgetItem item = itemOpt.get();
                item.setValue(request.getNewValue());
            } else {
                // ΔΕΝ ΥΠΑΡΧΕΙ: Create Now (Ανάθεση στο Υπουργείο του αιτούντος)
                String realName = request.getBudgetItemName();
                List<Ministry> ministries = new ArrayList<>();

                // Βρίσκουμε τον αιτούντα και παίρνουμε το Υπουργείο του (fallback)
                User submitter = userRepository.findById(request.getRequestById()).orElse(null);
                if (submitter instanceof GovernmentMember) {
                    ministries.add(((GovernmentMember) submitter).getMinistry());
                }

                if (!ministries.isEmpty()) {
                    BudgetItem newItem = new BudgetItem(
                        request.getBudgetItemId(),
                        request.getBudgetItemYear(),
                        realName, // Καθαρό όνομα
                        request.getNewValue(),
                        false, // Default Expense
                        ministries
                    );
                    
                    if (budget.getItems() == null) { /* init check */ }
                    budget.getItems().add(newItem);
                } else {
                     LOGGER.severe("CRITICAL: Submitter ministry missing. Cannot create item.");
                     return;
                }
            }

            // ΜΟΝΟ ΤΩΡΑ ΣΩΖΟΥΜΕ ΣΤΟ BUDGET REPO
            budgetRepository.save(budget);
            
            request.approve();
            
            int newLogId = changeLogRepository.generateId();
            ChangeLog logEntry = new ChangeLog(
                newLogId,
                request.getBudgetItemId(),
                request.getOldValue(),
                request.getNewValue(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                pm.getFullName(),
                pm.getId()
            );
            changeLogRepository.save(logEntry);

        } else if (status == Status.REJECTED) {
            request.reject();
        }

        changeRequestRepository.save(request);
    }
    /**
     * Counts the number of pending requests submitted by a user.
     * @param userId the ID of the user
     * @return the count of pending requests
     */
    private long countPendingRequestsByUser(UUID userId) {
        Iterable<PendingChange> all = changeRequestRepository.load();
        if (all == null) return 0;
        return java.util.stream.StreamSupport.stream(all.spliterator(), false)
                .filter(c -> c != null && userId.equals(c.getRequestById()) && c.getStatus() == Status.PENDING)
                .count();
    }
}

