package budget.model.domain;
/**
 * Represents a change log entry for a budget item.
 * Each change log entry has a unique ID,
 * a budget item ID,
 * an old value,
 * a new value,
 * a submitted date,
 * an actor user name,
 * and an actor ID.
 */
public record ChangeLog(
    int id,
    int budgetItemId,
    double oldValue,
    double newValue,
    String submittedDate,
    String actorUserName,
    int actorId
) { 
    /**
     * Constructs a new change log entry.
     * @param id the unique ID of the change log entry
     * @param budgetItemId the ID of the budget item
     * @param oldValue the old value of the budget item
     * @param newValue the new value of the budget item
     * @param submittedDate the date when the change was submitted
     * @param actorUserName the user name of the actor
     * @param actorId the ID of the actor
     */
}
