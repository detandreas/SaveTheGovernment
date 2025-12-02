package budget.model.domain;

import budget.model.enums.Status;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Locale;
import java.util.UUID;


/**
 * Represents a proposed update to a budget item.
 *
 * Records the target budget item id, requester (name/id), previous and
 * proposed values, current review status and submission timestamp.
 */
public class PendingChange {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final int budgetItemId;
    private final String budgetItemName;
    private final int budgetItemYear;
    private final String requestByName;
    private final UUID requestById;
    private final double oldValue;
    private final double newValue;
    private Status status;
    private final String submittedDate;
    /**
     * Constructs a new pending change with the specified parameters.
     * The change is automatically assigned a unique ID and set to
     * PENDING status.
     *
     * @param budgetItemId the ID of the budget item to be modified
     * @param budgetItemYear the year of the Budget that
     *                                      the budgetItem belongs to
     * @param budgetItemName the name of the budget item to be modified.
     * @param requestByName the user name requesting the change
     * @param requestById the user id requesting the change
     * @param oldValue the current value of the budget item
     * @param newValue the proposed new value for the budget item
     */
    public PendingChange(
        int budgetItemId,
        int budgetItemYear,
        String budgetItemName,
        String requestByName,
        UUID requestById,
        double oldValue,
        double newValue
    ) {
        this.id = NEXT_ID.getAndIncrement();
        this.budgetItemName = budgetItemName;
        this.budgetItemId = budgetItemId;
        this.budgetItemYear = budgetItemYear;
        this.requestByName = requestByName;
        this.requestById = requestById;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.status = Status.PENDING;
        this.submittedDate = LocalDateTime.now()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    /**
     * Return pending change unique id.
     * @return an int representing the id of a pending change
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the year of the budget item that this change affects.
     *
     * @return an int representing the year of the budget item to be modified
     */
    public int getBudgetItemYear() {
        return budgetItemYear;
    }
    /**
     * Returns the name of the budget item that this change affects.
     *
     * @return a String representing the name of the budget item to be modified
     */
    public String getBudgetItemName() {
        return budgetItemName;
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
     * Return the name of the user that requested the change.
     * @return a String representing UserName
     */
    public String getRequestByName() {
        return requestByName;
    }
    /**
     * Return the ID of the user that requested the change.
     * @return an int representing the ID of the user
     */
    public UUID getRequestById() {
        return requestById;
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
     * @return a String representing the submission date
     */
    public String getSubmittedDate() {
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
            Locale.US,
            "PendingChange{id=%d, budgetItemId=%d, budgetItemYear=%d, "
            + "budgetItemName=%s, requestByName=%s, requestById=%s, "
            + "oldValue=%.2f, newValue=%.2f, status=%s, submittedDate=%s}",
            id, budgetItemId, budgetItemYear, budgetItemName, requestByName,
            requestById, oldValue, newValue, status, submittedDate
        );
    }
}
