package budget.model.domain;

import budget.model.domain.user.User;
import budget.model.enums.Status;
import java.time.LocalDate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Represents a change proposed by a
 * government member for a budget item.
 * Each pending change has a unique ID
 * and tracks the modification request
 * from submission to approval or rejection.
 */
public class PendingChange {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final int budgetItemId;
    private final User requestBy;
    private final double oldValue;
    private final double newValue;
    private Status status;
    private final LocalDate submittedDate;
    /**
     * Constructs a new pending change with the specified parameters.
     * The change is automatically assigned a unique ID and set to
     * PENDING status.
     *
     * @param budgetItemId the ID of the budget item to be modified
     * @param requestBy the user requesting the change
     * @param oldValue the current value of the budget item
     * @param newValue the proposed new value for the budget item
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PendingChange(
        int budgetItemId,
        User requestBy,
        double oldValue,
        double newValue
    ) {
        this.id = NEXT_ID.getAndIncrement();
        this.budgetItemId = budgetItemId;
        this.requestBy = requestBy;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.status = Status.PENDING;
        this.submittedDate = LocalDate.now();
    }
    /**
     * Return pending change unique id.
     * @return an int representing the id of a pending change
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the ID of the budget item that this change affects.
     *
     * @return an int representing the ID of the budget item to be modified
     */
    public int getBudgetItemId() {
        return budgetItemId;
    }
    /**
     * Return the user that requested the change.
     * @return a User instance
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public User getRequestBy() {
        return requestBy;
    }
    /**
     * Return value of the item before the requested change.
     * @return a double representing the old value
     * of the budget item
     */
    public double getOldValue() {
        return oldValue;
    }
    /**
     * Returns the proposed new value for the budget item.
     *
     * @return a double representing the new value of the budget item
     */
    public double getNewValue() {
        return newValue;
    }
    /**
     * Returns the current status of this pending change.
     *
     * @return the current Status of this change
     * (PENDING, APPROVED, or REJECTED)
     */
    public Status getStatus() {
        return status;
    }
    /**
     * Rejects this pending change by setting its status to REJECTED.
     * This action is irreversible.
     */
    public void reject() {
        this.status = Status.REJECTED;
    }
    /**
     * Approves this pending change by setting its status to APPROVED.
     * This action is irreversible.
     */
    public void approve() {
        this.status = Status.APPROVED;
    }
    /**
     * Returns the date when this change was submitted.
     *
     * @return a LocalDate representing the submission date
     */
    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    /**
     * Returns a string representation of this pending change.
     *
     * @return a string containing all relevant information about this change
     */
    @Override
    public String toString() {
        return String.format(
            "PendingChange{id=%d, budgetItemId=%d, requestBy=%s, "
            + "oldValue=%.2f, newValue=%.2f, status=%s, submittedDate=%s}",
            id, budgetItemId, requestBy, oldValue, newValue,
            status, submittedDate
        );
    }
}
