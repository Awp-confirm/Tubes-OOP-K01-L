package nimons.core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nimons.gui.MainMenuScene;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        MainMenuScene menu = new MainMenuScene(primaryStage);
        Scene scene = new Scene(menu.rootPane);
        scene.getStylesheets().add(getClass().getResource("/styles/mainmenu.css").toExternalForm());

        primaryStage.setTitle("Nimonscooked - Main Menu");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}