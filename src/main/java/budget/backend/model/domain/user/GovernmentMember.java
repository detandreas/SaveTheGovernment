package budget.backend.model.domain.user;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.backend.model.enums.UserRole;

/**
 * Represents a GovernmentMember user of the Budget system.
 */
public class GovernmentMember extends User {
    private Ministry ministry;
    /**
     * @param username GovernmentMember name inside the system.
     * @param fullName GovernmentMember full name.
     * @param password GovernmentMember password.
     * @param ministry GovernmentMember ministry it belongs.
     */
    public GovernmentMember(
            String username,
            String fullName,
            String password,
            Ministry ministry) {
        super(username, fullName, password, UserRole.GOVERNMENT_MEMBER);
        this.ministry = ministry;
    }

    /**
     * Returns the ministry.
     * @return the ministry of the government member.
     */
    public Ministry getMinistry() {
        return ministry;
    }
    /**
     * Sets the ministry of the government member.
     * @param ministry Ministry to set for this government member.
     */
    public void setMinistry(Ministry ministry) {
        this.ministry = ministry;
    }

    /**
     * Checks if GovernmentMember can Edit the Budget.
     * @param item BudgetItem to check editing permissions for.
     * @return {@code true} if this member can edit the given item,
     *         {@code false} otherwise.
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
     * Checks if this GovernmentMember can submit a change request
     * for a budget item.
     */
    @Override
    public boolean canSubmitChangeRequest() {
        return true;
    }
    /**
     * Returns a string representation of a GovernmentMember.
     * @return a formatted string containing GovernmentMember information
     */
    @Override
    public String toString() {
        String userString = super.toString();
        return userString.substring(0, userString.length() - 1)
           + String.format(", ministry=%s}", ministry);
    }
}
