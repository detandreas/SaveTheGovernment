package budget.service;

import java.time.LocalDateTime;

import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.GovernmentMember;
import budget.model.enums.Status;
import budget.repository.ChangeRequestRepository;
import budget.repository.BudgetItemRepository;
import budget.util.Constants.Limits;
import budget.util.Constants.Message;

public class ChangeRequestService {
    private final ChangeRequestRepository changeRequest;
    private final BudgetItemService budgetService;

    public ChangeRequestService(
        ChangeRequestRepository changeRequest,
        BudgetItemService budgetService
    ) {
        this.changeRequest = changeRequest;
        this.budgetService = budgetService;
    }

    public void submitChangeRequest(User user, BudgetItem item, double newValue) {
        BudgetItem existingItem = budgetService.findItemById(item.getId());
        if (!(user instanceof GovernmentMember)) {
            throw new IllegalArgumentException(Message.CHANGE_REQUEST_PERMISSION);
        }
        if (existingItem == null) {
            System.out.println(Message.BUDGET_ITEM_CREATION_SUCCESS);
            existingItem = budgetService.createNewBudgetItem(item);
        }
        if (newValue < 0) {
            throw new IllegalArgumentException(Message.NON_NEGATIVE_AMOUNT_ERROR);
        }
        if (changeRequest.countPendingRequestsByUser(user.getId()) >= Limits.MAX_PENDING_REQUESTS_PER_USER) {
            throw new IllegalStateException(Message.MAX_PENDING_REQUESTS_MESSAGE);
        }
        if (item.isDeletionProhibited()) {
            throw new IllegalStateException(Message.DELETE_NOT_ALLOWED_MESSAGE);
        }
        if (newValue == existingItem.getValue()) {
            throw new IllegalArgumentException(Message.NO_AMOUNT_CHANGE);
        }
        
        PendingChange change = new PendingChange(
            existingItem.getId(),
            user.getFullName(),
            user.getId(),
            existingItem.getValue(),
            newValue,
            Status.PENDING,
            LocalDateTime.now()
        );
        changeRequest.addChangeRequest(change);
        System.out.println(Message.CHANGE_REQUEST_SUBMITTED);
    }
    public class ApproveRequestService {
    /**
     * Service for approving change requests by the Prime Minister.
     */
    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetRepository budgetRepository;
    private final ChangeLogRepository changeLogRepository;
    /**
     * Constructs an ApproveRequestService with the specified repositories.
     *
     * @param changeRequestRepository repository for managing change requests
     * @param budgetRepository repository for managing budget items
     * @param changeLogRepository repository for logging approved changes
     */
    public ApproveRequestService(
        ChangeRequestRepository changeRequestRepository,
        BudgetRepository budgetRepository,
        ChangeLogRepository changeLogRepository
    ) {
        this.changeRequestRepository = changeRequestRepository;
        this.budgetRepository = budgetRepository;
        this.changeLogRepository = changeLogRepository;
    }
    /**
     * Approves a change request with the given ID by the specified Prime Minister.
     *
     * @param pm the Prime Minister approving the request
     * @param requestId the ID of the change request to approve
     * @throws IllegalArgumentException if the request does not exist
     * @throws IllegalStateException if the request is already resolved
     * @throws SecurityException if the user is not authorized to approve requests
     */
    public void approveRequest(PrimeMinister pm, int requestId) {
        PendingChange request = changeRequestRepository.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        if (request.getStatus() != Status.PENDING) {
            throw new IllegalStateException(Message.REQUEST_ALREADY_RESOLVED_MESSAGE);
        }
        if (pm == null) {
            throw new SecurityException(Message.REQUEST_AUTHORITY_MESSAGE);
        }
        BudgetItem item = budgetRepository.findById(request.getBudgetItemId());
        if (item != null) {
            item.setValue(request.getNewValue());
            budgetRepository.save();
        }
        request.approve();
        request.setSubmittedDate(LocalDateTime.now());
        changeRequestRepository.updateChangeStatus(requestId, Status.APPROVED);
        changeLogRepository.addLog(pm.getId(), pm.getFullName(), item.getId(),
                request.getOldValue(), request.getNewValue(), LocalDateTime.now());
    }





}



