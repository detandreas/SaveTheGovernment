package budget.frontend.util;

import budget.frontend.util.SceneLoader.ViewResult;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.function.BiConsumer;

/**
 * Utility class for managing and opening new windows (stages).
 */
public final class WindowUtils {

    private WindowUtils() { }

    /**
     * Loads an FXML view and opens it as a modal dialog.
     *
     * @param fxmlPath    The path to the FXML file (from Constants).
     * @param title       The title of the window.
     * @param owner       The parent window (can be null).
     * @param initializer A lambda to configure the controller
     *                    before showing the window.
     * Accepts the Controller and the Stage as arguments.
     * @param <T>         The type of the Controller.
     * @return The controller instance after
     *      the window is closed, or null if loading failed.
     */
    public static <T> T openModal(
            String fxmlPath,
            String title,
            Window owner,
            BiConsumer<T, Stage> initializer) {

        ViewResult<T> result = SceneLoader.loadViewWithController(fxmlPath);
        if (result == null) {
            AlertUtils.showError(
                "System Error",
                null, "Could not load view: "
                + fxmlPath
            );
            return null;
        }

        Parent root = result.getRoot();
        T controller = result.getController();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setResizable(false);

        if (initializer != null) {
            initializer.accept(controller, stage);
        }

        stage.showAndWait();

        return controller;
    }
}
