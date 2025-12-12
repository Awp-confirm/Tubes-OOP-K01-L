package nimons.core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nimons.gui.MainMenuScene;
import javafx.scene.text.Font;
import java.io.InputStream;
import nimons.core.GameConfig;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Load custom font dari resources
        try {
            String[] candidates = new String[] {
                "/assets/fonts/PixelifySans.ttf",
                "/assets/fonts/PixelifySans-Regular.ttf",
                "/assets/fonts/Pixelify_Sans.ttf",
                "/assets/fonts/Pixelify Sans.ttf"
            };
            for (String c : candidates) {
                InputStream is = getClass().getResourceAsStream(c);
                if (is != null) {
                    Font loaded = Font.loadFont(is, 12);
                    System.out.println("Loaded custom font from: " + c + " -> family=" + (loaded != null ? loaded.getFamily() : "null"));
                    is.close();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load custom font: " + e.getMessage());
        }

        MainMenuScene menu = new MainMenuScene(primaryStage);
        Scene scene = new Scene(menu.rootPane);
        scene.getStylesheets().add(getClass().getResource("/styles/mainmenu.css").toExternalForm());

        primaryStage.setTitle("Nimonscooked - Main Menu");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        menu.playMusic();
    }

    public static void main(String[] args) {
        launch(args);
    }
}