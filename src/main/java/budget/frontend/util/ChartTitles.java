package budget.frontend.util;

    /**
     * Data class for chart titles.
     */
public class ChartTitles {
    private final String pieChartTitle;
    private final String lineChart1Title;
    private final String lineChart2Title;
    private final String barChartTitle;


    /**
     * Constructor for ChartTitles.
     * @param pieChartTitle the pie chart title
     * @param lineChart1Title the first line chart title
     * @param lineChart2Title the second line chart title
     * @param barChartTitle the bar chart title
     */
    public ChartTitles(String pieChartTitle, String lineChart1Title,
                        String lineChart2Title, String barChartTitle
    ) {
        this.pieChartTitle = pieChartTitle;
        this.lineChart1Title = lineChart1Title;
        this.lineChart2Title = lineChart2Title;
        this.barChartTitle = barChartTitle;
    }

    /**
     * Gets the pie chart title.
     * @return the pie chart title
     */
    public String getPieChartTitle() {
        return pieChartTitle;
    }

    /**
     * Gets the first line chart title.
     * @return the first line chart title
     */
    public String getLineChart1Title() {
        return lineChart1Title;
    }

    /**
     * Gets the second line chart title.
     * @return the second line chart title
     */
    public String getLineChart2Title() {
        return lineChart2Title;
    }

    /**
     * Gets the bar chart title.
     * @return the bar chart title
     */
    public String getBarChartTitle() {
        return barChartTitle;
    }
}
