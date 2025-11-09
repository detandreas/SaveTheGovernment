package budget.service;

import java.time.LocalDateTime;
import java.util.Scanner;

import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.User;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.GovernmentMember;
import budget.model.enums.Status;
import budget.repository.ChangeRequestRepository;
import budget.repository.BudgetItemRepository;
import budget.constants.Menu;
import budget.constants.Message;
import budget.constants.Limits;
import budget.repository.ChangeLogRepository;
/**
 * Service class to handle change requests for budget items.
 * 
 * Provides functionality for submitting change requests by Government Members
 * and approving/rejecting these requests by the Prime Minister.
 */

public class ChangeRequestService {
    private final ChangeRequestRepository changeRequestRepository;
    private final BudgetService budgetService;
    private final BudgetItemRepository budgetRepository;
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
        BudgetItemRepository budgetRepository,
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
        if (changeRequestRepository.countPendingRequestsByUser
        (user.getId()) >= Limits.MAX_PENDING_REQUESTS_PER_USER) {
            throw new IllegalStateException(Message.MAX_PENDING_REQUESTS_MESSAGE);
        }
        if (item.isDeletionProhibited()) {
            throw new IllegalStateException(Message.DELETE_NOT_ALLOWED_MESSAGE);
        }
        if (newValue == existingItem.getValue()) {
            throw new IllegalArgumentException(Message.NO_AMOUNT_CHANGE_MESSAGE);
        }
        System.out.println(Menu.CONFIRMATION_SUBMENU);
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();
        switch(choice) {
            case "1":
                changeRequestRepository.addChangeRequest(
                    new PendingChange(
                        existingItem.getId(),
                        user.getFullName(),
                        user.getId(),
                        existingItem.getValue(),
                        newValue
                    )
                );
                System.out.println(Message.CHANGE_REQUEST_SUBMITTED);
                break;
            case "2":
                System.out.println(Message.OPERATION_CANCELLED);
                break;
            default:
                System.out.println(Message.INVALID_OPTION);
                break;
        }
    }
    /**
     * Handles a change request (approve/reject) by the Prime Minister.
     *
     * @param pm the Prime Minister handling the request
     * @param requestId the ID of the change request
     * @param approve true to approve, false to reject
     * @throws IllegalArgumentException if the request does not exist
     * @throws IllegalStateException if the request is already resolved
     * @throws SecurityException if the user is not authorized
     */
    public void handleChangeRequest(PrimeMinister pm, int requestId, boolean approve) {
        if (pm == null || !pm.canApprove()) {
            throw new SecurityException(Message.REQUEST_AUTHORITY_MESSAGE);
        }
        PendingChange request = changeRequestRepository.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException(Message.REQUEST_DOES_NOT_EXIST_MESSAGE);
        }
        if (request.getStatus() != Status.PENDING) {
            throw new IllegalStateException(Message.REQUEST_ALREADY_RESOLVED_MESSAGE);
        }

        System.out.println(String.format(Menu.REQUEST_DECISION_SUBMENU.get(0), requestId));
        for (int i = 1; i < Menu.REQUEST_DECISION_SUBMENU.size(); i++) {
            System.out.println(Menu.REQUEST_DECISION_SUBMENU.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": // Approve
                BudgetItem item = budgetRepository.findById(request.getBudgetItemId());
                if (item != null) {
                    item.setValue(request.getNewValue());
                    budgetRepository.save();
                }
                request.approve();
                changeRequestRepository.updateChangeStatus(requestId, Status.APPROVED);
                if (item != null) {
                    changeLogRepository.addLog(pm.getId(), pm.getFullName(), item.getId(),
                            request.getOldValue(), request.getNewValue(), LocalDateTime.now());
                } else {
                    changeLogRepository.addLog(pm.getId(), pm.getFullName(), request.getBudgetItemId(),
                            request.getOldValue(), request.getNewValue(), LocalDateTime.now());
                }
                System.out.println();
                break;
            case "2": // Reject
                request.reject();
                changeRequestRepository.updateChangeStatus(requestId, Status.REJECTED);
                System.out.println();
                break;
            case "3": // Return to Main Menu
                System.out.println(Message.RETURNING_TO_MAIN_MENU);
                break;
            default:
                System.out.println(Message.INVALID_OPTION);
                break;
        }
    }
}
