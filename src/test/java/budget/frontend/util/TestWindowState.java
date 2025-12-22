package budget.frontend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestWindowState {
    @BeforeEach
    void cleanUp() {
        WindowState.setSize(0, 0);
    }

    @Test
    void TestSetSizeandGetDimentions() {
        double width = 800.0;
        double height = 600.0;

        WindowState.setSize(width, height);

        assertEquals(width, WindowState.getWidth(),
                "Failure - Width not stored Correctly");
        assertEquals(height, WindowState.getHeight(),
                "Failure - Height not stored Correctyly");
    }

    @Test
    void testIsStoredReturnsFalseInitially() {
        assertFalse(WindowState.isStored(),
                "Failure - isStored() should be false when dimensions are 0");
    }

    @Test
    void TestIsStoredReturnTrueAfterSettingSize() {
        WindowState.setSize(1024, 768);

        assertTrue(WindowState.isStored(),
                "Failure - isStored() should be true after setting valid dimensions");
    }

    @Test
    void TestIsStoredWithZeroDimentions() {
        WindowState.setSize(0, 100);
        assertFalse(WindowState.isStored(),
                "Failure - isStored() should be false if width is 0");

        WindowState.setSize(100, 0);
        assertFalse(WindowState.isStored(),
                "Failure - isStored() should be false if height is 0");
    }
}