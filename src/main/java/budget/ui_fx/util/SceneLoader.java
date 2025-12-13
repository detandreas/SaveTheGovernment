package budget.ui_fx.util;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility class for loading scenes in a JavaFX application.
 */
public final class SceneLoader {

    private static final Logger LOGGER =
                                Logger.getLogger(SceneLoader.class.getName());
    /**
     * Constructor.
     */
    private SceneLoader() {
        // prevents initialization
    }
    /**
     * Loads a new scene from the specified FXML file and sets it on the stage.
     *
     * @param stage The primary stage where scenes will be loaded.
     * @param fxmlPath The path to the FXML file.
     * @param title The title of the window.
     */
    public static void load(Stage stage, String fxmlPath, String title) {

        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ ΦΟΡΤΩΣΗΣ: Το FXML αρχείο "
                                        + "δεν βρέθηκε στον classpath: {0}",
                fxmlPath
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ FXML PARSING Ή I/O: "
                                + "Πρόβλημα κατά τη φόρτωση ή ανάλυση του {0}",
                fxmlPath
            );
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "Γενικό σφάλμα κατά τη φόρτωση της σκηνής: {0}",
                title
            );
            e.printStackTrace();
        }
    }

    /**
    * Loads a new scene from the specified FXML file and sets it on the stage.
    * Returns the controller instance for further configuration.
    *
    * @param <T> The type of the controller to be returned.
    * @param stage The primary stage where scenes will be loaded.
    * @param fxmlPath The path to the FXML file.
    * @param title The title of the window.
    * @return The controller instance, or null if loading failed.
    */
    public static <T> T loadAndGetController(
        Stage stage,
        String fxmlPath,
        String title
    ) {
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ ΦΟΡΤΩΣΗΣ: Το FXML αρχείο "
                                        + "δεν βρέθηκε στον classpath: {0}",
                fxmlPath
            );
            return null;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

            // Επιστρέφουμε το controller
            return loader.getController();

        } catch (IOException e) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ FXML PARSING Ή I/O: "
                                + "Πρόβλημα κατά τη φόρτωση ή ανάλυση του {0}",
                fxmlPath
            );
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "Γενικό σφάλμα κατά τη φόρτωση της σκηνής: {0}",
                title
            );
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads an FXML file and returns its root node.
     *
     * @param fxmlPath The path to the FXML file.
     * @return The root node of the loaded FXML, or null if loading failed.
     */
    public static Parent loadNode(String fxmlPath) {
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ: Το αρχείο {0} δεν βρέθηκε.",
                fxmlPath
            );
            return null;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            return loader.load();
        } catch (IOException e) {
            LOGGER.log(
                Level.SEVERE,
                "ΣΦΑΛΜΑ κατά τη φόρτωση του Node: {0}",
                fxmlPath
            );
            e.printStackTrace();
            return null;
        }
    }
}
