package budget.service;

import budget.model.domain.BudgetItem;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.exceptions.UserNotAuthorizedException;
/**
 * Service class to handle user authorization for budget item edits.
 */
public class UserAuthorizationService {
    /**
     * Checks if a user is authorized to submit
     * change requests for a budget item.
     *
     * @param user the user attempting to submit the request
     * @param item the budget item in question
     * @throws IllegalArgumentException if the user or budget item is null
     * @throws IllegalArgumentException if the budget item
     *                                  has no associated ministries
     * @throws UserNotAuthorizedException if the user is not a Government Member
     * @throws UserNotAuthorizedException if the user's ministry
     *                                    is not authorized for the budget item
     */
    public void checkCanSubmitRequest(User user, BudgetItem item) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Budget item cannot be null.");
        }
        if (item.getMinistries() == null || item.getMinistries().isEmpty()) {
            throw new IllegalArgumentException(
                "Budget item must be associated with at least one ministry."
            );
        }

        if (!(user instanceof GovernmentMember gm)) {
            throw new UserNotAuthorizedException(
                "Only government members can submit change requests."
            );
        }

        if (!item.getMinistries().contains(gm.getMinistry())) {
            throw new UserNotAuthorizedException(
                "User's ministry (" + gm.getMinistry()
                + ") is not authorized to submit change requests "
                + "for this budget item. Allowed ministries: "
                + item.getMinistries()
            );
        }
    }
    /**
     * Checks if a user is authorized to approve change requests.
     *
     * @param user the user attempting to approve requests
     * @throws IllegalArgumentException if the user is null
     * @throws UserNotAuthorizedException if the user is not the Prime Minister
     */
    public void checkCanApproveRequests(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!(user instanceof PrimeMinister)) {
            throw new UserNotAuthorizedException(
                "Only the Prime Minister can approve change requests."
            );
        }

    }
    /**
     * Checks if a government member from the Finance Ministry
     * can directly edit a budget item.
     *
     * @param user the user attempting to edit the budget item
     * @param item the budget item in question
     * @throws IllegalArgumentException if the user is null
     * @throws IllegalArgumentException if the budget item is null
     * @throws UserNotAuthorizedException if the user is not a Government Member
     * @throws UserNotAuthorizedException if the user's ministry is not Finance
     */
    public void checkCanUserEditBudgetItem(User user, BudgetItem item) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Budget item cannot be null.");
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

    }
}
