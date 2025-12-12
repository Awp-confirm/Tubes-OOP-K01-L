package nimons.gui;

import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainMenuScene {

    public final StackPane rootPane;
    private final Stage stage;

    public MainMenuScene(Stage stage) {
        this.stage = stage;
        rootPane = new StackPane();
        // Use screen bounds for fullscreen
        rootPane.setPrefSize(javafx.stage.Screen.getPrimary().getBounds().getWidth(), 
                            javafx.stage.Screen.getPrimary().getBounds().getHeight());

        BackgroundImage bg = createBackground("/assets/menu_background.png");
        if (bg != null) rootPane.setBackground(new Background(bg));
        else rootPane.setStyle("-fx-background-color: linear-gradient(#6b0f0f, #3a0000);");

        StackPane playButton = createPlayButton();
        StackPane.setAlignment(playButton, Pos.CENTER);
        playButton.setTranslateY(85);

        HBox bottom = createBottomButtons();
        StackPane.setAlignment(bottom, Pos.CENTER);
        bottom.setTranslateY(275);

        rootPane.getChildren().addAll(bottom, playButton);

        playButton.toFront();
        playButton.setMouseTransparent(false);
        playButton.setPickOnBounds(false);
    }

    private BackgroundImage createBackground(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            Image img = new Image(is);
            BackgroundSize bs = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);
            return new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Font loadPixelFont(double size) {
        try (InputStream is = getClass().getResourceAsStream("/assets/pixel_font.ttf")) {
            if (is != null) return Font.loadFont(is, size);
        } catch (Exception ignored) {}
        return Font.font("Verdana", size);
    }

    private StackPane createPlayButton() {
        double radius = 72;
        Circle circle = new Circle(radius);
        circle.setFill(Color.web("#2A0F0F"));
        circle.setStroke(Color.web("#E8A36B"));
        circle.setStrokeWidth(6);
        circle.setEffect(new DropShadow(12, Color.color(0,0,0,0.6)));

        Polygon triangle = new Polygon(-22.0, -28.0, 28.0, 0.0, -22.0, 28.0);
        triangle.setFill(Color.web("#F2C38F"));

        StackPane btn = new StackPane(circle, triangle);
        btn.setCursor(Cursor.HAND);

        btn.setPickOnBounds(false);

        btn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handlePlay());

        btn.setOnMouseEntered(e -> { btn.setScaleX(1.06); btn.setScaleY(1.06); });
        btn.setOnMouseExited(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });

        return btn;
    }

    private HBox createBottomButtons() {
        Button settings = createMenuButton("Settings");
        Button help = createMenuButton("How To Play");
        Button exit = createMenuButton("Exit");

        settings.setOnAction(e -> handleSettings());
        help.setOnAction(e -> handleHelp());
        exit.setOnAction(e -> handleExit());

        HBox h = new HBox(65, settings, help, exit);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(10));

        h.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button) node).setMinWidth(300);
                ((Button) node).setMinHeight(75);
            }
        });

        return h;
    }

    private Button createMenuButton(String label) {
        Button b = new Button(label);
        b.setMinWidth(160);
        b.setMinHeight(48);
        b.setFont(Font.font(18));
        b.setStyle(
                "-fx-background-radius: 18; " +
                        "-fx-background-color: linear-gradient(#2d0b0b, #220606); " +
                        "-fx-text-fill: #F2C38F; " +
                        "-fx-border-color: #4b2a20; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 18;"
        );
        b.setCursor(Cursor.HAND);
        b.setEffect(new DropShadow(6, Color.color(0,0,0,0.6)));
        b.setOnMouseEntered(ev -> { b.setScaleX(1.03); b.setScaleY(1.03); });
        b.setOnMouseExited(ev -> { b.setScaleX(1.0); b.setScaleY(1.0); });
        return b;
    }

    private void handlePlay() {
        System.out.println("Opening stage select...");
        javafx.scene.Scene currentScene = stage.getScene();
        StageSelectScreen stageSelect = new StageSelectScreen(stage, currentScene);
        stageSelect.show();
    }

    private void handleSettings() {
        System.out.println("Opening settings...");
        javafx.scene.Scene currentScene = stage.getScene();
        SettingsScreen settingsScreen = new SettingsScreen(stage, currentScene);
        settingsScreen.show();
    }

    private void handleHelp() {
        System.out.println("Opening help screen...");
        javafx.scene.Scene currentScene = stage.getScene();
        HelpScreen helpScreen = new HelpScreen(stage, currentScene);
        helpScreen.show();
    }

    private void handleExit() {
        System.exit(0);
    }
    
    /**
     * Display the main menu scene
     */
    public void start() {
        javafx.scene.Scene scene = new javafx.scene.Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Main Menu");
        stage.show();
        
        // Play main menu music with loop
        playMusic();
    }
    
    /**
     * Play the main menu music
     */
    public void playMusic() {
        nimons.core.SoundManager.getInstance().playMusic("mainmenu", "music1.mp3");
    }
}