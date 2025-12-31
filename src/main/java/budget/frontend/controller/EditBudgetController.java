package budget.frontend.controller;

import budget.backend.model.domain.BudgetItem;
import budget.backend.service.BudgetValidationService;
import budget.backend.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class EditBudgetController {

    @FXML private Label editBudgetLabel;
    @FXML private TextField newValueTextField;

    private Stage dialogStage;
    private BudgetItem originalItem; // Κρατάμε το αρχικό αντικείμενο
    private BudgetValidationService validationService; // Το Service για τους ελέγχους
    
    private boolean saveClicked = false;
    private double resultValue = 0.0;

    @FXML
    private void initialize() {
        // Καθαρισμός στυλ όταν γράφει ο χρήστης
        newValueTextField.setOnKeyPressed(e -> {
            newValueTextField.setStyle("");
            newValueTextField.setTooltip(null);
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Inject το Validation Service
    public void setValidationService(BudgetValidationService validationService) {
        this.validationService = validationService;
    }

    public void setBudgetItem(BudgetItem item) {
        this.originalItem = item;
        editBudgetLabel.setText("Edit: " + item.getName());
        newValueTextField.setText(String.valueOf(item.getValue()));
        newValueTextField.selectAll();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public double getResultValue() {
        return resultValue;
    }

    @FXML
    private void handleSave() {
        // 1. Βασικός έλεγχος μορφής (αν είναι αριθμός)
        if (!isFormatValid()) {
            return;
        }

        try {
            double newValue = Double.parseDouble(newValueTextField.getText().replace(",", "."));
            
            // 2. Δημιουργία ενός ΠΡΟΣΩΡΙΝΟΥ αντικειμένου για τον έλεγχο
            // Υποθέτουμε ότι το BudgetItem έχει constructor copy ή setters. 
            // Εδώ φτιάχνουμε ένα αντίγραφο για να μην πειράξουμε το original πριν την έγκριση.
            BudgetItem tempItem = new BudgetItem(
                originalItem.getId(),
                originalItem.getYear(),  
                originalItem.getName(),
                newValue,
                originalItem.getIsRevenue(), 
                originalItem.getMinistries()
            );

            // 3. Κλήση του Service για έλεγχο (Business Logic)
            if (validationService != null) {
                validationService.validateBudgetItemUpdate(originalItem, tempItem);
            }

            // 4. Αν δεν πετάξει Exception, όλα καλά!
            resultValue = newValue;
            saveClicked = true;
            dialogStage.close();

        } catch (ValidationException e) {
            // 5. Αν το Service βρει λάθος (π.χ. >10% αλλαγή), το δείχνουμε
            showErrorEffect(e.getMessage());
        } catch (Exception e) {
            showErrorEffect("Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    // Ελέγχει μόνο αν το πεδίο είναι κενό ή δεν είναι αριθμός
    private boolean isFormatValid() {
        String text = newValueTextField.getText();
        if (text == null || text.trim().isEmpty()) {
            showErrorEffect("Value cannot be empty");
            return false;
        }
        try {
            Double.parseDouble(text.replace(",", "."));
            return true;
        } catch (NumberFormatException e) {
            showErrorEffect("Invalid number format");
            return false;
        }
    }

    private void showErrorEffect(String message) {
        // Κόκκινο περίγραμμα
        newValueTextField.setStyle("-fx-border-color: #a52b1e; -fx-border-width: 2px; -fx-border-radius: 5px;");
        
        // Tooltip για να δει ο χρήστης τι πήγε στραβά (π.χ. "Exceeds limit")
        Tooltip tooltip = new Tooltip(message);
        tooltip.setStyle("-fx-background-color: #a52b1e; -fx-text-fill: white; -fx-font-size: 12px;");
        newValueTextField.setTooltip(tooltip);
        
        // Εμφάνιση του tooltip αμέσως
        javafx.util.Duration duration = javafx.util.Duration.millis(3000); 
        // Hack για να εμφανιστεί το tooltip πάνω στο πεδίο
        tooltip.show(newValueTextField, 
            newValueTextField.getScene().getWindow().getX() + newValueTextField.getLayoutX(), 
            newValueTextField.getScene().getWindow().getY() + newValueTextField.getLayoutY());
            
        // Προαιρετικά: Αν θες να κρύβεται μόνο του μετά από λίγο
        // new Timeline(new KeyFrame(duration, event -> tooltip.hide())).play();
    }
}