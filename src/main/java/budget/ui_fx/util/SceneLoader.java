package budget.ui_fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneLoader {

    private final Stage stage;

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
        
        // Χρησιμοποιούμε την κλάση μας για να βρούμε το resource
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        // 1. Ελέγχουμε αν βρέθηκε ο πόρος πριν καλέσουμε τον FXMLLoader
        if (fxmlUrl == null) {
            System.err.println("ΣΦΑΛΜΑ ΦΟΡΤΩΣΗΣ: Το FXML αρχείο δεν βρέθηκε στον classpath: " + fxmlPath);
            // Σημαντικό: Μπορείτε να τερματίσετε την εφαρμογή ή να δείξετε ένα μήνυμα σφάλματος.
            return; 
        }

        try {
            // Χρησιμοποιούμε το URL που βρήκαμε
            FXMLLoader loader = new FXMLLoader(fxmlUrl); 
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Προαιρετικό: Φόρτωση CSS (Αν το CSS είναι στο /styles/application.css)
            // scene.getStylesheets().add(SceneLoader.class.getResource("/styles/application.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            // Πιάνει σφάλματα parsing του FXML (όπως το "ImageView is not a valid type")
            System.err.println("ΣΦΑΛΜΑ FXML PARSING Ή I/O: Πρόβλημα κατά τη φόρτωση ή ανάλυση του " + fxmlPath);
            e.printStackTrace();
        } catch (Exception e) {
            // Πιάνει οποιαδήποτε άλλη εξαίρεση
            System.err.println("Γενικό σφάλμα κατά τη φόρτωση της σκηνής: " + title);
            e.printStackTrace();
        }
    }

    public Parent loadNode(String fxmlPath) {
        URL fxmlUrl = SceneLoader.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("ΣΦΑΛΜΑ: Το αρχείο " + fxmlPath + " δεν βρέθηκε.");
            return null; // Ή πέταξε exception
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