package budget.backend.model.domain.user;

import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.UserRole;
/**
 * Represents a Citizen user of the budget system.
 */
public class Citizen extends User {
    /**
     * Constructor of Citizen class.
     * @param userName citizen name inside the budget system
     * @param fullName  citizen full name
     * @param password  citizen password
     */
    public Citizen(
    String userName,
    String fullName,
    String password
    ) {
        super(userName, fullName, password, UserRole.CITIZEN);
    }
    /**
     * Checks if citizen can edit a budget item.
     * @param budgetItem a item of the whole budget
     * @return false for citizen
     */
    @Override
    public boolean canEdit(BudgetItem budgetItem) {
        return false;
    }
    /**
     * Checks if citizen can approve a proposed change in the budget.
     * @return false for citizen
     */
    @Override
    public boolean canApprove() {
        return false;
    }
    /**
     * Checks if Citizen can sumbit a request
     * to change a budget item.
     * @return always false
     */
    @Override
    public boolean canSubmitChangeRequest() {
        return false;
    }
    /**
     * Returns a string representation of a Citizen.
     * @return a formatted string containing Citizen information
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
