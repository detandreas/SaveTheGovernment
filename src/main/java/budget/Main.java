package budget;

import java.util.logging.Level;
import java.util.logging.Logger;

import budget.ui_fx.util.SceneLoader;
import javafx.application.Application;
import javafx.stage.Stage;
/**
 * Main class for the Save The Government application.
 * This class extends JavaFX Application to launch the GUI.
 */
public class Main extends Application {
    private static final Logger LOGGER =
                                Logger.getLogger(Main.class.getName());
    /**
     * Starts the JavaFX application.
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            SceneLoader.load(
                primaryStage,
                "/view/WelcomeView.fxml",
                "Save The Government App - Welcome"
            );
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "Κρίσιμο σφάλμα κατά την εκκίνηση της εφαρμογής!"
            );
            e.printStackTrace();
        }
    }
    /**
     * The main entry point for the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
