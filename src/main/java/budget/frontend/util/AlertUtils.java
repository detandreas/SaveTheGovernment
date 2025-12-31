package budget.frontend.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Utility class for displaying standardized alerts across the application.
 * Handles CSS styling and consistency.
 */
public final class AlertUtils {

    private static final String CSS_PATH = "/styles/dialog.css";

    private AlertUtils() {}

    /**
     * Shows a success information alert.
     *
     * @param title   The title of the window.
     * @param content The message body.
     */
    public static void showSuccess(String title, String content) {
        showStyledAlert(
            AlertType.INFORMATION, 
            title,
            null,
            content,
            "approve-alert"
        );
    }

    /**
     * Shows an error alert.
     *
     * @param title   The title of the window.
     * @param header  The header text (can be null).
     * @param content The message body.
     */
    public static void showError(String title, String header, String content) {
        showStyledAlert(
            AlertType.ERROR,
            title,
            header,
            content,
            "reject-alert"
        );
    }

    /**
     * Shows a confirmation dialog and returns true if OK was pressed.
     *
     * @param title   The title of the window.
     * @param header  The header text.
     * @param content The question to ask.
     * @return true if user clicked OK, false otherwise.
     */
    public static boolean showConfirmation(
        String title, String header, String content
    ) {
        Alert alert = createStyledAlert(
            AlertType.CONFIRMATION,
            title,
            header,
            content,
            "approve-alert"
        );
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
        /**
     * Shows a reject confirmation dialog
     * and returns true if OK was pressed.
     *
     * @param title   The title of the window.
     * @param header  The header text.
     * @param content The question to ask.
     * @return true if user clicked OK, false otherwise.
     */
    public static boolean showRejectConfirmation(
        String title, String header, String content
    ) {
        Alert alert = createStyledAlert(
            AlertType.CONFIRMATION,
            title,
            header,
            content,
            "reject-alert"
        );
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    // --- Private Helper Method ---

    private static void showStyledAlert(
        AlertType type,
        String title,
        String header,
        String content,
        String cssClass
    ) {
        Alert alert = createStyledAlert(type, title, header, content, cssClass);
        alert.showAndWait();
    }

    private static Alert createStyledAlert(
        AlertType type,
        String title,
        String header,
        String content,
        String cssClass
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setGraphic(null);

        // Styling
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String css = AlertUtils.class.getResource(CSS_PATH).toExternalForm();
            dialogPane.getStylesheets().add(css);
            dialogPane.getStyleClass().add(cssClass);
        } catch (Exception e) {
            System.err.println("Could not load CSS for Alert: " + CSS_PATH);
        }

        return alert;
    }
}
