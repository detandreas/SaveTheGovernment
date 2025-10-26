package budget.model.user;

import budget.model.BudgetItem;
import budget.model.UserRole;
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
    public Boolean canEdit(BudgetItem budgetItem) {
        return false;
    }
    /**
     * Checks if citizen can approve a proposed change in the budget.
     * @return false for citizen
     */
    @Override
    public Boolean canApprove() {
        return false;
    }
}
