package budget.ui;

import java.util.List;

import budget.model.domain.ChangeLog;
import budget.model.domain.PendingChange;

public class DisplayFormatter {
    /**
     * Formats a list of ChangeLog entries into a table-like string representation.
     *
     * @param changeLogs the list of ChangeLog entries to format
     * @return a formatted string representing the ChangeLog entries in tabular form
     * @throws IllegalArgumentException if the changeLogs list is null or empty
     */
    public static String formatChangeLog(List<ChangeLog> changeLogs) {
        if (changeLogs == null || changeLogs.isEmpty()) {
            throw new IllegalArgumentException("ChangeLog list cannot be null or empty");
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
        sb.append(createSeparator(headers.length, 20)).append("\n");
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
     * Formats a list of PendingChange entries into a table-like string representation.
     *
     * @param pendingChanges the list of PendingChange entries to format
     * @return a formatted string representing the PendingChange entries in tabular form
     * @throws IllegalArgumentException if the pendingChanges list is null or empty
     */
    public static String formatPendingChange(List<PendingChange> pendingChanges) {
        if (pendingChanges == null || pendingChanges.isEmpty()) {
            throw new IllegalArgumentException("PendingChange list cannot be null or empty");
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
        sb.append(createSeparator(headers.length, 20)).append("\n");
        for (PendingChange change : pendingChanges) {
            String[] rowData = {
                String.valueOf(change.getId()),
                String.valueOf(change.getBudgetItemId()),
                change.getSubmittedDate(),
                change.getRequestByName(),
                String.format("%.2f", change.getOldValue()),
                String.format("%.2f", change.getNewValue()),
                change.getStatus().name(),
                String.valueOf(change.getRequestById()),
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
        int totalLength = numColumns * columnWidth + numColumns * 3; 
        return new String(new char[totalLength]).replace('\0', '-');
    }
}