package budget;

import javafx.application.Application;
import javafx.stage.Stage;
import budget.ui_fx.util.SceneLoader;
/**
 * Main class for the Save The Government application.
 * This class extends JavaFX Application to launch the GUI.
 */
public class Main extends Application {
    /**
     * Starts the JavaFX application.
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            SceneLoader loader = new SceneLoader(primaryStage);
            loader.load("/view/WelcomeView.fxml", "Save The Government App - Welcome");
        } catch (Exception e) {
            System.err.println("Κρίσιμο σφάλμα κατά την εκκίνηση της εφαρμογής!");
            e.printStackTrace();
        }
    }
    /**
     * The main entry point for the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
