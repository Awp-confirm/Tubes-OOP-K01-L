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
import javafx.stage.Stage;
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
    
    private double tileSize = 64; // Dynamic tile size
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    
    // Asset images
    private Image floorImage;
    private Image wallImage;
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
        
        System.out.println("Loading assets...");
        
        // Load tile images
        floorImage = loadImage("/assets/picture/floor.png");
        if (floorImage != null) System.out.println("✓ floor.png loaded");
        
        wallImage = loadImage("/assets/picture/wall.png");
        if (wallImage != null) System.out.println("✓ wall.png loaded");
        
        // Load station images
        Image cookImg = loadImage("/assets/picture/cook.png");
        if (cookImg != null) {
            stationImages.put("cook", cookImg);
            System.out.println("✓ cook.png loaded");
        }
        
        Image tableImg = loadImage("/assets/picture/table.png");
        if (tableImg != null) {
            stationImages.put("table", tableImg);
            System.out.println("✓ table.png loaded");
        }
        
        Image servingImg = loadImage("/assets/picture/serving.png");
        if (servingImg != null) {
            stationImages.put("serving", servingImg);
            System.out.println("✓ serving.png loaded");
        }
        
        Image washImg = loadImage("/assets/picture/washstasion.png");
        if (washImg != null) {
            stationImages.put("wash", washImg);
            System.out.println("✓ washstasion.png loaded");
        }
        
        System.out.println("Assets loading complete!");
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
                        gc.setFill(Color.web("#3d3d3d"));
                        gc.fillRect(screenX, screenY, tileSize, tileSize);
                    }
                } else {
                    if (floorImage != null) {
                        gc.drawImage(floorImage, screenX, screenY, tileSize, tileSize);
                    } else {
                        gc.setFill(Color.web("#e8dcc8"));
                        gc.fillRect(screenX, screenY, tileSize, tileSize);
                    }
                }
                
                // Draw station
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
                        gc.setFont(javafx.scene.text.Font.font(tileSize * 0.4));
                        gc.fillText(station.getName().substring(0, 1), 
                                   screenX + tileSize * 0.35, screenY + tileSize * 0.65);
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
        
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Game");
        gameLoop.start();
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}
