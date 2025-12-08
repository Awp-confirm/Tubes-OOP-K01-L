package nimons.gui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import nimons.entity.chef.Chef;
import nimons.entity.chef.Direction;
import nimons.entity.common.Position;
import nimons.entity.map.MapLoadResult;
import nimons.entity.map.MapLoader;
import nimons.entity.map.Tile;
import nimons.entity.map.TileManager;
import nimons.entity.station.AssemblyStation;
import nimons.entity.station.CookingStation;
import nimons.entity.station.CuttingStation;
import nimons.entity.station.IngredientStorageStation;
import nimons.entity.station.PlateStorageStation;
import nimons.entity.station.ServingStation;
import nimons.entity.station.Station;
import nimons.entity.station.WashingStation;

public class GameScreen {

    private final Stage stage;
    private final StackPane rootPane;
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    private TileManager tileManager;
    private List<Position> spawnPositions;
    private AnimationTimer gameLoop;
    private Chef playerChef;
    
    // Smooth movement
    private double chefRenderX = 0;
    private double chefRenderY = 0;
    private double chefTargetX = 0;
    private double chefTargetY = 0;
    private static final double MOVE_SPEED = 0.3; // Interpolation speed
    
    private double tileSize = 64; // Dynamic tile size
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    
    // Chef movement control
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private long lastMoveTime = 0;
    private static final long MOVE_COOLDOWN = 150; // milliseconds
    
    // Pause state
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private double menuMainMenuButtonX = 0;
    private double menuMainMenuButtonY = 0;
    private double menuMainMenuButtonWidth = 0;
    private double menuMainMenuButtonHeight = 0;
    
    // Asset images
    private Image floorImage;
    private Image wallImage;
    private Image chefImage;
    private Map<String, Image> stationImages;

    public GameScreen(Stage stage) {
        this.stage = stage;
        this.rootPane = new StackPane();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        
        rootPane.getChildren().add(canvas);
        
        // Load assets
        loadAssets();
        
        // Load default map
        loadMap("stageSushi");
        
        // Setup game loop
        setupGameLoop();
    }
    
