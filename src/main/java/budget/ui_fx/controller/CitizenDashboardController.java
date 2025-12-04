package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import budget.constants.Message;

public class CitizenDashboardController {
    
    @FXML private Button viewBudgetButton;
    @FXML private Button viewStatisticsButton;
    @FXML private ImageView logoImageView;

    @FXML
    public void initialize() {
        viewBudgetButton.setText("View Total Budget");
        viewStatisticsButton.setText("View Statistics");
    }
}
