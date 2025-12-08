package budget.ui_fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

/** 
 * Utility class for loading scenes in a JavaFX application.
 */
public class SceneLoader {

    private final Stage stage;
    /** 
     * Constructor for SceneLoader.
     *
     * @param stage The primary stage where scenes will be loaded.
     */
    public SceneLoader(Stage stage) {
        this.stage = stage;
    }
    /** 
     * Loads a new scene from the specified FXML file and sets it on the stage.
     *
     * @param fxmlPath The path to the FXML file.
     * @param title    The title of the window.
     */
    public void load(String fxmlPath, String title) {
        
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("ΣΦΑΛΜΑ ΦΟΡΤΩΣΗΣ: Το FXML αρχείο δεν βρέθηκε στον classpath: " + fxmlPath);
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
            System.err.println("ΣΦΑΛΜΑ FXML PARSING Ή I/O: Πρόβλημα κατά τη φόρτωση ή ανάλυση του " + fxmlPath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Γενικό σφάλμα κατά τη φόρτωση της σκηνής: " + title);
            e.printStackTrace();
        }
    }
    /** 
     * Loads an FXML file and returns its root node.
     *
     * @param fxmlPath The path to the FXML file.
     * @return The root node of the loaded FXML, or null if loading failed.
     */
    public Parent loadNode(String fxmlPath) {
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("ΣΦΑΛΜΑ: Το αρχείο " + fxmlPath + " δεν βρέθηκε.");
            return null;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            return loader.load();
        } catch (IOException e) {
            System.err.println("ΣΦΑΛΜΑ κατά τη φόρτωση του Node: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}
