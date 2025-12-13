package budget;

import java.util.logging.Level;
import java.util.logging.Logger;

import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import budget.frontend.util.WindowState;
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
            primaryStage.setMaximized(true);
            primaryStage.requestFocus();
            primaryStage.toFront();

            // Event listener: όταν αλλάζει μέγεθος ο χρήστης
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                WindowState.setSize(
                        primaryStage.getWidth(),
                        primaryStage.getHeight()
                    );
            });

            primaryStage.heightProperty().addListener((obs, oldVal, newVal)
                                                                        -> {
                WindowState.setSize(
                        primaryStage.getWidth(),
                        primaryStage.getHeight()
                    );
            });
            SceneLoader.initializeScene(
                primaryStage,
                Constants.WELCOME_VIEW,
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
