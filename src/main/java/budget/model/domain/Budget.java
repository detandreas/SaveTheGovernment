package budget.model;

import java.util.List;
import java.util.ArrayList;
/**
 * Represent the state budget.
 */
public class Budget {
    private List<BudgetItem> items;
    private final int year;
    private double totalRevenue;
    private double totalExpense;
    private double netResult;
    /**
     * Constructor for budget.
     * Only 2 parameters
     * @param items list of budget items
     * @param year issued year
     */
    public Budget(List<BudgetItem> items, int year) {
        this.items = new ArrayList<>(items);
        this.year = year;
    }
    /**
     * Complete constructor for Budget.
     * @param items list of budget items
     * @param year issued year
     * @param totalRevenue the sum of revenue budget items
     * @param totalExpense the sum of expense budget items
     * @param netResult totalRevenue - totalExpense
     */
    public Budget(
        List<BudgetItem> items,
        int year,
        double totalRevenue,
        double totalExpense,
        double netResult
    ) {
        this(items, year);
        this.totalRevenue = totalRevenue;
        this.totalExpense = totalExpense;
        this.netResult = netResult;
    }
    /**
     * Return a list of all the budget items.
     * @return a copy of the list
     * containing budget item instances
     */
    public List<BudgetItem> getItems() {
        return new ArrayList<>(items);
    }
    /**
     * Set budget items of budget state.
     * @param items budget items
     */
    public void setItems(List<BudgetItem> items) {
        this.items = new ArrayList<>(items);
    }
    /**
     * Return the year of budget state.
     * @return an int representing the issued
     * year of budget state
     */
    public int getYear() {
        return year;
    }
    /**
     * Return the total revenue of budget state.
     * @return a double representing the sum of revenue budget items
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }
    /**
     * Set the total revenue of budget state.
     * @param totalRevenue the sum of revenue budget items
     */
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    /**
     * Return the total expense of budget state.
     * @return a double representing the sum of expense budget items
     */
    public double getTotalExpense() {
        return totalExpense;
    }
    /**
     * Set the total expense of budget state.
     * @param totalExpense the sum of expense budget items
     */
    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }
    /**
     * Return the net result of budget state.
     * @return a double representing totalRevenue - totalExpense
     */
    public double getNetResult() {
        return netResult;
    }
    /**
     * Set the net result of budget state.
     * @param netResult totalRevenue - totalExpense
     */
    public void setNetResult(double netResult) {
        this.netResult = netResult;
    }
    /**
     * Returns a string representation of the budget.
     * @return a formatted string containing budget information
     */
    @Override
    public String toString() {
        return String.format(
            "Budget{year=%d, totalRevenue=%.2f, totalExpense=%.2f, "
            + "netResult=%.2f, itemsCount=%d}",
            year, totalRevenue, totalExpense, netResult, items.size()
        );
    }
}
