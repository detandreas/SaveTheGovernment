package budget.model.domain.user;
package budget.model.user;

import budget.model.BudgetItem;
import budget.model.PendingChange;
import budget.model.Ministry;
import budget.model.UserRole;


/**
 * Represents a GovernmentMember user of the Budget system
 */
public class GovernmentMember extends User {
    private Ministry ministry;
    /**
     * @param username GovernmentMember name inside the system
     * @param fullName GovernmentMember full name
     * @param password GovernmentMember password 
     * @param ministry GovernmentMember ministry it belongs
     */
    public GovernmentMember(String username, String fullName, String password, Ministry ministry) {
        super(username, fullName, password, UserRole.GOVERNMENT_MEMBER);
        this.ministry = ministry;
    }

    /**
     * Returns the ministry
     * @return the ministry of the government member
     */
    public Ministry getMinistry() {
        return ministry;
    }
    /**
     * Sets the ministry of the government member
     */
    public void setMinistry(Ministry ministry) {
        this.ministry = ministry;
    }

    /**
     * Checks if GovernmentMember can Edit the Budget
     * @param item BudgetItem to check editing permissions for
     * @return {@code true} if this member can edit the given item,
     *         {@code false} otherwise
     */
    @Override
    public boolean canEdit(BudgetItem item) {
        return this.ministry == Ministry.FINANCE;
    }
    /**
     * Checks if this government member can approve budget changes.
     */
    @Override
    public boolean canApprove() {
        return false;
    }

    /**
     * Submits a change request for a given budget item.
     *
     * @param item the budget item to be updated
     * @param newAmount the proposed new value for the budget item
     * @return a PendingChange object representing the requested update
 */
    public PendingChange submitChangeRequest(BudgetItem item, double newAmount) {
        int userId = 0; /* placeholder for user id */
        return new PendingChange(
            item.getId(),
            getUserName(),
            userId,
            item.getValue(),
            newAmount
        );
    }
}
