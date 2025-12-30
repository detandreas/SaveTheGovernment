package budget.frontend.controller;

import budget.backend.model.domain.BudgetItem;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Controller class for creating change requests in the budget frontend.
 */
public class CreateChangeRequestController {

    private static final Logger LOGGER = Logger.getLogger(
        CreateChangeRequestController.class.getName()
    );

    @FXML private ComboBox<BudgetItem> budgetItemComboBox;
    @FXML private TextField oldValueTextField;
    @FXML private TextField newValueTextField;
    @FXML private Button submitButton;
    @FXML private ToggleGroup percentGroup;

    private boolean submitClicked = false;
    /**
     * Initializes the controller class.
     * Sets up the ComboBox converter and listeners for UI components.
     */
    @FXML
    public void initialize() {
        NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.GERMANY);

        budgetItemComboBox.setConverter(new StringConverter<BudgetItem>() {
            @Override
            public String toString(BudgetItem item) {
                return (item == null) ? "" : item.getName();
            }
            @Override
            public BudgetItem fromString(String string) {
                return null;
            }
        });

        budgetItemComboBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                oldValueTextField.setText(currencyFormat
                    .format(newVal.getValue()));
                newValueTextField.clear();
                if (percentGroup.getSelectedToggle() != null) {
                    percentGroup.getSelectedToggle().setSelected(false);
                }
            }
        });
        // Αν ο χρήστης γράψει χειροκίνητα New Value, ξε-επιλέγουμε τα ποσοστά
        newValueTextField.textProperty()
            .addListener((obs, oldText, newText) -> {
            if (newValueTextField.isFocused()) {
                 if (percentGroup.getSelectedToggle() != null) {
                    percentGroup.getSelectedToggle().setSelected(false);
                }
            }
        });
    }
    /**
     * Sets the budget items to be displayed in the ComboBox.
     *
     * @param items the list of BudgetItem objects
     */
    public void setBudgetItems(ObservableList<BudgetItem> items) {
        this.budgetItemComboBox.setItems(items);
    }
    /**
     * Indicates whether the submit button was clicked.
     *
     * @return true if submit was clicked, false otherwise
     */
    public boolean isSubmitClicked() {
        return submitClicked;
    }
    /**
     * Returns the selected budget item from the ComboBox.
     *
     * @return the selected BudgetItem, or null if none is selected
     */
    public BudgetItem getSelectedBudgetItem() {
        return budgetItemComboBox.getValue();
    }
    /**
     * Parses and returns the new value entered by the user.
     *
     * @return the new value as a Double, or null if parsing fails
     */
    public Double getNewValue() {
        try {
            String text = newValueTextField.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            String cleanValue = text
                .replace(".", "")
                .replace(",", ".");
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    private void handlePercentageClick(ActionEvent event) {
        if (oldValueTextField.getText().isEmpty()) {
            // Αν δεν έχει επιλέξει είδος, ξε-επιλέγουμε το κουμπί
            if (percentGroup.getSelectedToggle() != null) {
                percentGroup.getSelectedToggle().setSelected(false);
            }
            return;
        }

        try {
            BudgetItem item = budgetItemComboBox.getValue();
            if (item == null) {
                LOGGER.log(Level.WARNING, "No budget item selected");
                return;
            }
            double currentVal = item.getValue();
            Node source = (Node) event.getSource();
            String userDataStr = (String) source.getUserData();

            if (userDataStr != null) {
                double percent = Double.parseDouble(userDataStr);
                double newVal = currentVal + (currentVal * percent);
                newValueTextField.setText(
                    String.format(Locale.GERMANY, "%,.2f", newVal)
                );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating percentage", e);
        }
    }

    @FXML
    private void handleSubmitRequest() {
        if (budgetItemComboBox.getValue() == null
            || newValueTextField.getText().isEmpty()
        ) {
            return;
        }
        submitClicked = true;
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        submitClicked = false;
        closeWindow();
    }

    private void closeWindow() {
        if (submitButton != null && submitButton.getScene() != null) {
             ((Stage) submitButton.getScene().getWindow()).close();
        }
    }
}
