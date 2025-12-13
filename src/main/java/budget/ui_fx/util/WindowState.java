package budget.ui_fx.util;

public class WindowState {

    private static double width = 0;
    private static double height = 0;

    public static void setSize(double w, double h) {
        width = w;
        height = h;
    }

    public static double getWidth() {
        return width;
    }

    public static double getHeight() {
        return height;
    }

    public static boolean isStored() {
        return width > 0 && height > 0;
    }
}
