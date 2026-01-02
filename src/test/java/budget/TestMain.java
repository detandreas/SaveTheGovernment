package budget;

import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import budget.frontend.util.WindowState;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Mock
    private Stage primaryStage;
    
    private Main main;
    private DoubleProperty widthProperty;
    private DoubleProperty heightProperty;

    @BeforeEach
    void setUp() {
        main = new Main();
        
        // Δημιουργία properties για να προσομοιώσουμε τη συμπεριφορά του Stage
        widthProperty = new SimpleDoubleProperty(800.0);
        heightProperty = new SimpleDoubleProperty(600.0);

        // Ορισμός συμπεριφοράς του mock primaryStage
        when(primaryStage.widthProperty()).thenReturn(widthProperty);
        when(primaryStage.heightProperty()).thenReturn(heightProperty);
        
        // Χρησιμοποιούμε thenAnswer ώστε να επιστρέφει την τρέχουσα τιμή του property
        when(primaryStage.getWidth()).thenAnswer(invocation -> widthProperty.get());
        when(primaryStage.getHeight()).thenAnswer(invocation -> heightProperty.get());
    }

    @Test
    void start_validStage_doesNotThrowException() {
        try (MockedStatic<SceneLoader> sceneLoaderMock = Mockito.mockStatic(SceneLoader.class);
             MockedStatic<WindowState> windowStateMock = Mockito.mockStatic(WindowState.class)) {

            assertDoesNotThrow(() -> main.start(primaryStage));
        }
    }

    @Test
    void start_initializesWelcomeScene() {
        try (MockedStatic<SceneLoader> sceneLoaderMock = Mockito.mockStatic(SceneLoader.class);
             MockedStatic<WindowState> windowStateMock = Mockito.mockStatic(WindowState.class)) {
            
            main.start(primaryStage);

            // Επαλήθευση ότι κλήθηκε ο SceneLoader με τις σωστές παραμέτρους
            sceneLoaderMock.verify(() ->
                SceneLoader.initializeScene(
                    primaryStage,
                    Constants.WELCOME_VIEW,
                    "Save The Government App - Welcome"
                )
            );
        }
    }

    @Test
    void start_whenWindowResizes_updatesWindowState() {
        try (MockedStatic<WindowState> windowStateMock = Mockito.mockStatic(WindowState.class);
             MockedStatic<SceneLoader> sceneLoaderMock = Mockito.mockStatic(SceneLoader.class)) {
            
            // Εκκίνηση της μεθόδου για να εγγραφούν οι listeners
            main.start(primaryStage);

            // Προσομοίωση αλλαγής μεγέθους από τον χρήστη
            widthProperty.set(1024.0);
            heightProperty.set(768.0);

            // Επαλήθευση ότι το WindowState ενημερώθηκε με τις νέες διαστάσεις
            windowStateMock.verify(
                () -> WindowState.setSize(1024.0, 768.0),
                Mockito.atLeastOnce()
            );
        }
    }
}
