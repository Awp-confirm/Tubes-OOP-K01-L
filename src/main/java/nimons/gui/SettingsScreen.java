package nimons.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import nimons.core.GameSettings;
import nimons.core.GameConfig;
import nimons.core.GameSettings.Difficulty;

/**
 * Settings screen for difficulty selection
 */
public class SettingsScreen {
    
    private final Stage stage;
    private final Scene previousScene;
    
    public SettingsScreen(Stage stage, Scene previousScene) {
        this.stage = stage;
        this.previousScene = previousScene;
    }
    
    public void show() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
            Color.web("#1a0505"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Title
        Label title = new Label("SETTINGS");
    title.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#F2C38F"));
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(50, 20, 30, 20));
        
        // Difficulty section
        Label difficultyLabel = new Label("DIFFICULTY");
    difficultyLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 28));
        difficultyLabel.setTextFill(Color.web("#E8A36B"));
        
        // Current difficulty display
        GameSettings settings = GameSettings.getInstance();
        Label currentDifficulty = new Label("Current: " + settings.getDifficulty().getDisplayName());
    currentDifficulty.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.NORMAL, 20));
        currentDifficulty.setTextFill(Color.web("#D4A574"));
        
        // Difficulty buttons
        VBox difficultyButtons = new VBox(15);
        difficultyButtons.setAlignment(Pos.CENTER);
        
        for (Difficulty diff : Difficulty.values()) {
            Button btn = createDifficultyButton(diff, currentDifficulty);
            difficultyButtons.getChildren().add(btn);
        }
        
        VBox content = new VBox(20, difficultyLabel, currentDifficulty, difficultyButtons);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));
        
        // Back button
        Button backButton = createBackButton();
        backButton.setOnAction(e -> goBack());
        
        VBox centerContent = new VBox(30, title, content);
        centerContent.setAlignment(Pos.TOP_CENTER);
        
        root.setCenter(centerContent);
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(30));
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Settings");
    }
    
    private Button createDifficultyButton(Difficulty difficulty, Label currentLabel) {
        GameSettings settings = GameSettings.getInstance();
        
        String livesText = difficulty.isUnlimitedLives() ? 
            "Unlimited Lives" : difficulty.getLives() + " Lives";
        
        Button button = new Button(difficulty.getDisplayName() + " - " + livesText);
        button.setMinWidth(400);
        button.setMinHeight(60);
    button.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 20));
        
        // Highlight if selected
        updateButtonStyle(button, settings.getDifficulty() == difficulty);
        
        button.setCursor(Cursor.HAND);
        button.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.6)));
        
        button.setOnAction(e -> {
            settings.setDifficulty(difficulty);
            currentLabel.setText("Current: " + difficulty.getDisplayName());
            
            // Update all buttons
            ((VBox) button.getParent()).getChildren().forEach(node -> {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    updateButtonStyle(btn, btn == button);
                }
            });
        });
        
        button.setOnMouseEntered(e -> {
            if (settings.getDifficulty() != difficulty) {
                button.setScaleX(1.03);
                button.setScaleY(1.03);
            }
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        return button;
    }
    
    private void updateButtonStyle(Button button, boolean selected) {
        if (selected) {
            button.setStyle(
                "-fx-background-radius: 15; " +
                "-fx-background-color: linear-gradient(#4a2010, #3a1608); " +
                "-fx-text-fill: #FFD700; " +
                "-fx-border-color: #FFD700; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 15;"
            );
        } else {
            button.setStyle(
                "-fx-background-radius: 15; " +
                "-fx-background-color: linear-gradient(#2d0b0b, #220606); " +
                "-fx-text-fill: #F2C38F; " +
                "-fx-border-color: #4b2a20; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;"
            );
        }
    }
    
    private Button createBackButton() {
        Button button = new Button("BACK TO MENU");
        button.setMinWidth(200);
        button.setMinHeight(50);
    button.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 18));
        button.setStyle(
            "-fx-background-radius: 15; " +
            "-fx-background-color: linear-gradient(#2d0b0b, #220606); " +
            "-fx-text-fill: #F2C38F; " +
            "-fx-border-color: #E8A36B; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 15;"
        );
        button.setCursor(Cursor.HAND);
        button.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.6)));
        
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        return button;
    }
    
    private void goBack() {
        stage.setScene(previousScene);
        stage.setTitle("Nimonscooked - Main Menu");
    }
}