    private void loadAssets() {
        stationImages = new HashMap<>();
        
        System.out.println("=== Loading Assets ===");
        
        // Load tile images
        floorImage = loadImage("/assets/picture/floor.png");
        System.out.println("Floor image loaded: " + (floorImage != null));
        
        wallImage = loadImage("/assets/picture/wall.png");
        System.out.println("Wall image loaded: " + (wallImage != null));
        
        // Load chef image
        chefImage = loadImage("/assets/picture/chef.png");
        System.out.println("Chef image loaded: " + (chefImage != null));
        
        // Load station images
        Image cookImg = loadImage("/assets/picture/cook.png");
        if (cookImg != null) {
            stationImages.put("cook", cookImg);
            System.out.println("✓ cook.png loaded");
        } else {
            System.out.println("✗ cook.png FAILED");
        }
        
        Image tableImg = loadImage("/assets/picture/table.png");
        if (tableImg != null) {
            stationImages.put("table", tableImg);
            System.out.println("✓ table.png loaded");
        } else {
            System.out.println("✗ table.png FAILED");
        }
        
        Image servingImg = loadImage("/assets/picture/serving.png");
        if (servingImg != null) {
            stationImages.put("serving", servingImg);
            System.out.println("✓ serving.png loaded");
        } else {
            System.out.println("✗ serving.png FAILED");
        }
        
        Image washImg = loadImage("/assets/picture/washstasion.png");
        if (washImg != null) {
            stationImages.put("wash", washImg);
            System.out.println("✓ washstasion.png loaded");
        } else {
            System.out.println("✗ washstasion.png FAILED");
        }
        
        System.out.println("=== Assets Loading Complete ===");
    }
    
    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("✗ Image not found: " + path);
                return null;
            }
            Image img = new Image(is);
            is.close();
            return img;
        } catch (Exception e) {
            System.err.println("✗ Failed to load image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void loadMap(String stageId) {
        try {
            MapLoader loader = new MapLoader();
            MapLoadResult result = loader.load(stageId);
            
            this.tileManager = result.getTileManager();
            this.spawnPositions = result.getSpawnPositions();
            
            // Calculate tile size to fill the entire window
            if (tileManager.getWidth() > 0 && tileManager.getHeight() > 0) {
                double tileSizeByWidth = WINDOW_WIDTH / tileManager.getWidth();
                double tileSizeByHeight = WINDOW_HEIGHT / tileManager.getHeight();
                tileSize = Math.min(tileSizeByWidth, tileSizeByHeight);
            }
            
            System.out.println("Map loaded: " + stageId);
            System.out.println("Map size: " + tileManager.getWidth() + "x" + tileManager.getHeight());
            System.out.println("Tile size: " + tileSize);
            System.out.println("Spawn positions: " + spawnPositions.size());
            
            // Create chef at spawn position
            if (!spawnPositions.isEmpty()) {
                Position spawnPos = spawnPositions.get(0);
                playerChef = new Chef("player1", "Chef", spawnPos, Direction.DOWN);
                
                // Initialize smooth position
                chefRenderX = spawnPos.getX();
                chefRenderY = spawnPos.getY();
                chefTargetX = spawnPos.getX();
                chefTargetY = spawnPos.getY();
                
                // Place chef on tile
                Tile spawnTile = tileManager.getTileAt(spawnPos);
                if (spawnTile != null) {
                    spawnTile.setChefOnTile(playerChef);
                }
                
                System.out.println("Chef spawned at: (" + spawnPos.getX() + ", " + spawnPos.getY() + ")");
            }
        } catch (Exception e) {
            System.err.println("Failed to load map: " + stageId);
            e.printStackTrace();
            
            // Create empty fallback
            Tile[][] emptyTiles = new Tile[10][10];
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 10; x++) {
                    emptyTiles[y][x] = new Tile(new Position(x, y), false);
                }
            }
            this.tileManager = new TileManager(10, 10, emptyTiles);
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                long deltaTime = (now - lastUpdate) / 1_000_000; // Convert to milliseconds
                lastUpdate = now;
                
                update(deltaTime);
                render();
            }
        };
    }

    private void update(long deltaTime) {
        // Skip update jika game di-pause
        if (isPaused) {
            return;
        }
        
        // Handle chef movement
        handleChefMovement(System.nanoTime());
        
        // Smooth chef position interpolation
        if (playerChef != null) {
            double dx = chefTargetX - chefRenderX;
            double dy = chefTargetY - chefRenderY;
            
            if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) {
                chefRenderX += dx * MOVE_SPEED;
                chefRenderY += dy * MOVE_SPEED;
            } else {
                chefRenderX = chefTargetX;
                chefRenderY = chefTargetY;
            }
        }
        
        // Update all stations
        if (tileManager != null) {
            Tile[][] tiles = tileManager.getTiles();
            for (int y = 0; y < tileManager.getHeight(); y++) {
                for (int x = 0; x < tileManager.getWidth(); x++) {
                    Tile tile = tiles[y][x];
                    if (tile != null && tile.getStation() != null) {
                        tile.getStation().update(deltaTime);
                    }
                }
            }
        }
    }

    private void render() {
        // Clear screen
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        if (tileManager == null) return;
        
        // Calculate offset to center the map
        double mapWidth = tileManager.getWidth() * tileSize;
        double mapHeight = tileManager.getHeight() * tileSize;
        double offsetX = (WINDOW_WIDTH - mapWidth) / 2;
        double offsetY = (WINDOW_HEIGHT - mapHeight) / 2;
        
        // Render tiles
        Tile[][] tiles = tileManager.getTiles();
        for (int y = 0; y < tileManager.getHeight(); y++) {
            for (int x = 0; x < tileManager.getWidth(); x++) {
                Tile tile = tiles[y][x];
                if (tile == null) continue;
                
                double screenX = offsetX + x * tileSize;
                double screenY = offsetY + y * tileSize;
                
                // Draw tile background
                if (tile.isWall()) {
                    if (wallImage != null) {
                        gc.drawImage(wallImage, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Fallback: dark gray wall
                        gc.setFill(Color.web("#2d2d2d"));
                        gc.fillRect(screenX, screenY, tileSize, tileSize);
                    }
                } else {
                    if (floorImage != null) {
                        gc.drawImage(floorImage, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Fallback: light floor
                        gc.setFill(Color.web("#e8dcc8"));
                        gc.fillRect(screenX, screenY, tileSize, tileSize);
                    }
                }
                
                // Draw station on top of floor
                Station station = tile.getStation();
                if (station != null) {
                    Image stationImage = getStationImage(station);
                    if (stationImage != null) {
                        gc.drawImage(stationImage, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Fallback to colored rectangle
                        gc.setFill(Color.web("#ff6b35"));
                        double padding = tileSize * 0.1;
                        gc.fillRect(screenX + padding, screenY + padding, 
                                   tileSize - padding * 2, tileSize - padding * 2);
                        
                        // Draw station initial
                        gc.setFill(Color.WHITE);
                        gc.setFont(javafx.scene.text.Font.font(tileSize * 0.3));
                        String initial = station.getClass().getSimpleName().substring(0, 1);
                        gc.fillText(initial, screenX + tileSize * 0.4, screenY + tileSize * 0.6);
                    }
                }
                
                // Draw spawn positions
                if (spawnPositions != null) {
                    for (Position spawn : spawnPositions) {
                        if (spawn.getX() == x && spawn.getY() == y) {
                            gc.setFill(Color.web("#4ecdc480")); // Semi-transparent
                            double ovalPadding = tileSize * 0.2;
                            gc.fillOval(screenX + ovalPadding, screenY + ovalPadding, 
                                       tileSize - ovalPadding * 2, tileSize - ovalPadding * 2);
                        }
                    }
                }
            }
        }
        
        // Draw chef with smooth movement
        if (playerChef != null && chefImage != null) {
            double chefScreenX = offsetX + chefRenderX * tileSize;
            double chefScreenY = offsetY + chefRenderY * tileSize;
            
            // Draw chef using image asset with smooth interpolation
            gc.drawImage(chefImage, chefScreenX, chefScreenY, tileSize, tileSize);
        } else if (playerChef != null) {
            // Fallback: Draw chef as a circle if image not loaded
            double chefScreenX = offsetX + chefRenderX * tileSize;
            double chefScreenY = offsetY + chefRenderY * tileSize;
            
            gc.setFill(Color.web("#ff6b6b"));
            double chefPadding = tileSize * 0.15;
            gc.fillOval(chefScreenX + chefPadding, chefScreenY + chefPadding,
                       tileSize - chefPadding * 2, tileSize - chefPadding * 2);
            
            // Draw direction indicator
            gc.setFill(Color.WHITE);
            double dirSize = tileSize * 0.15;
            double centerX = chefScreenX + tileSize / 2;
            double centerY = chefScreenY + tileSize / 2;
            
            switch (playerChef.getDirection()) {
                case UP:
                    gc.fillRect(centerX - dirSize / 2, centerY - tileSize * 0.3, dirSize, dirSize);
                    break;
                case DOWN:
                    gc.fillRect(centerX - dirSize / 2, centerY + tileSize * 0.15, dirSize, dirSize);
                    break;
                case LEFT:
                    gc.fillRect(centerX - tileSize * 0.3, centerY - dirSize / 2, dirSize, dirSize);
                    break;
                case RIGHT:
                    gc.fillRect(centerX + tileSize * 0.15, centerY - dirSize / 2, dirSize, dirSize);
                    break;
            }
        }
        
        // Render pause menu jika game di-pause
        if (isPaused) {
            renderPauseMenu();
        }
    }
    
    private Image getStationImage(Station station) {
        if (station instanceof CookingStation) {
            // R = Cooking Station (Stove/Oven)
            return stationImages.get("cook");
        } else if (station instanceof CuttingStation) {
            // C = Cutting Station
            return stationImages.get("table");
        } else if (station instanceof AssemblyStation) {
            // A = Assembly Station
            return stationImages.get("table");
        } else if (station instanceof ServingStation) {
            // S = Serving Counter
            return stationImages.get("serving");
        } else if (station instanceof WashingStation) {
            // W = Washing Station
            return stationImages.get("wash");
        } else if (station instanceof IngredientStorageStation) {
            // I = Ingredient Storage
            return stationImages.get("table");
        } else if (station instanceof PlateStorageStation) {
            // P = Plate Storage
            return stationImages.get("table");
        } else if (station instanceof nimons.entity.station.TrashStation) {
            // T = Trash Station
            return stationImages.get("table");
        }
        return null;
    }

    public void start() {
        Scene scene = new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/mainmenu.css").toExternalForm());
        
        // Setup keyboard controls
        setupKeyboardControls(scene);
        
        // Setup mouse controls for pause menu
        setupMouseControls(scene);
        
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Game");
        gameLoop.start();
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    private void goToMainMenu() {
        stop();
        MainMenuScene menu = new MainMenuScene(stage);
        Scene scene = new Scene(menu.rootPane);
        scene.getStylesheets().add(getClass().getResource("/styles/mainmenu.css").toExternalForm());
        
        stage.setTitle("Nimonscooked - Main Menu");
        stage.setScene(scene);
    }
    
    private void setupKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    moveUp = true;
                    break;
                case S:
                    moveDown = true;
                    break;
                case A:
                    moveLeft = true;
                    break;
                case D:
                    moveRight = true;
                    break;
                case ESCAPE:
                    togglePause();
                    break;
                default:
                    break;
            }
        });
        
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W:
                    moveUp = false;
                    break;
                case S:
                    moveDown = false;
                    break;
                case A:
                    moveLeft = false;
                    break;
                case D:
                    moveRight = false;
                    break;
                default:
                    break;
            }
        });
    }
    
    private void handleChefMovement(long currentTime) {
        if (playerChef == null || tileManager == null) {
            return;
        }
        
        // Movement cooldown
        if (currentTime - lastMoveTime < MOVE_COOLDOWN * 1_000_000) {
            return;
        }
        
        Direction newDirection = null;
        
        if (moveUp) {
            newDirection = Direction.UP;
        } else if (moveDown) {
            newDirection = Direction.DOWN;
        } else if (moveLeft) {
            newDirection = Direction.LEFT;
        } else if (moveRight) {
            newDirection = Direction.RIGHT;
        }
        
        if (newDirection != null) {
            Position currentPos = playerChef.getPosition();
            playerChef.setDirection(newDirection);
            
            // Calculate new position
            int newX = currentPos.getX();
            int newY = currentPos.getY();
            
            switch (newDirection) {
                case UP:
                    newY--;
                    break;
                case DOWN:
                    newY++;
                    break;
                case LEFT:
                    newX--;
                    break;
                case RIGHT:
                    newX++;
                    break;
            }
            
            Position newPos = new Position(newX, newY);
            
            // Check if can move to new position
            if (tileManager.isWalkable(newPos)) {
                // Remove chef from old tile
                Tile oldTile = tileManager.getTileAt(currentPos);
                if (oldTile != null) {
                    oldTile.setChefOnTile(null);
                }
                
                // Move chef
                playerChef.setPosition(newPos);
                
                // Update target position for smooth movement
                chefTargetX = newPos.getX();
                chefTargetY = newPos.getY();
                
                // Add chef to new tile
                Tile newTile = tileManager.getTileAt(newPos);
                if (newTile != null) {
                    newTile.setChefOnTile(playerChef);
                }
                
                lastMoveTime = currentTime;
            }
        }
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseStartTime = System.currentTimeMillis();
        }
    }
    
    private void renderPauseMenu() {
        // Draw semi-transparent overlay
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Draw pause menu box
        double menuWidth = 400;
        double menuHeight = 300;
        double menuX = (WINDOW_WIDTH - menuWidth) / 2;
        double menuY = (WINDOW_HEIGHT - menuHeight) / 2;
        
        // Menu background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
        
        // Menu border
        gc.setStroke(Color.web("#4b2a20"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
        
        // Title
        gc.setFill(Color.web("#F2C38F"));
        gc.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("PAUSED", WINDOW_WIDTH / 2, menuY + 80);
        // Instructions
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Press ESC to resume", WINDOW_WIDTH / 2, menuY + 140);
        
        // Main Menu Button
        drawButton(gc, "Main Menu", menuX + (menuWidth - 200) / 2, menuY + 190, 200, 50);
    }

    private void setupMouseControls(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (isPaused) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                
                // Check if click is within Main Menu button
                if (mouseX >= menuMainMenuButtonX && mouseX <= menuMainMenuButtonX + menuMainMenuButtonWidth &&
                    mouseY >= menuMainMenuButtonY && mouseY <= menuMainMenuButtonY + menuMainMenuButtonHeight) {
                    goToMainMenu();
                }
            }
        });
    }
    
    private void drawButton(GraphicsContext gc, String text, double x, double y, double width, double height) {
        // Store button bounds for mouse click detection
        menuMainMenuButtonX = x;
        menuMainMenuButtonY = y;
        menuMainMenuButtonWidth = width;
        menuMainMenuButtonHeight = height;
        
        // Button background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(x, y, width, height, 10, 10);
        
        // Button border
        gc.setStroke(Color.web("#4b2a20"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
        
        // Button text
        gc.setFill(Color.web("#F2C38F"));
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2 + 6);
    }
}
