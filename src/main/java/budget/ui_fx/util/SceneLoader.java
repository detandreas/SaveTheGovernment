package budget.ui_fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneLoader {

    private Stage stage;

    public SceneLoader(Stage stage) {
        this.stage = stage;
    }

    public void load(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Προαιρετικό: Φόρτωση CSS αν έχεις
            // scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Σφάλμα: Δεν βρέθηκε το αρχείο " + fxmlPath);
            System.out.println("Ελεγξε αν ο φάκελος λέγεται 'view' ή 'fxml' στα resources!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
