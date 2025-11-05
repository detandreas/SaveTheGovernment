package budget.model.domain.user;
import budget.model.domain.PendingChange;
import budget.model.enums.UserRole;
import budget.model.domain.BudgetItem;

/**
 * Represents the Prime Minister user.
 * The Prime Minister can edit and approve all budget items
 * and pending changes in the system.
 */
public class PrimeMinister extends User {
    
     /**
     * Creates a new Prime Minister.
     * This constructor is private because only one Prime Minister
     * instance can exist (singleton pattern).
     * @param id the unique ID of the Prime Minister
     * @param userName the username of the Prime Minister
     * @param fullName the full name of the Prime Minister
     * @param password the password for login
     */
    private static PrimeMinister instance;

    private PrimeMinister(int id, String userName, String fullName, String password) {
        super(userName, fullName, password, UserRole.PRIME_MINISTER);
    }

     /**
     * Returns the single instance of the Prime Minister.
     * If it doesn't exist, a new one is created.
     * @param id the unique ID of the Prime Minister
     * @param userName the username of the Prime Minister
     * @param password the password for login
     * @param fullName the full name of the Prime Minister
     * @return the single Prime Minister instance
     */
    public static synchronized PrimeMinister getInstance(int id, String userName, String password, String fullName){
        if (instance == null) {
            instance = new PrimeMinister(id, userName, fullName, password);
        }
        return instance;
    }
    
     /**
     * Checks if the Prime Minister can edit the given budget item.
     *
     * @param budgetItem the budget item to check
     * @return {@code true}, because the Prime Minister can edit all items
     */
    @Override
    public boolean canEdit(BudgetItem budgetItem) {
        return true;  // Prime Minister can edit all items
    }

     /**
     * Checks if the Prime Minister can approve changes.
     *
     * @return {@code true}, because the Prime Minister can approve all changes
     */
    @Override
    public boolean canApprove() {
        return true;  // Prime Minister can approve changes
    }

     /**
     * Approves a pending change.
     *
     * @param change the pending change to approve
     */
    public void approvePendingChange(PendingChange change) {
        change.approve();
    }

     /**
     * Rejects a pending change.
     *
     * @param change the pending change to reject
     */
    public void rejectPendingChange(PendingChange change) {
        change.reject();
    }
}

