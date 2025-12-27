package budget.frontend.util;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
     * Initializes Scene for the first time.
     * Invoked only from Main.java.
     *
     * @param stage The primary stage.
     * @param fxmlPath The path to the initial FXML file.
     * @param title The title of the window.
     */
    public static void initializeScene(
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
            applicationScene.setRoot(root);
        }
        stage.setTitle(title);

        if (WindowState.isStored()) {
            stage.setWidth(WindowState.getWidth());
            stage.setHeight(WindowState.getHeight());
        }

        stage.show();
    }

    /**
     * Loads a new scene from the specified FXML file and sets it on the stage.
     * It uses the same Scene instance and only changes the root node.
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
    * It uses the same Scene instance and only changes the root node.
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

    /**
     * Loads an FXML file and returns both the root node and the controller.
     * Does NOT set the scene on the stage.
     *
     * @param fxmlPath The path to the FXML file.
     * @param <T> The type of the controller.
     * @return A ViewResult containing both
     *      the root node and the controller, or null if failed.
     */
    public static <T> ViewResult<T> loadViewWithController(String fxmlPath) {
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            LOGGER.log(
                Level.SEVERE, "ΣΦΑΛΜΑ: Το αρχείο {0} δεν βρέθηκε.",
                fxmlPath
            );
            return null;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            T controller = loader.getController(); // Παίρνουμε τον Controller

            return new ViewResult<>(root, controller);

        } catch (IOException e) {
            LOGGER.log(
                Level.SEVERE, "ΣΦΑΛΜΑ κατά τη φόρτωση του View: {0}",
                fxmlPath
            );
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A wrapper class to hold both the loaded root node and its controller.
     * Useful when loading sub-views
     * where we need to inject data into the controller.
     * @param <T> The type of the controller.
     */
    public static class ViewResult<T> {
        private final Parent root;
        private final T controller;
        /**
         * Constructor.
         *
         * @param root The root node.
         * @param controller The controller instance.
         */
        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                "Wrapper class must store external mutable JavaFX objects."
        )
        public ViewResult(Parent root, T controller) {
            this.root = root;
            this.controller = controller;
        }
        /**
         * Returns the root node.
         *
         * @return The root node.
         */
        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Returns mutable JavaFX root node intentionally."
        )
        public Parent getRoot() {
            return root;
        }
        /**
         * Returns the controller instance.
         *
         * @return The controller.
         */
        public T getController() {
            return controller;
        }
    }
}
