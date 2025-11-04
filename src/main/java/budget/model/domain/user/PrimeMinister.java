package budget.model.domain.user;

import budget.model.domain.PendingChange;
import budget.model.enums.UserRole;
import budget.model.domain.BudgetItem;

public class PrimeMinister extends User {
    
    private static PrimeMinister instance;

    private PrimeMinister(int id, String userName, String fullName, String password) {
        super(userName, fullName, password, UserRole.PRIME_MINISTER);
    }

    public static PrimeMinister getInstance(int id, String userName, String password, String fullName){
        if (instance == null) {
            instance = new PrimeMinister(id, userName, fullName, password);
        }
        return instance;
    }

    @Override
    public boolean canEdit(BudgetItem budgetItem) {
        return true;  // Prime Minister can edit all items
    }

    @Override
    public boolean canApprove() {
        return true;  // Prime Minister can approve changes
    }

    public void approvePendingChange(PendingChange change) {
        change.approve();
    }

    public void rejectPendingChange(PendingChange change) {
        change.reject();
    }
}

