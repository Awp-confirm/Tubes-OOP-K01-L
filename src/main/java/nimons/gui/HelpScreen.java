package nimons.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import nimons.core.GameConfig;

public class HelpScreen {
    
    private final Stage stage;
    private final Scene previousScene;
    
    public HelpScreen(Stage stage, Scene previousScene) {
        this.stage = stage;
        this.previousScene = previousScene;
    }
    
        
    public void show() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
            Color.web("#1a0505"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        
        Label title = new Label("GAME CONTROLS & TUTORIAL");
    title.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#F2C38F"));
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(30, 20, 20, 20));
        
        
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        contentBox.setMaxWidth(900);
        
        
        VBox movementBox = createControlBox(
            "MOVEMENT",
            new String[][] {
                {"W", "Move Up"},
                {"A", "Move Left"},
                {"S", "Move Down"},
                {"D", "Move Right"},
                {"SHIFT + WASD", "Dash"}
            }
        );
        
        
        VBox actionsBox = createControlBox(
            "ACTIONS",
            new String[][] {
                {"SPACE", "Interact with station/item"},
                {"F", "Switch chef (multiplayer)"},
                {"ESC", "Pause game"},
                {"Q", "Throw item"}
            }
        );
        
        contentBox.getChildren().addAll(movementBox, actionsBox);
        
        
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a0505; -fx-background-color: #1a0505;");
        scrollPane.setPadding(new Insets(0));
        
        
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        
        Button backButton = createBackButton();
        backButton.setOnAction(e -> goBack());
        
        VBox centerContent = new VBox(10, title, scrollPane);
        centerContent.setAlignment(Pos.TOP_CENTER);
        
        root.setCenter(centerContent);
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(20));
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Help & Tutorial");
    }
    
        
    private VBox createControlBox(String sectionTitle, String[][] controls) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(20));
        box.setBackground(new Background(new BackgroundFill(
            Color.web("#2A0F0F"), new CornerRadii(10), Insets.EMPTY)));
        box.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.5)));
        
        
        Label titleLabel = new Label(sectionTitle);
    titleLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#E8A36B"));
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        
        box.getChildren().add(titleLabel);
        
        
        for (String[] control : controls) {
            HBox controlRow = new HBox(15);
            controlRow.setAlignment(Pos.CENTER_LEFT);
            
            
            Label keyLabel = new Label(control[0]);
            keyLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 16));
            keyLabel.setTextFill(Color.web("#FFFFFF"));
            keyLabel.setMinWidth(120);
            keyLabel.setAlignment(Pos.CENTER);
            keyLabel.setBackground(new Background(new BackgroundFill(
                Color.web("#3a1f1f"), new CornerRadii(5), Insets.EMPTY)));
            keyLabel.setPadding(new Insets(8, 12, 8, 12));
            
            
            Label descLabel = new Label(control[1]);
            descLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, 16));
            descLabel.setTextFill(Color.web("#D4A574"));
            
            controlRow.getChildren().addAll(keyLabel, descLabel);
            box.getChildren().add(controlRow);
        }
        
        return box;
    }
    
        
    private VBox createInfoBox(String sectionTitle, String[] items) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(20));
        box.setBackground(new Background(new BackgroundFill(
            Color.web("#2A0F0F"), new CornerRadii(10), Insets.EMPTY)));
        box.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.5)));
        
        
        Label titleLabel = new Label(sectionTitle);
    titleLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#E8A36B"));
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        
        box.getChildren().add(titleLabel);
        
        
        for (String item : items) {
            Label itemLabel = new Label(item);
            itemLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, 14));
            itemLabel.setTextFill(Color.web("#D4A574"));
            itemLabel.setWrapText(true);
            itemLabel.setMaxWidth(800);
            itemLabel.setTextAlignment(TextAlignment.LEFT);
            box.getChildren().add(itemLabel);
        }
        
        return box;
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
