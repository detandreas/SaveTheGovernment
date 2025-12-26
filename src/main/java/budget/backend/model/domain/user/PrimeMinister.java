package budget.backend.model.domain.user;

import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.UserRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents the single Prime Minister user in the system.
 * Implemented as a thread-safe singleton.
 */
public final class PrimeMinister extends User {
    // visible accros multiple threads
    private static volatile PrimeMinister instance;

    /**
     * Creates a Prime Minister user with the provided credentials.
     * Constructor is private to enforce the singleton pattern.
     *
     * @param userName the username within the budget system
     * @param fullName the full name of the Prime Minister
     * @param password the password (already hashed by the service layer)
     */
    private PrimeMinister(String userName, String fullName, String password) {
        super(userName, fullName, password, UserRole.PRIME_MINISTER);
    }

    /**
     * Returns the singleton instance, creating it on the first call.
     * Subsequent calls return the existing instance.
     *
     * @param userName the username within the budget system
     * @param fullName the full name of the Prime Minister
     * @param password the password (already hashed by the service layer)
     * @return the Prime Minister instance
     */
    @SuppressFBWarnings(
      value = "MS_EXPOSE_REP",
      justification = "Singleton instance must be globally accessible."
    )
    public static PrimeMinister getInstance(
        String userName,
        String fullName,
        String password
    ) {
        //double checked locking pattern -> singleton with thread safety
        if (instance == null) {
            /*
            lock PrimeMinister instance so only one thread at a time
            can access next blocks
            */
            synchronized (PrimeMinister.class) {
                if (instance == null) {
                    instance = new PrimeMinister(userName, fullName, password);
                }
            }
        }
        return instance;
    }

    /**
     * Returns the existing singleton instance.
     *
     * @return the Prime Minister instance
     * @throws IllegalStateException if the instance has
     * not been initialized yet
     */
    @SuppressFBWarnings(
      value = "MS_EXPOSE_REP",
      justification = "Singleton instance must be globally accessible."
    )
    public static synchronized PrimeMinister getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "PrimeMinister instance is not initialized yet."
            );
        }
        return instance;
    }

    /**
     * Sets the singleton instance.
     * Suppressed warning justification: This is a session singleton intentionally 
     * storing the mutable logged-in user state.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_STATIC_REP2", justification = "Singleton instance is mutable by design")
    public static void setInstance(PrimeMinister pm) {
        instance = pm;
    }
    
    /**
     * Clears the singleton instance.
     * Used for testing purposes to reset the state.
     */
    public static void clearInstance() {
        instance = null;
    }

    /**
     * Indicates whether the Prime Minister can edit the provided budget item.
     *
     * @param budgetItem the budget item in question
     * @return {@code false} because the Prime Minister can't edit any item
     */
    @Override
    public boolean canEdit(BudgetItem budgetItem) {
        return false;
    }

    /**
     * Indicates whether the Prime Minister can approve budget changes.
     *
     * @return true because the Prime Minister can approve any change
     */
    @Override
    public boolean canApprove() {
        return true;
    }

    /**
     * Indicates whether the Prime Minister can submit change requests.
     *
     * @return false because the Prime Minister does not submit change requests
     */
    @Override
    public boolean canSubmitChangeRequest() {
        return false;
    }

    /**
     * Returns a string representation of the Prime Minister.
     *
     * @return a formatted string containing user information
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
