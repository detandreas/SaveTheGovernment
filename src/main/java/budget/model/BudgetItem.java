package budget.model;

/**
 * Represent a budget line item of the state budget.
 */
public class BudgetItem {

    private final int id;
    private final int year;
    private String name;
    private double value;
    private final boolean isRevenue;
    /**
     * Constructor for BudgetItem.
     * @param id unique budget item id
     * @param year issued year
     * @param name name of the budget item e.g taxes
     * @param value budget item value
     * @param isRevenue checks if budget item is issued as revenue
     */
    public BudgetItem(
        int id,
        int year,
        String name,
        double value,
        boolean isRevenue
    ) {
        this.id = id;
        this.year = year;
        this.name = name;
        this.value = value;
        this.isRevenue = isRevenue;
    }
    /**
     * Return budget item id.
     * @return id
     */
    public int getId() {
        return id;
    }
    /**
     * Return budget item issued year.
     * @return year
     */
    public int getYear() {
        return year;
    }
    /**
     * Return budget item name.
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Set name for budget item.
     * @param name new budget item name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Return value of a budget item.
     * @return value.
     */
    public double getValue() {
        return value;
    }
    /**
     * Set value of a budget item.
     * @param value value of a snigle budget item
     */
    public void setValue(double value) {
        this.value = value;
    }
    /**
     * Return if budget item is revenue.
     * @return True if item is a revenue item
     */
    public boolean getIsRevenue() {
        return isRevenue;
    }
    /**
     * Return  a string represantation of a budget item.
     * @return a formatted String containing
     * information for a budget item
     */
    @Override
    public String toString() {
        return String.format(
            "BudgetItem{id=%d, year=%d, name=%s, "
            + "value=%.2f, isRevenue=%b}",
            id, year, name, value, isRevenue
            );
    }
}
