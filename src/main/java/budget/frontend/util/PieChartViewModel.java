package budget.frontend.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class PieChartViewModel {

    private final PieChart chart;

    /**
     * Class constructor.
     * @param chart the chart which is being edited
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2"
    )
    public PieChartViewModel(PieChart chart) {
        this.chart = chart;
    }

    /**
     * Loads data into pie chart.
     * @param data pie chart data
     */
    public void loadData(ObservableList<PieChart.Data> data) {
        chart.getData().clear();
        chart.setData(data);
    }

    /**
     * Sets the chart title.
     * @param title chart title
     */
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    /**
     * Clears the chart.
     */
    public void clear() {
        chart.getData().clear();
    }

    /**
     * Clears and sets error message in title.
     * @param message showed if something fails
     */
    public void showError(String message) {
        clear();
        setTitle(message);
    }


}
