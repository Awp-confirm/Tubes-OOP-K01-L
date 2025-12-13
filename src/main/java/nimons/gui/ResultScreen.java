package nimons.gui;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nimons.core.GameConfig;
import javafx.stage.Stage;
import nimons.core.SoundManager;
import nimons.logic.GameState.FailReason;

public class ResultScreen {
    private final Stage stage;
    private final StackPane rootPane;
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    
    private int finalScore;
    private boolean isPassed;
    private String resultMessage;
    private String failDescription;
    private int passThreshold;
    
    private double backButtonX = 0;
    private double backButtonY = 0;
    private double backButtonWidth = 0;
    private double backButtonHeight = 0;

    public ResultScreen(Stage stage, int finalScore, boolean isPassed, int passThreshold) {
        this(stage, finalScore, isPassed, passThreshold, FailReason.NONE);
    }
    
    public ResultScreen(Stage stage, int finalScore, boolean isPassed, int passThreshold, FailReason failReason) {
        this.stage = stage;
        this.rootPane = new StackPane();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        
        this.finalScore = finalScore;
        this.isPassed = isPassed;
        this.passThreshold = passThreshold;
        this.resultMessage = isPassed ? "PASS" : "FAIL";
        this.failDescription = getFailDescription(failReason, isPassed);
        
        rootPane.getChildren().add(canvas);
    }

        
    public void start() {
        Scene scene = new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        
        setupMouseControls(scene);
        
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Result");
        
        
        if (isPassed) {
            SoundManager.getInstance().playSoundEffect("pass");
        } else {
            SoundManager.getInstance().playSoundEffect("fail");
        }
        
        
        render();
    }

        
    private void render() {
        
        gc.setFill(Color.web("#2A0F0F"));
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        
        double boxWidth = 600;
        double boxHeight = 500;
        double boxX = (WINDOW_WIDTH - boxWidth) / 2;
        double boxY = (WINDOW_HEIGHT - boxHeight) / 2;
        
        
        gc.setFill(Color.web("#4b2a20"));
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        
        double titleY = boxY + 120;
        gc.setFill(isPassed ? Color.web("#2ecc71") : Color.web("#e74c3c"));
    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 80));
        drawCenteredText(gc, resultMessage, boxX + (boxWidth - 100) / 2 - 20, titleY + 30);
        
        double scoreStartY = titleY + 100;
        
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 28));
        drawCenteredText(gc, "Final Score", boxX + boxWidth / 2 - 40, scoreStartY);
        
    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 60));
        gc.setFill(Color.web("#E8A36B"));
        drawCenteredText(gc, String.valueOf(finalScore), boxX + boxWidth / 2 - 10, scoreStartY + 70);
        
        
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, 20));
        drawCenteredText(gc, "Required: " + passThreshold, boxX + boxWidth / 2 - 20, scoreStartY + 110);
        
        
        if (!isPassed && failDescription != null) {
            gc.setFill(Color.web("#e74c3c"));
            gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
            drawCenteredText(gc, failDescription, boxX + boxWidth / 2 - 60, scoreStartY + 150);
        }
        
        
        drawButton(gc, "Back to Menu", boxX + (boxWidth - 190) / 2, boxY + boxHeight - 80, 200, 50);
    }
    
    private String getFailDescription(FailReason reason, boolean passed) {
        if (passed) {
            return null;
        }
        
        switch (reason) {
            case TIME_UP:
                return "Time's Up! Not enough score before time ran out.";
            case NO_LIVES:
                return "No Lives Remaining! Too many mistakes.";
            default:
                return "Failed to reach target score.";
        }
    }

        
    private void drawButton(GraphicsContext gc, String text, double x, double y, double width, double height) {
        
        backButtonX = x;
        backButtonY = y;
        backButtonWidth = width;
        backButtonHeight = height;
        
        
        gc.setFill(Color.web("#2A0F0F"));
        gc.fillRoundRect(x, y, width, height, 10, 10);
        
        
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
        
        
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, 18));
        drawCenteredText(gc, text, x + width / 2 - 20, y + height / 2 + 6);
    }
    
    
        
    private void drawLeftAlignedText(GraphicsContext gc, String text, double x, double y) {
        gc.fillText(text, x, y);
    }
    
    
        
    private void drawCenteredText(GraphicsContext gc, String text, double centerX, double centerY) {
        
        
        
        double textWidth = text.length() * 6.0; 
        double x = centerX - textWidth / 2;
        gc.fillText(text, x, centerY);
    }

    private void setupMouseControls(Scene scene) {
        scene.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            
            
            if (mouseX >= backButtonX && mouseX <= backButtonX + backButtonWidth &&
                mouseY >= backButtonY && mouseY <= backButtonY + backButtonHeight) {
                goToMainMenu();
            }
        });
    }

        
    private void goToMainMenu() {
        
        GameScreen.resetInstance();
        
        MainMenuScene menu = new MainMenuScene(stage);
        menu.playMusic();  
        Scene scene = new Scene(menu.rootPane, 1920, 1080);
        stage.setScene(scene);
        stage.show();
    }
}
