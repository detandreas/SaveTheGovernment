package budget.ui;

import java.util.List;

import budget.model.domain.ChangeLog;
import budget.model.domain.PendingChange;
import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
/**
 * Utility class for formatting various
 * budget-related data into table-like string representations.
 */
public final class DisplayFormatter {

    private static final int PIPE_AND_SPACES_WIDTH = 3;
    private static final int DEFAULT_COLUMN_WIDTH = 20;

    /** Private constructor to prevent instantiation. */
    private DisplayFormatter() { }
    /**
     * Formats a list of ChangeLog entries
     * into a table-like string representation.
     *
     * @param changeLogs the list of ChangeLog
     *                   entries to format
     * @return a formatted string representing
     *         the ChangeLog entries in tabular form
     * @throws IllegalArgumentException if the changeLogs list is null or empty
     */
    public static String formatChangeLog(List<ChangeLog> changeLogs) {
        if (changeLogs == null || changeLogs.isEmpty()) {
            throw new IllegalArgumentException(
                "ChangeLog list cannot be null or empty"
            );
        }
        String[] headers = {
            "Change Log ID",
            "Budget Item ID",
            "Old Value",
            "New Value",
            "Submitted Date",
            "Submitter's ID",
        };
        StringBuilder sb = new StringBuilder();
        sb.append(createRowString(headers)).append("\n");
        sb.append(createSeparator(headers.length, DEFAULT_COLUMN_WIDTH))
          .append("\n");
        for (ChangeLog log : changeLogs) {
            String[] rowData = {
                String.valueOf(log.id()),
                String.valueOf(log.budgetItemId()),
                String.format("%.2f", log.oldValue()),
                String.format("%.2f", log.newValue()),
                log.submittedDate(),
                String.valueOf(log.actorId()),
            };
            sb.append(createRowString(rowData)).append("\n");
        }
        return sb.toString();
    }
    /**
     * Formats a list of PendingChange entries
     * into a table-like string representation.
     *
     * @param pendingChanges the list of PendingChange entries to format
     * @return a formatted string representing
     *         the PendingChange entries in tabular form
     * @throws IllegalArgumentException if the pendingChanges list
     *                                  is null or empty
     */
    public static String formatPendingChange(
        List<PendingChange> pendingChanges) {
        if (pendingChanges == null || pendingChanges.isEmpty()) {
            throw new IllegalArgumentException(
                "PendingChange list cannot be null or empty"
            );
        }
        String[] headers = {
            "Pending Change ID",
            "Budget Item ID",
            "Requester Name",
            "Requester ID",
            "Old Value",
            "New Value",
            "Status",
            "Submitted Date",
        };
        StringBuilder sb = new StringBuilder();
        sb.append(createRowString(headers)).append("\n");
        sb.append(createSeparator(headers.length, DEFAULT_COLUMN_WIDTH))
          .append("\n");
        for (PendingChange change : pendingChanges) {
            String[] rowData = {
                String.valueOf(change.getId()),
                String.valueOf(change.getBudgetItemId()),
                change.getRequestByName(),
                String.valueOf(change.getRequestById()),
                String.format("%.2f", change.getOldValue()),
                String.format("%.2f", change.getNewValue()),
                change.getStatus().name(),
                change.getSubmittedDate(),
            };
            sb.append(createRowString(rowData)).append("\n");
        }
        return sb.toString();
    }
    /**
     * Formats a Budget object into a table-like string representation.
     *
     * @param budget the Budget object to format
     * @return a formatted string representing the Budget in tabular form
     * @throws IllegalArgumentException if the budget is null
     */
    public static String formatBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        String[] headers = {
            "Year",
            "Total Revenue",
            "Total Expense",
            "Net Result",
        };
        StringBuilder sb = new StringBuilder();
        sb.append(createRowString(headers)).append("\n");
        sb.append(createSeparator(headers.length, DEFAULT_COLUMN_WIDTH))
          .append("\n");
        String[] rowData = {
            String.valueOf(budget.getYear()),
            String.format("%.2f", budget.getTotalRevenue()),
            String.format("%.2f", budget.getTotalExpense()),
            String.format("%.2f", budget.getNetResult()),
        };
        sb.append(createRowString(rowData)).append("\n");
        return sb.toString();
    }
    /**
     * Formats a list of BudgetItem entries
     * into a table-like string representation.
     *
     * @param items the list of BudgetItem entries to format
     * @return a formatted string representing
     *         the BudgetItem entries in tabular form
     * @throws IllegalArgumentException if the items list is null or empty
     */
    public static String formatBudgetItems(List<BudgetItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                "BudgetItem list cannot be null or empty"
            );
        }
        String[] headers = {
            "Budget Item ID",
            "Name of Budget Item",
            "Issued Year",
            "Value",
            "Category",
        };
        StringBuilder sb = new StringBuilder();
        sb.append(createRowString(headers)).append("\n");
        sb.append(createSeparator(headers.length, DEFAULT_COLUMN_WIDTH))
          .append("\n");
        for (BudgetItem item : items) {
            String[] rowData = {
                String.valueOf(item.getId()),
                item.getName(),
                String.valueOf(item.getYear()),
                String.format("%.2f", item.getValue()),
                item.getIsRevenue() ? "Revenue" : "Expense",
            };
            sb.append(createRowString(rowData)).append("\n");
        }
        return sb.toString();
    }
    /**
     * Formats a list of BudgetItem entries
     * for a specific ministry into a table-like string representation.
     *
     * @param items the list of BudgetItem entries to format
     * @param ministryName the name of the ministry
     * @return a formatted string representing
     *         the BudgetItem entries in tabular form
     * @throws IllegalArgumentException if the items list is null or empty
     */
    public static String formatBudgetItemsPerMinistry(
        List<BudgetItem> items,
        String ministryName) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                "BudgetItem list cannot be null or empty"
            );
        }
        String[] headers = {
            "Budget Item ID",
            "Name of Budget Item",
            "Issued Year",
            "Value",
            "Category",
        };
        StringBuilder sb = new StringBuilder();
        sb.append("Ministry: ").append(ministryName).append("\n");
        sb.append(createRowString(headers)).append("\n");
        sb.append(createSeparator(headers.length, DEFAULT_COLUMN_WIDTH))
          .append("\n");
        for (BudgetItem item : items) {
            boolean belongsToMinistry = item.getMinistries().stream()
                .anyMatch(ministry -> ministry.getDisplayName()
                .equals(ministryName));

            if (!belongsToMinistry) {
                continue; // Skip items not belonging to the specified ministry
            }
            String[] rowData = {
                String.valueOf(item.getId()),
                item.getName(),
                String.valueOf(item.getYear()),
                String.format("%.2f", item.getValue()),
                item.getIsRevenue() ? "Revenue" : "Expense",
            };
            sb.append(createRowString(rowData)).append("\n");
        }
        return sb.toString();
    }
    /**
     * Helper method to create a formatted row string.
     *
     * @param columns the array of column values
     * @return a formatted string representing the row
     */
    private static String createRowString(String[] columns) {
        return "| " + String.join(" | ", columns) + " |";
    }
    /**
     * Helper method to create a separator line for the table.
     *
     * @param numColumns the number of columns in the table
     * @param columnWidth the width of each column
     * @return a string representing the separator line
     */
    private static String createSeparator(int numColumns, int columnWidth) {
        int totalLength = numColumns * columnWidth
            + numColumns * PIPE_AND_SPACES_WIDTH;
        return new String(new char[totalLength]).replace('\0', '-');
    }
}
