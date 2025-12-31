package budget.frontend.controller;

import budget.backend.model.domain.BudgetItem;
import budget.backend.service.BudgetValidationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import budget.backend.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditBudgetController {

    private static final Logger LOGGER =
        Logger.getLogger(EditBudgetController.class.getName());

    @FXML private Label editBudgetLabel;
    @FXML private TextField newValueTextField;

    private Stage dialogStage;
    private BudgetItem originalItem;
    private BudgetValidationService validationService;
    private boolean saveClicked = false;
    private double resultValue = 0.0;
    private final NumberFormat currencyFormat =
        NumberFormat.getInstance(Locale.GERMANY);

    @FXML
    private void initialize() {
        newValueTextField.setOnKeyPressed(e -> {
            newValueTextField.getStyleClass().remove("error-field");
            newValueTextField.setTooltip(null);
        });
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);
    }
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
          "JavaFX Stage is a heavy mutable object "
          + "and must be passed by reference."
    )
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setValidationService(
        BudgetValidationService validationService
    ) {
        this.validationService = validationService;
    }
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
          "Controller needs reference to the"
          + " original mutable BudgetItem to perform updates."
    )
    public void setBudgetItem(BudgetItem item) {
        this.originalItem = item;
        editBudgetLabel.setText("Edit: " + item.getName());  
        newValueTextField.setText(currencyFormat.format(item.getValue()));
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
        if (!isFormatValid()) {
            return;
        }

        try {
            NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            double newValue = nf.parse(newValueTextField.getText()).doubleValue();

            BudgetItem tempItem = new BudgetItem(
                originalItem.getId(),
                originalItem.getYear(),
                originalItem.getName(),
                newValue,
                originalItem.getIsRevenue(),
                originalItem.getMinistries()
            );

            // Κλήση του Service
            if (validationService != null) {
                validationService.validateBudgetItemUpdate(originalItem, tempItem);
            }

            resultValue = newValue;
            saveClicked = true;
            dialogStage.close();

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation failed: {0}", e.getMessage());
            showErrorEffect(e.getMessage());
            
        } catch (NumberFormatException e) {
            showErrorEffect("Invalid number format.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during save", e);
            showErrorEffect("System error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isFormatValid() {
        String text = newValueTextField.getText();
        if (text == null || text.trim().isEmpty()) {
            showErrorEffect("Value cannot be empty");
            return false;
        }
        try {

            NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            Number number = nf.parse(text);
            double val = number.doubleValue();

            if (val < 0) {
                showErrorEffect("Value cannot be negative");
                return false;
            }

            return true;

        } catch (ParseException e) {
            showErrorEffect("Invalid format. Use 1.000,00");
            return false;
        }
    }

    private void showErrorEffect(String message) {
        if (!newValueTextField.getStyleClass().contains("error-field")) {
            newValueTextField.getStyleClass().add("error-field");
        }

        Tooltip tooltip = new Tooltip(message);
        tooltip.getStyleClass().add("error-tooltip");
        tooltip.setAutoHide(true);
        newValueTextField.setTooltip(tooltip);

        tooltip.show(newValueTextField, 
            newValueTextField.getScene().getWindow().getX() + newValueTextField.getLayoutX(), 
            newValueTextField.getScene().getWindow().getY() + newValueTextField.getLayoutY() + 40);
    }
}
