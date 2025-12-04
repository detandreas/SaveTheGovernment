package budget;

import javafx.application.Application;
import javafx.stage.Stage;
import budget.ui_fx.util.SceneLoader;

public class Main extends Application {

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
    public static void main(String[] args) {
        launch(args);
    }
}
