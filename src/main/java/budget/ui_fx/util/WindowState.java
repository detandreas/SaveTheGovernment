package budget.ui_fx.util;

/**
 * Class that maintains the window size of the application.
 * Used for storing and retrieving window dimensions between different scenes.
 */
public final class WindowState {

    private static double width = 0;
    private static double height = 0;

    private WindowState() {
        // prevent initialization
    }
    /**
     * Sets the window size.
     *
     * @param w the width of the window in pixels
     * @param h the height of the window in pixels
     */
    public static void setSize(double w, double h) {
        width = w;
        height = h;
    }

    /**
     * Returns the width of the window.
     *
     * @return the width of the window in pixels
     */
    public static double getWidth() {
        return width;
    }

    /**
     * Returns the height of the window.
     *
     * @return the height of the window in pixels
     */
    public static double getHeight() {
        return height;
    }

    /**
     * Checks if the window size has been stored.
     *
     * @return true if both width and height are greater than 0,
     *         false otherwise
     */
    public static boolean isStored() {
        return width > 0 && height > 0;
    }
}
