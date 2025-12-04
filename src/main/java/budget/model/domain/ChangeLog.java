package budget.model.domain;

import java.util.UUID;

/**
 * Represents a change log entry for a budget item.
 * @param id the unique ID of the change log entry
 * @param budgetItemId the ID of the budget item
 * @param oldValue the old value of the budget item
 * @param newValue the new value of the budget item
 * @param submittedDate the date when the change was submitted
 * @param actorName the full name of the actor
 * @param actorId the ID of the actor
 */
public record ChangeLog(
    int id,
    int budgetItemId,
    double oldValue,
    double newValue,
    String submittedDate,
    String actorName,
    UUID actorId
) { }
