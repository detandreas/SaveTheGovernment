package budget.ui_fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import budget.repository.UserRepository;

public class SceneLoader {

    private Stage stage;

    public SceneLoader(Stage stage) {
        this.stage = stage;
    }

    public void load(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final UserRepository userRepository = new UserRepository();

    public static UserRepository getUserRepository() {
        return userRepository;
    }
}
