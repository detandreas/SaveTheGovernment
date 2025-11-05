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

}



