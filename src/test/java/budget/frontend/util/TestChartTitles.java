package budget.frontend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TestChartTitles {
    ChartTitles ct;

    @Test
    void testConstructorAndGetters() {
        ct = new ChartTitles("title1", "title2", "title3", "title4");
        assertEquals("title1", ct.getPieChartTitle(), "Failure - constructor not working properly");
        assertEquals("title2", ct.getLineChart1Title(), "Failure - constructor not working properly");
        assertEquals("title3", ct.getLineChart2Title(), "Failure - constructor not working properly");
        assertEquals("title4", ct.getBarChartTitle(), "Failure - constructor not working properly");
    }

}
