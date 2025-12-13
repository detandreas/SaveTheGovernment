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
    
    // Αποθήκευση ενός μόνο Scene instance για όλη την εφαρμογή
    private static Scene applicationScene;
    
    /**
     * Constructor.
     */
    private SceneLoader() {
        // prevents initialization
    }
    
    /**
     * Αρχικοποιεί το Scene για πρώτη φορά.
     * Καλείται μόνο από το Main.java.
     *
     * @param stage The primary stage.
     * @param fxmlPath The path to the initial FXML file.
     * @param title The title of the window.
     */
    public static void initializeScene(Stage stage, String fxmlPath, String title) {
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
            
            configureStage(root, stage, title);

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
     * Configures the Stage with the new root node.
     * Creates the Scene instance only on the first call,
     * while on subsequent changes it only updates the root node
     * to avoid transition effects.
     * Also preserves the window size set by the user
     * and updates the window title.
     *
     * @param root The root node to be displayed on the Stage
     * @param stage The Stage to be configured
     * @param title The window title
     */
    private static void configureStage(Parent root, Stage stage, String title) {
        // Αν το Scene δεν έχει αρχικοποιηθεί ακόμα, το δημιουργούμε
        if (applicationScene == null) {
            applicationScene = new Scene(root);
            stage.setScene(applicationScene);
        } else {
            // Αλλάζουμε μόνο το root node - δεν δημιουργούμε νέο Scene
            applicationScene.setRoot(root);
        }
        stage.setTitle(title);
        
        // Αν ο χρήστης έχει αλλάξει μέγεθος → χρησιμοποίησέ το
        if (WindowState.isStored()) {
            stage.setWidth(WindowState.getWidth());
            stage.setHeight(WindowState.getHeight());
        }
        
        stage.show();
    }
    
    /**
     * Loads a new scene from the specified FXML file and sets it on the stage.
     * Χρησιμοποιεί το ίδιο Scene instance και αλλάζει μόνο το root node.
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
            
            configureStage(root, stage, title);

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
    * Χρησιμοποιεί το ίδιο Scene instance και αλλάζει μόνο το root node.
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

            configureStage(root, stage, title);

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
