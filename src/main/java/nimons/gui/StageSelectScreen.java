package nimons.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import nimons.core.GameConfig;
import nimons.logic.StageProgress;
import nimons.logic.StageProgress.StageStatus;

/**
 * Stage selection screen with split layout
 */
public class StageSelectScreen {

    private final Stage stage;
    private final Scene previousScene;
    private VBox detailPanel;
    private String selectedStageId = "stageSushi"; // Default selection
    private List<Button> stageButtons = new ArrayList<>();

    public StageSelectScreen(Stage stage, Scene previousScene) {
        this.stage = stage;
        this.previousScene = previousScene;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#1a0505"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label title = new Label("SELECT STAGE");
        title.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#F2C38F"));
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(10, 20, 5, 20));

        // Main content split layout
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(10));

        // LEFT SIDE - Stage List
        VBox leftSide = createStageList();
        leftSide.setPrefWidth(400);

        // RIGHT SIDE - Stage Details
        detailPanel = new VBox(15);
        detailPanel.setAlignment(Pos.TOP_CENTER);
        detailPanel.setPadding(new Insets(10, 20, 10, 20));
        detailPanel.setBackground(new Background(new BackgroundFill(
                Color.web("#2A0F0F"), new CornerRadii(15), Insets.EMPTY)));
        detailPanel.setEffect(new DropShadow(12, Color.color(0, 0, 0, 0.6)));
        HBox.setHgrow(detailPanel, Priority.ALWAYS);

        // Load initial detail
        updateDetailPanel(selectedStageId, "Stage 1: Sushi Restaurant",
                "Master the art of sushi making!", GameConfig.PASSING_SCORE_THRESHOLD);

        mainContent.getChildren().addAll(leftSide, detailPanel);

        // Back button
        Button backButton = createBackButton();
        backButton.setOnAction(e -> goBack());

        VBox topContent = new VBox(10, title, mainContent);
        topContent.setAlignment(Pos.TOP_CENTER);

        root.setCenter(topContent);
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(20));

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Stage Select");
    }

    private VBox createStageList() {
        VBox listBox = new VBox(15);
        listBox.setAlignment(Pos.TOP_CENTER);
        listBox.setPadding(new Insets(20));
        listBox.setBackground(new Background(new BackgroundFill(
                Color.web("#2A0F0F"), new CornerRadii(15), Insets.EMPTY)));
        listBox.setEffect(new DropShadow(12, Color.color(0, 0, 0, 0.6)));

        Label listTitle = new Label("STAGES");
        listTitle.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 24));
        listTitle.setTextFill(Color.web("#E8A36B"));

        VBox stageButtonsContainer = new VBox(10);
        stageButtonsContainer.setAlignment(Pos.TOP_CENTER);

        stageButtonsContainer.getChildren().add(createStageListItem(
                "Stage 1: Sushi Restaurant",
                "stageSushi",
                "Master the art of sushi making!",
                GameConfig.PASSING_SCORE_THRESHOLD
        ));

        ScrollPane scrollPane = new ScrollPane(stageButtonsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        listBox.getChildren().addAll(listTitle, scrollPane);

        return listBox;
    }

    private Button createStageListItem(String stageName, String stageId, String description, int targetScore) {
        StageProgress progress = StageProgress.getInstance();
        StageStatus status = progress.getStageStatus(stageId);

        Button stageButton = new Button();
        stageButton.setMinWidth(350);
        stageButton.setMinHeight(80);
        stageButton.setMaxWidth(Double.MAX_VALUE);
        stageButton.setCursor(status != StageStatus.LOCKED ? Cursor.HAND : Cursor.DEFAULT);

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(stageName);
        nameLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#F2C38F"));

        Label statusLabel = new Label(getStatusText(status));
        statusLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 12));
        statusLabel.setTextFill(getStatusColor(status));
        statusLabel.setPadding(new Insets(3, 10, 3, 10));
        statusLabel.setBackground(new Background(new BackgroundFill(
                Color.web("#1a0505"), new CornerRadii(8), Insets.EMPTY)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, spacer, statusLabel);
        content.getChildren().add(header);

        stageButton.setGraphic(content);
        stageButton.setStyle(getButtonStyle(status, stageId.equals(selectedStageId)));

        stageButtons.add(stageButton);

        if (status != StageStatus.LOCKED) {
            stageButton.setOnAction(e -> {
                selectedStageId = stageId;
                updateDetailPanel(stageId, stageName, description, targetScore);
                updateAllButtonStyles();
                stageButton.setStyle(getButtonStyle(status, true));
            });

            stageButton.setOnMouseEntered(e -> {
                if (!stageId.equals(selectedStageId)) {
                    stageButton.setOpacity(0.8);
                }
            });
            stageButton.setOnMouseExited(e -> stageButton.setOpacity(1.0));
        }

        return stageButton;
    }

    private void updateAllButtonStyles() {
        for (Button btn : stageButtons) {
            btn.setStyle(getButtonStyle(StageStatus.AVAILABLE, false));
        }
    }

    private String getButtonStyle(StageStatus status, boolean selected) {
        String baseColor;

        switch (status) {
            case SUCCESS: baseColor = "#2e7d32"; break;
            case FAILED: baseColor = "#c62828"; break;
            case AVAILABLE: baseColor = "#1565c0"; break;
            case LOCKED:
            default: baseColor = "#3a1515"; break;
        }

        if (status == StageStatus.LOCKED) {
            return "-fx-background-color: " + baseColor + "; " +
                    "-fx-text-fill: #666666; " +
                    "-fx-border-color: #4b2a20; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10;";
        } else if (selected) {
            return "-fx-background-color: " + baseColor + "; " +
                    "-fx-border-color: #FFD700; " +
                    "-fx-border-width: 3; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10;";
        } else {
            return "-fx-background-color: " + baseColor + "; " +
                    "-fx-border-color: #5a3a2a; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10;";
        }
    }

    // ==================================================
    // 📌 The Method Modified to Support MAP PREVIEW
    // ==================================================
    private void updateDetailPanel(String stageId, String stageName, String description, int targetScore) {
        detailPanel.getChildren().clear();

        StageProgress progress = StageProgress.getInstance();
        StageStatus status = progress.getStageStatus(stageId);
        int bestScore = progress.getBestScore(stageId);

        // Title
        Label nameLabel = new Label(stageName);
        nameLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 36));
        nameLabel.setTextFill(Color.web("#F2C38F"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        // Status Badge
        Label statusBadge = new Label(getStatusTextDetail(status));
        statusBadge.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 20));
        statusBadge.setTextFill(getStatusColor(status));
        statusBadge.setPadding(new Insets(8, 20, 8, 20));
        statusBadge.setBackground(new Background(new BackgroundFill(
                Color.web("#1a0505"), new CornerRadii(12), Insets.EMPTY)));

        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.NORMAL, 16));
        descLabel.setTextFill(Color.web("#D4A574"));
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(500);

        // ========== MAP PREVIEW REPLACEMENT ==========
        VBox mapPreview = new VBox(10);
        mapPreview.setAlignment(Pos.CENTER);
        mapPreview.setPadding(new Insets(5, 0, 0, 0));
        mapPreview.setPrefSize(400, 170);
        mapPreview.setMaxSize(400, 170);
        mapPreview.setBackground(new Background(new BackgroundFill(
                Color.web("#1a0505"), new CornerRadii(10), Insets.EMPTY)));
        mapPreview.setEffect(new DropShadow(5, Color.color(0, 0, 0, 0.5)));

        Label mapLabel = new Label("MAP PREVIEW");
        mapLabel.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 14));
        mapLabel.setTextFill(Color.web("#888888"));

        ImageView previewImageView = new ImageView();
        previewImageView.setPreserveRatio(true);
        previewImageView.setFitWidth(360);
        previewImageView.setFitHeight(150);

        String previewPath = "/assets/picture/map.png";

        try (InputStream is = getClass().getResourceAsStream(previewPath)) {
            if (is != null) {
                Image preview = new Image(is);
                previewImageView.setImage(preview);
                mapPreview.getChildren().addAll(mapLabel, previewImageView);
            } else {
                Label missing = new Label("Preview Not Available");
                missing.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, 12));
                missing.setTextFill(Color.web("#666666"));
                mapPreview.getChildren().addAll(mapLabel, missing);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Label error = new Label("Failed to load preview");
            error.setTextFill(Color.ORANGE);
            mapPreview.getChildren().addAll(mapLabel, error);
        }

        // Score Grid
        HBox scoresBox = new HBox(60);
        scoresBox.setAlignment(Pos.CENTER);

        VBox targetBox = createScoreBox("TARGET SCORE", String.valueOf(targetScore), "#FFD700");
        VBox bestBox = createScoreBox("BEST SCORE", bestScore > 0 ? String.valueOf(bestScore) : "---", "#E8A36B");

        scoresBox.getChildren().addAll(targetBox, bestBox);

        // Play Button
        Button playButton = new Button(status == StageStatus.LOCKED ? "LOCKED" : "PLAY STAGE");
        playButton.setMinWidth(300);
        playButton.setMinHeight(60);
        playButton.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 24));
        playButton.setDisable(status == StageStatus.LOCKED);

        if (status == StageStatus.LOCKED) {
            playButton.setStyle(
                    "-fx-background-radius: 15; " +
                            "-fx-background-color: #3a1515; " +
                            "-fx-text-fill: #666666; " +
                            "-fx-border-color: #4b2a20; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 15;"
            );
        } else {
            playButton.setStyle(
                    "-fx-background-radius: 15; " +
                            "-fx-background-color: linear-gradient(#4a2010, #3a1608); " +
                            "-fx-text-fill: #FFD700; " +
                            "-fx-border-color: #FFD700; " +
                            "-fx-border-width: 4; " +
                            "-fx-border-radius: 15;"
            );
            playButton.setCursor(Cursor.HAND);
            playButton.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.7)));

            playButton.setOnMouseEntered(e -> {
                playButton.setScaleX(1.05);
                playButton.setScaleY(1.05);
            });
            playButton.setOnMouseExited(e -> {
                playButton.setScaleX(1.0);
                playButton.setScaleY(1.0);
            });

            playButton.setOnAction(e -> startStage(stageId));
        }

        // Spacers
        Region spacer1 = new Region();
        spacer1.setMinHeight(10);
        Region spacer2 = new Region();
        spacer2.setMinHeight(15);

        // Add Components
        detailPanel.getChildren().addAll(
                nameLabel,
                statusBadge,
                spacer1,
                descLabel,
                mapPreview,
                spacer2,
                scoresBox,
                playButton
        );
    }

    private VBox createScoreBox(String label, String value, String valueColor) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Label labelText = new Label(label);
        labelText.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 14));
        labelText.setTextFill(Color.web("#A0866F"));

        Label valueText = new Label(value);
        valueText.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 36));
        valueText.setTextFill(Color.web(valueColor));

        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private String getStatusText(StageStatus status) {
        switch (status) {
            case SUCCESS: return "PASS";
            case FAILED: return "FAIL";
            case AVAILABLE: return "NOT STARTED";
            case LOCKED: return "LOCKED";
            default: return "";
        }
    }

    private String getStatusTextDetail(StageStatus status) {
        switch (status) {
            case SUCCESS: return "★ PASS";
            case FAILED: return "✗ FAIL";
            case AVAILABLE: return "NOT STARTED";
            case LOCKED: return "LOCKED";
            default: return "";
        }
    }

    private Color getStatusColor(StageStatus status) {
        switch (status) {
            case SUCCESS: return Color.web("#4CAF50");
            case FAILED: return Color.web("#F44336");
            case AVAILABLE: return Color.web("#2196F3");
            case LOCKED: return Color.web("#888888");
            default: return Color.WHITE;
        }
    }

    private void startStage(String stageId) {
        System.out.println("Starting stage: " + stageId);
        GameScreen gameScreen = new GameScreen(stage, stageId);
        gameScreen.start();
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