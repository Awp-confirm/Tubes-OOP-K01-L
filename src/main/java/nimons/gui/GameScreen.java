package nimons.gui;

import java.io.InputStream;
import java.util.ArrayList;
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
import nimons.core.GameConfig;
import nimons.core.SoundManager;
import nimons.entity.chef.Chef;
import nimons.entity.chef.Direction;
import nimons.entity.common.Position;
import nimons.entity.item.IngredientState;
import nimons.entity.item.ingredient.Cucumber;
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.map.MapLoadResult;
import nimons.entity.map.MapLoader;
import nimons.entity.map.Tile;
import nimons.entity.map.TileManager;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;
import nimons.entity.station.AssemblyStation;
import nimons.entity.station.CookingStation;
import nimons.entity.station.CuttingStation;
import nimons.entity.station.IngredientStorageStation;
import nimons.entity.station.PlateStorageStation;
import nimons.entity.station.ServingStation;
import nimons.entity.station.Station;
import nimons.entity.station.WashingStation;
import nimons.logic.GameState;
import nimons.logic.concurrency.GameTaskExecutor;
import nimons.logic.concurrency.OrderGeneratorTask;
import nimons.logic.order.OrderManager;

public class GameScreen {

    // Use centralized game configuration
    private static final int WINDOW_WIDTH = GameConfig.WINDOW_WIDTH;
    private static final int WINDOW_HEIGHT = GameConfig.WINDOW_HEIGHT;
    
    private final Stage stage;
    private final StackPane rootPane;
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    private TileManager tileManager;
    private List<Position> spawnPositions;
    private AnimationTimer gameLoop;
    private Chef playerChef; 	// Chef pertama
    private Chef chef2; 	 	// Chef kedua
    private Chef activeChef; 	// Chef yang sedang dikontrol
    
    // Smooth movement
    private double chefRenderX = 0;
    private double chefRenderY = 0;
    private double chefTargetX = 0;
    private double chefTargetY = 0;
    private double tileSize = 64; // Dynamic tile size
    
    // Chef movement control
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private long lastMoveTime = 0;
    
    // Dash control
    private boolean shiftPressed = false;
    
    // Pause state
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private double menuResumeButtonX = 0;
    private double menuResumeButtonY = 0;
    private double menuResumeButtonWidth = 0;
    private double menuResumeButtonHeight = 0;
    private double menuMainMenuButtonX = 0;
    private double menuMainMenuButtonY = 0;
    private double menuMainMenuButtonWidth = 0;
    private double menuMainMenuButtonHeight = 0;
    
    // Game state (timer and score)
    private GameState gameState;
    
    // Order manager
    private OrderManager orderManager;
    
    // Asset images
    private Image floorImage;
    private Image wallImage;
    private Image chefImage; // Chef idle image (menghadap kanan)
    private Image chefMoveGif; // Chef move animation (pergerakan ke kanan)
    // Directional chef images (idle/move) keyed by Direction
    private Map<Direction, Image> chefIdleImages = new HashMap<>();
    private Map<Direction, Image> chefMoveImages = new HashMap<>();
    private Map<String, Image> stationImages;
    private Map<String, Image> itemImages; // For ingredients, plates, utensils
    private Image boilingPotFillGif; // GIF untuk boiling pot fill animation
    
    // Chef animation tracking
    private Map<Chef, Boolean> chefIsMoving = new HashMap<>(); // Track apakah chef sedang bergerak

    // --- LOGGING & SINGLETON FIELDS ---
    private List<String> onScreenLogs = new ArrayList<>();
    private static GameScreen instance; // Static instance untuk Singleton
    private final static int MAX_LOGS = 5; // Maksimal 5 baris log di layar
    
    // Game timing
    private long gameStartTime = 0;
    
    // --- CONCURRENCY FIELDS ---
    private GameTaskExecutor taskExecutor;
    private OrderGeneratorTask orderGeneratorTask;
    private Thread orderGeneratorThread;
    
    private String currentStageId;

    public GameScreen(Stage stage) {
        this(stage, "stageSushi"); // Default stage
    }
    
    public GameScreen(Stage stage, String stageId) {
        this.stage = stage;
        this.currentStageId = stageId;
        this.rootPane = new StackPane();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        
        // --- IMPLEMENTASI SINGLETON ---
        // Reset instance jika ada instance lama untuk memungkinkan restart game
        instance = this; // Set instance saat konstruktor dipanggil
        // ------------------------------
        
        rootPane.getChildren().add(canvas);
        
        // Initialize game state using GameConfig and difficulty settings
        this.gameState = new GameState(GameConfig.GAME_DURATION_SECONDS, GameConfig.PASSING_SCORE_THRESHOLD);
        
        // Initialize order manager (singleton)
        this.orderManager = OrderManager.getInstance();
        
        // Load assets
        loadAssets();
        
        // Load stage map
        loadMap(stageId);
        
        // Setup game loop
        setupGameLoop();
    }
    
    /**
     * Getter statis untuk instance GameScreen (Singleton).
     */
    public static GameScreen getInstance() { 
        return instance; 
    }
    
    /**
     * Reset singleton instance (dipanggil saat kembali ke main menu)
     */
    public static void resetInstance() {
        if (instance != null && instance.gameLoop != null) {
            instance.gameLoop.stop();
        }
        instance = null;
        
        // Reset all singleton stations to clear state
        PlateStorageStation.resetInstance();
    }
    
    /**
     * Get GameState untuk diakses oleh Station
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Method untuk menyimpan log dan ditampilkan di HUD (Dipanggil dari Station.java)
     */
    public void addLog(String message) {
        // Tambahkan ke depan list (LIFO: Log terbaru ada di index 0)
        onScreenLogs.add(0, message); 
        
        // Batasi jumlah log yang ditampilkan
        if (onScreenLogs.size() > MAX_LOGS) {
            // Hapus log yang paling lama (di akhir list)
            onScreenLogs.remove(onScreenLogs.size() - 1); 
        }
    }
    
    
    private void loadAssets() {
        // Initialize Map
        stationImages = new HashMap<>();
        
        System.out.println("=== Loading Assets ===");
        
        // Load tile images
        floorImage = loadImage("/assets/picture/tile.png");
        System.out.println("Floor (tile) image loaded: " + (floorImage != null));
        
        wallImage = loadImage("/assets/picture/wall.png");
        if (wallImage == null) {
            wallImage = floorImage; // Use floor as fallback
            System.out.println("Wall image using floor fallback");
        } else {
            System.out.println("Wall image loaded: " + (wallImage != null));
        }
        
        // Load chef image
        chefImage = loadImage("/assets/picture/chef side idle.gif");
        System.out.println("Chef image loaded: " + (chefImage != null));
        
        // Load chef move animation GIF
        chefMoveGif = loadImage("/assets/picture/chef side move.gif");
        System.out.println("Chef move animation loaded: " + (chefMoveGif != null));
        
    // Load directional chef assets (down/up). If you have left/right specific assets,
    // you can place them here as well. We will reuse 'side' assets for RIGHT and flip for LEFT.
    Image chefDownIdle = loadImage("/assets/picture/chef down idle.gif");
    Image chefDownMove = loadImage("/assets/picture/chef down move.gif");
    Image chefUpIdle = loadImage("/assets/picture/chef up idle.gif");
    Image chefUpMove = loadImage("/assets/picture/chef up move.gif");

    // Register to maps with fallbacks
    if (chefDownIdle != null) { chefIdleImages.put(Direction.DOWN, chefDownIdle); System.out.println("✓ chef down idle loaded"); }
    if (chefDownMove != null) { chefMoveImages.put(Direction.DOWN, chefDownMove); System.out.println("✓ chef down move loaded"); }
    if (chefUpIdle != null) { chefIdleImages.put(Direction.UP, chefUpIdle); System.out.println("✓ chef up idle loaded"); }
    if (chefUpMove != null) { chefMoveImages.put(Direction.UP, chefUpMove); System.out.println("✓ chef up move loaded"); }

    // Use side assets for RIGHT and LEFT (LEFT will be drawn flipped)
    if (chefImage != null) { chefIdleImages.put(Direction.RIGHT, chefImage); chefIdleImages.put(Direction.LEFT, chefImage); }
    if (chefMoveGif != null) { chefMoveImages.put(Direction.RIGHT, chefMoveGif); chefMoveImages.put(Direction.LEFT, chefMoveGif); }
        
        // Load station images
        Image tableImg = loadImage("/assets/picture/table.png");
        Image tableTopImg = loadImage("/assets/picture/table top.png");
        Image tableLeftImg = loadImage("/assets/picture/table left.png");
        Image tableRightImg = loadImage("/assets/picture/table right.png");
        Image servingStationImg = loadImage("/assets/picture/serving station.gif");
        
        // Use table.png as default for all stations
        if (tableImg != null) {
            System.out.println("✓ table.png loaded (default for stations)");
            stationImages.put("table", tableImg);
            stationImages.put("cook", tableImg);
        }
        
        // Use serving station image if available
        if (servingStationImg != null) {
            System.out.println("✓ serving station.png loaded");
            stationImages.put("serving", servingStationImg);
        } else {
            stationImages.put("serving", tableImg);
        }
        
        // Load table variants
        if (tableTopImg != null) {
            stationImages.put("tableTop", tableTopImg);
            System.out.println("✓ table top.png loaded");
        }
        if (tableLeftImg != null) {
            stationImages.put("tableLeft", tableLeftImg);
            System.out.println("✓ table left.png loaded");
        }
        if (tableRightImg != null) {
            stationImages.put("tableRight", tableRightImg);
            System.out.println("✓ table right.png loaded");
        }
        
        // Load specific station images using helper
        loadAndRegisterImage("cutting", "/assets/picture/cutting station.png", "cutting station.png");
        loadAndRegisterImage("wash", "/assets/picture/washing station.png", "washing station.png");
        loadAndRegisterImage("rack", "/assets/picture/rack.png", "rack.png");
        loadAndRegisterImage("plateStorage", "/assets/picture/plate storage.png", "plate storage.png");
        loadAndRegisterImage("trash", "/assets/picture/trash station.png", "trash station.png");
        loadAndRegisterImage("cookingStation", "/assets/picture/cooking station.png", "cooking station.png");
        
        // Load ingredient box images
        loadAndRegisterImage("boxCucumber", "/assets/picture/box cucumber.png", "box cucumber.png");
        loadAndRegisterImage("boxFish", "/assets/picture/box fish.png", "box fish.png");
        loadAndRegisterImage("boxNori", "/assets/picture/box nori.png", "box nori.png");
        loadAndRegisterImage("boxRice", "/assets/picture/box rice.png", "box rice.png");
        loadAndRegisterImage("boxShrimp", "/assets/picture/box shrimp.png", "box shrimp.png");
        
        // Load ingredient and item images
        itemImages = new HashMap<>();
        
        // Cucumber
        itemImages.put("cucumber_raw", loadImage("/assets/picture/cucumber.png"));
        itemImages.put("cucumber_chopped", loadImage("/assets/picture/cucumber cut.png"));
        
        // Fish
        itemImages.put("fish_raw", loadImage("/assets/picture/fish raw.png"));
        itemImages.put("fish_chopped", loadImage("/assets/picture/fish cut.png"));
        
        // Nori
        itemImages.put("nori_raw", loadImage("/assets/picture/nori raw.png"));
        
        // Rice
        itemImages.put("rice_raw", loadImage("/assets/picture/rice raw.png"));
        itemImages.put("rice_cooked", loadImage("/assets/picture/rice cooked.png"));
        itemImages.put("rice_burned", loadImage("/assets/picture/rice burned.png"));
        
        // Shrimp
        itemImages.put("shrimp_raw", loadImage("/assets/picture/shrimp raw.png"));
        itemImages.put("shrimp_chopped", loadImage("/assets/picture/shrimp cut.png"));
        itemImages.put("shrimp_cooked", loadImage("/assets/picture/shrimp cooked.png"));
        itemImages.put("shrimp_burned", loadImage("/assets/picture/shrimp burned.png"));
        
        // Load utensil images (for empty utensils)
        itemImages.put("boilingpot_take", loadImage("/assets/picture/boiling pot take empty.png"));
        itemImages.put("fryingpan_empty", loadImage("/assets/picture/frying pan.png"));
        
        // Load boiling pot states with ingredients
        itemImages.put("boilingpot_rice_raw", loadImage("/assets/picture/boiling pot rice raw.png"));
        itemImages.put("boilingpot_rice_cooked", loadImage("/assets/picture/boiling pot rice cooked.png"));
        itemImages.put("boilingpot_rice_burned", loadImage("/assets/picture/boiling pot rice burned.png"));
        
        // Load frying pan states with ingredients
        itemImages.put("fryingpan_shrimp_raw", loadImage("/assets/picture/frying pan shrimp raw.png"));
        itemImages.put("fryingpan_shrimp_cooked", loadImage("/assets/picture/frying pan shrimp cooked.png"));
        itemImages.put("fryingpan_shrimp_burned", loadImage("/assets/picture/frying pan shrimp burned.png"));
        
        // Load plate images
        Image plateImage = loadImage("/assets/picture/plate.png");
        itemImages.put("plate", plateImage);
        Image plateDirtyImage = loadImage("/assets/picture/plate dirty.png");
        itemImages.put("plate_dirty", plateDirtyImage);
        System.out.println("✓ Loaded plate images:");
        System.out.println("  - plate.png: " + (plateImage != null ? "SUCCESS" : "FAILED"));
        System.out.println("  - plate dirty.png: " + (plateDirtyImage != null ? "SUCCESS" : "FAILED"));
        
        // Load plate combination images
        itemImages.put("plate+cucumber", loadImage("/assets/picture/plate+cucumber.png"));
        itemImages.put("plate+fish", loadImage("/assets/picture/plate+fish.png"));
        itemImages.put("plate+nori", loadImage("/assets/picture/plate+nori.png"));
        itemImages.put("plate+rice", loadImage("/assets/picture/plate+rice.png"));
        itemImages.put("plate+shrimp", loadImage("/assets/picture/plate+shrimp.png"));
        itemImages.put("plate+nori+cucumber", loadImage("/assets/picture/plate+nori+cucumber.png"));
        itemImages.put("plate+nori+fish", loadImage("/assets/picture/plate+nori+fish.png"));
        itemImages.put("plate+nori+fish+cucumber", loadImage("/assets/picture/plate+nori+fish+cucumber.png"));
        itemImages.put("plate+nori+rice", loadImage("/assets/picture/plate+nori+rice.png"));
        itemImages.put("plate+nori+shrimp", loadImage("/assets/picture/plate+nori+shrimp.png"));
        itemImages.put("plate+rice+cucumber", loadImage("/assets/picture/plate+rice+cucumber.png"));
        itemImages.put("plate+rice+fish", loadImage("/assets/picture/plate+rice+fish.png"));
        itemImages.put("plate+rice+fish+cucumber", loadImage("/assets/picture/plate+rice+fish+cucumber.png"));
        itemImages.put("plate+rice+shrimp", loadImage("/assets/picture/plate+rice+shrimp.png"));
        System.out.println("✓ Loaded " + 14 + " plate combination images");
        
        // Load completed dish images
        itemImages.put("ebi_maki", loadImage("/assets/picture/ebi maki (plate+nori+rice+shrimp).png"));
        itemImages.put("kappa_maki", loadImage("/assets/picture/kappa maki (plate+nori+rice+timun).png"));
        itemImages.put("sakana_maki", loadImage("/assets/picture/sakana maki (plate+rice+nori+fish).png"));
        itemImages.put("fish_cucumber_roll", loadImage("/assets/picture/fish cucumber roll (plate+nori+fish+cucumber+rice).png"));
        System.out.println("✓ Loaded completed dish images");
        
        // Load boiling pot fill GIF
        boilingPotFillGif = loadImage("/assets/picture/boiling pot fill.gif");
        if (boilingPotFillGif != null) {
            System.out.println("✓ Loaded boiling pot fill GIF");
        }
        
        // Note: Plates, dishes, and filled utensils will be rendered with overlays
        // For now, we use ingredient images to show what's in them
        
        System.out.println("✓ Loaded " + itemImages.size() + " ingredient/item images");
        
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
    
    /**
     * Helper method to load and register an image in one call
     */
    private void loadAndRegisterImage(String key, String path, String displayName) {
        Image img = loadImage(path);
        if (img != null) {
            stationImages.put(key, img);
            System.out.println("✓ " + displayName + " loaded");
        }
    }
    
    /**
     * Get the correct chef image based on movement state
     * @param chef The chef entity
     * @return The appropriate Image (GIF if moving, PNG if idle)
     */
    private Image getChefImage(Chef chef) {
        Boolean isMoving = chefIsMoving.getOrDefault(chef, false);
        Direction dir = chef.getDirection();

        // Prefer directional images if available
        if (isMoving) {
            Image dirMove = chefMoveImages.get(dir);
            if (dirMove != null) return dirMove;
            // fallback to generic move gif
            if (chefMoveGif != null) return chefMoveGif;
        } else {
            Image dirIdle = chefIdleImages.get(dir);
            if (dirIdle != null) return dirIdle;
            // fallback to generic idle
            if (chefImage != null) return chefImage;
        }

        // Final fallback
        return chefImage != null ? chefImage : chefMoveGif;
    }
    
    /**
     * Apply directional transformation to chef image based on facing direction
     * @param gc GraphicsContext to draw on
     * @param image The chef image to draw
     * @param x The x coordinate
     * @param y The y coordinate
     * @param size The size of the chef sprite
     * @param direction The direction the chef is facing
     */
    private void drawChefWithDirection(GraphicsContext gc, Image image, double x, double y, double size, Direction direction) {
        if (image == null) return;
        
        // Save the current transform
        gc.save();
        
        switch (direction) {
            case RIGHT:
                // Normal - no transformation needed
                gc.drawImage(image, x, y, size, size);
                break;
                
            case LEFT:
                // Flip horizontally
                gc.translate(x + size, y);  // Move to right edge
                gc.scale(-1, 1);             // Flip horizontally
                gc.drawImage(image, 0, 0, size, size);
                break;
                
            case UP:
                // Up assets should already be oriented upwards - no rotation
                gc.drawImage(image, x, y, size, size);
                break;

            case DOWN:
                // Down assets should already be oriented downwards - no rotation
                gc.drawImage(image, x, y, size, size);
                break;
                
            default:
                gc.drawImage(image, x, y, size, size);
        }
        
        // Restore the transform
        gc.restore();
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
                
                // Scale tile size
                tileSize = tileSize * GameConfig.TILE_SIZE_SCALE; 
            }
            
            System.out.println("Map loaded: " + stageId);
            System.out.println("Map size: " + tileManager.getWidth() + "x" + tileManager.getHeight());
            System.out.println("Tile size: " + tileSize);
            System.out.println("Spawn positions: " + spawnPositions.size());
            
            // Create 2 chefs at different spawn positions
            if (!spawnPositions.isEmpty()) {
                // Chef 1
                Position spawnPos1 = spawnPositions.get(0);
                playerChef = new Chef("player1", "Chef 1", spawnPos1, Direction.RIGHT);
                chefIsMoving.put(playerChef, false); // Chef 1 awalnya idle
                
                // Place chef 1 on tile
                Tile spawnTile1 = tileManager.getTileAt(spawnPos1);
                if (spawnTile1 != null) {
                    spawnTile1.setChefOnTile(playerChef);
                }
                
                System.out.println("Chef 1 spawned at: (" + spawnPos1.getX() + ", " + spawnPos1.getY() + ")");
                
                // Chef 2 - gunakan spawn position kedua jika ada, atau posisi sebelah chef 1
                Position spawnPos2;
                if (spawnPositions.size() > 1) {
                    spawnPos2 = spawnPositions.get(1);
                } else {
                    // Spawn di sebelah chef 1 jika hanya ada 1 spawn point
                    spawnPos2 = new Position(spawnPos1.getX() + 1, spawnPos1.getY());
                    // Validasi posisi
                    if (!tileManager.isInBounds(spawnPos2) || !tileManager.isWalkable(spawnPos2)) {
                        spawnPos2 = new Position(spawnPos1.getX(), spawnPos1.getY() + 1);
                    }
                }
                
                chef2 = new Chef("player2", "Chef 2", spawnPos2, Direction.RIGHT);
                chefIsMoving.put(chef2, false); // Chef 2 awalnya idle
                
                // Place chef 2 on tile
                Tile spawnTile2 = tileManager.getTileAt(spawnPos2);
                if (spawnTile2 != null) {
                    spawnTile2.setChefOnTile(chef2);
                }
                
                System.out.println("Chef 2 spawned at: (" + spawnPos2.getX() + ", " + spawnPos2.getY() + ")");
                
                // Set active chef (awalnya chef 1)
                activeChef = playerChef;
                
                // Initialize smooth position untuk active chef
                chefRenderX = spawnPos1.getX();
                chefRenderY = spawnPos1.getY();
                chefTargetX = spawnPos1.getX();
                chefTargetY = spawnPos1.getY();
            }
        } catch (Exception e) {
            System.err.println("Failed to load map: " + stageId);
            e.printStackTrace();
            
            // Create empty fallback
            Tile[][] emptyTiles = new Tile[10][10];
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 10; x++) {
                    // Anda perlu memiliki constructor Tile(Position, boolean isWall)
                    // emptyTiles[y][x] = new Tile(new Position(x, y), false); 
                }
            }
            // this.tileManager = new TileManager(10, 10, emptyTiles);
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
        // Check if game is over
        if (gameState.isGameOver()) {
            showResultScreen();
            return;
        }
        
        // Update game state
        gameState.update();
        
        // Skip update jika game di-pause
        if (isPaused) {
            return;
        }
        
        // Update order manager
        if (orderManager != null) {
            orderManager.update(deltaTime);
            long currentTime = System.currentTimeMillis();
            orderManager.trySpawnNewOrder(currentTime);
        }
        
        // Handle chef movement
        handleChefMovement(System.nanoTime());
        
        // Smooth chef position interpolation (untuk active chef)
        if (activeChef != null) {
            double dx = chefTargetX - chefRenderX;
            double dy = chefTargetY - chefRenderY;
            
            if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) {
                chefRenderX += dx * GameConfig.MOVE_SPEED;
                chefRenderY += dy * GameConfig.MOVE_SPEED;
            } else {
                chefRenderX = chefTargetX;
                chefRenderY = chefTargetY;
                
                // Chef telah selesai bergerak, set to idle state
                chefIsMoving.put(activeChef, false);
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
        
        // Ensure both chefs are tracked on their tiles (for throw catching detection)
        if (playerChef != null) {
            ensureChefTileTracking(playerChef);
        }
        if (chef2 != null) {
            ensureChefTileTracking(chef2);
        }
        
        // Calculate map dimensions
        double mapWidth = tileManager.getWidth() * tileSize;
        double mapHeight = tileManager.getHeight() * tileSize;
        
        // Offset X: Tetap di tengah
        double offsetX = (WINDOW_WIDTH - mapWidth) / 2; 
        
        // Offset Y: Letakkan map 40px dari tepi atas (gap kecil)
        double mapTopMargin = 40; 
        double offsetY = mapTopMargin;
        
        // --- START: RENDERING MAP TILES DAN STATIONS ---
        Tile[][] tiles = tileManager.getTiles();
        for (int y = 0; y < tileManager.getHeight(); y++) {
            for (int x = 0; x < tileManager.getWidth(); x++) {
                Tile tile = tiles[y][x];
                if (tile == null) continue;
                
                double screenX = offsetX + x * tileSize;
                double screenY = offsetY + y * tileSize;
                
                // 1. Draw tile background (Floor/Wall)
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
                
                // 2. Draw station on top of floor
                Station station = tile.getStation();
                if (station != null) {
                    Image stationImg = getStationImage(station);
                    if (stationImg != null) {
                        gc.drawImage(stationImg, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Fallback to colored rectangle (Orange)
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
                    
                    // Draw items on stations
                    renderItemsOnStation(station, screenX, screenY, tileSize);
            
                } else {
                    // Render items dropped on floor (no station)
                    if (tile.getItemOnTile() != null) {
                        renderItemOnTile(tile.getItemOnTile(), screenX, screenY, tileSize);
                    }
                }
            }
        }
        
        // --- DRAW CHEFS ---
        if (playerChef != null) {
            drawChef(playerChef, offsetX, offsetY, playerChef == activeChef);
        }
        if (chef2 != null) {
            drawChef(chef2, offsetX, offsetY, chef2 == activeChef);
        }
        // --- END: RENDERING MAP ---

        // --- RENDER HUD STATUS DAN LOG DI BAGIAN BAWAH MAP ---
        
        // Posisi X Mutlak: 20px dari tepi kiri layar
        double fixedLogX = 20;
        
        // Posisi Y Mutlak Awal: Di bawah map, +30px spacing
        double hudYStart = offsetY + mapHeight + 30; 
        
        // 1. Status Tangan (Hand: Item)
        gc.setTextAlign(TextAlignment.LEFT);
        
        if (activeChef != null) {
            String handContent = activeChef.getInventory() != null ? 
                                activeChef.getInventory().getName() : 
                                "Kosong";
            
            gc.setFill(Color.web("#F2C38F")); 
            gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
            gc.fillText("Hand: " + handContent, fixedLogX, hudYStart);
        }
        
        // 2. Log Aksi (Stack dari Bawah)
        
        // Y awal untuk log (20px di bawah status tangan)
        double logYStart = hudYStart + 20; 
        
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, 16));
        
        // Log terbaru ada di index 0 (ingin log terbaru di BARIS PALING BAWAH)
        for (int i = 0; i < onScreenLogs.size(); i++) {
            String logMsg = onScreenLogs.get(i);
            
            // Stack ke bawah (logYStart + 20*i)
            double drawY = logYStart + i * 20; 
            
            gc.setFill(Color.WHITE); 
            gc.fillText(logMsg, fixedLogX, drawY);
        }
        // -----------------------------------------------------
        
        // Render game UI (Score and Timer)
        renderGameUI();

        // Render orders on left side (vertical layout)
        if (orderManager != null) {
            // Tempatkan order panel di kiri atas, di bawah margin
            double orderPanelY = 20; // Top margin
            OrderDisplay.renderOrders(gc, orderManager.getActiveOrders(), WINDOW_WIDTH, orderPanelY, itemImages);
        }
        
        // Render pause menu if paused
        if (isPaused) {
            renderPauseMenu();
        }

    }

    private Image getStationImage(Station station) {
        if (station instanceof CookingStation) {
            // R = Cooking Station
            return stationImages.getOrDefault("cookingStation", stationImages.get("cook"));
        } else if (station instanceof CuttingStation) {
            // C = Cutting Station - gunakan asset khusus
            return stationImages.getOrDefault("cutting", stationImages.get("table"));
        } else if (station instanceof AssemblyStation) {
            // A = Assembly Station - use table top
            Image tableTop = stationImages.get("tableTop");
            return tableTop != null ? tableTop : stationImages.get("table");
        } else if (station instanceof ServingStation) {
            // S = Serving Counter
            return stationImages.get("serving");
        } else if (station instanceof WashingStation) {
            // W = Washing Station
            return stationImages.getOrDefault("wash", stationImages.get("table"));
        } else if (station instanceof nimons.entity.station.Rack) {
            // K = Rack
            return stationImages.getOrDefault("rack", stationImages.get("table"));
        } else if (station instanceof IngredientStorageStation) {
            // I = Ingredient Storage - use specific box images
            IngredientStorageStation iss = (IngredientStorageStation) station;
            nimons.entity.item.Item storedItem = iss.getStoredItem();
            
            if (storedItem != null) {
                String itemName = storedItem.getName().toLowerCase();
                if (itemName.contains("cucumber") || itemName.contains("timun")) {
                    return stationImages.getOrDefault("boxCucumber", stationImages.get("table"));
                } else if (itemName.contains("fish") || itemName.contains("ikan")) {
                    return stationImages.getOrDefault("boxFish", stationImages.get("table"));
                } else if (itemName.contains("nori")) {
                    return stationImages.getOrDefault("boxNori", stationImages.get("table"));
                } else if (itemName.contains("rice") || itemName.contains("beras")) {
                    return stationImages.getOrDefault("boxRice", stationImages.get("table"));
                } else if (itemName.contains("shrimp") || itemName.contains("udang")) {
                    return stationImages.getOrDefault("boxShrimp", stationImages.get("table"));
                }
            }
            return stationImages.get("table");
        } else if (station instanceof PlateStorageStation) {
            // P = Plate Storage
            return stationImages.getOrDefault("plateStorage", stationImages.get("table"));
        } else if (station instanceof nimons.entity.station.TrashStation) {
            // T = Trash Station - gunakan asset khusus
            return stationImages.getOrDefault("trash", stationImages.get("table"));
        }
        return null;
    }
    
    /**
     * Get image for an item based on its type and state
     */
    private Image getItemImage(nimons.entity.item.Item item) {
        if (item == null) return null;
        
        String itemName = item.getName().toLowerCase();
        
        // Check for utensils (BoilingPot, FryingPan)
        if (item instanceof nimons.entity.item.KitchenUtensil) {
            nimons.entity.item.KitchenUtensil utensil = (nimons.entity.item.KitchenUtensil) item;
            
            // If utensil has contents, show utensil with ingredient state
            if (utensil.getContents() != null && !utensil.getContents().isEmpty()) {
                nimons.entity.item.interfaces.Preparable firstItem = utensil.getContents().iterator().next();
                if (firstItem instanceof nimons.entity.item.Ingredient) {
                    nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) firstItem;
                    String ingName = ing.getName().toLowerCase();
                    nimons.entity.item.IngredientState state = ing.getState();
                    
                    // Boiling Pot with Rice
                    if (itemName.contains("boiling") || itemName.contains("pot")) {
                        if (ingName.contains("rice") || ingName.contains("beras") || ingName.contains("nasi")) {
                            if (state == nimons.entity.item.IngredientState.BURNED) {
                                return itemImages.get("boilingpot_rice_burned");
                            } else if (state == nimons.entity.item.IngredientState.COOKED) {
                                return itemImages.get("boilingpot_rice_cooked");
                            } else {
                                return itemImages.get("boilingpot_rice_raw");
                            }
                        }
                    }
                    // Frying Pan with Shrimp
                    else if (itemName.contains("frying") || itemName.contains("pan")) {
                        if (ingName.contains("shrimp") || ingName.contains("udang")) {
                            if (state == nimons.entity.item.IngredientState.BURNED) {
                                return itemImages.get("fryingpan_shrimp_burned");
                            } else if (state == nimons.entity.item.IngredientState.COOKED) {
                                return itemImages.get("fryingpan_shrimp_cooked");
                            } else {
                                return itemImages.get("fryingpan_shrimp_raw");
                            }
                        }
                    }
                }
            }
            
            // Empty utensil - return image for floor/station display
            if (itemName.contains("boiling") || itemName.contains("pot")) {
                return itemImages.get("boilingpot_take");
            } else if (itemName.contains("frying") || itemName.contains("pan")) {
                return itemImages.get("fryingpan_empty");
            }
        }
        
        // Check for plates with dishes
        if (item instanceof nimons.entity.item.Plate) {
            nimons.entity.item.Plate plate = (nimons.entity.item.Plate) item;
            
            // If plate has food, determine combination image
            if (plate.getFood() != null) {
                nimons.entity.item.Dish dish = plate.getFood();
                
                // Check if it's a completed dish first
                String dishImage = getCompletedDishImage(dish);
                if (dishImage != null) {
                    Image img = itemImages.get(dishImage);
                    if (img != null) {
                        return img;
                    }
                }
                
                // Otherwise, get plate combination based on components
                String comboKey = getPlateCombinationKey(dish);
                if (comboKey != null) {
                    Image img = itemImages.get(comboKey);
                    if (img != null) {
                        return img;
                    } else {
                        // Only log when image is missing
                        System.out.println("WARNING: Plate combo image not found for key: " + comboKey);
                    }
                }
                
                // Fallback: if no specific combo image, return base plate
                return itemImages.get("plate");
            }
            
            // Dirty plate vs clean plate
            if (!plate.isClean()) {
                Image dirtyPlate = itemImages.get("plate_dirty");
                if (dirtyPlate != null) {
                    return dirtyPlate;
                }
            }
            
            // Empty clean plate
            Image emptyPlateImg = itemImages.get("plate");
            if (emptyPlateImg == null) {
                System.out.println("ERROR: Empty plate image is NULL!");
            }
            return emptyPlateImg;
        }
        
        // Check for ingredients with states
        if (item instanceof nimons.entity.item.Ingredient) {
            nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) item;
            nimons.entity.item.IngredientState state = ing.getState();
            
            if (itemName.contains("cucumber") || itemName.contains("timun")) {
                if (state == nimons.entity.item.IngredientState.CHOPPED) {
                    return itemImages.get("cucumber_chopped");
                }
                return itemImages.get("cucumber_raw");
            } else if (itemName.contains("fish") || itemName.contains("ikan")) {
                if (state == nimons.entity.item.IngredientState.CHOPPED) {
                    return itemImages.get("fish_chopped");
                }
                return itemImages.get("fish_raw");
            } else if (itemName.contains("nori")) {
                return itemImages.get("nori_raw");
            } else if (itemName.contains("rice") || itemName.contains("beras") || itemName.contains("nasi")) {
                if (state == nimons.entity.item.IngredientState.BURNED) {
                    return itemImages.get("rice_burned");
                } else if (state == nimons.entity.item.IngredientState.COOKED) {
                    return itemImages.get("rice_cooked");
                }
                return itemImages.get("rice_raw");
            } else if (itemName.contains("shrimp") || itemName.contains("udang")) {
                if (state == nimons.entity.item.IngredientState.BURNED) {
                    return itemImages.get("shrimp_burned");
                } else if (state == nimons.entity.item.IngredientState.COOKED) {
                    return itemImages.get("shrimp_cooked");
                } else if (state == nimons.entity.item.IngredientState.CHOPPED) {
                    return itemImages.get("shrimp_chopped");
                }
                return itemImages.get("shrimp_raw");
            }
        }
        
        return null;
    }
    
    /**
     * Get completed dish image key based on dish name
     */
    private String getCompletedDishImage(nimons.entity.item.Dish dish) {
        if (dish == null || dish.getName() == null) {
            return null;
        }
        
        String dishName = dish.getName().toLowerCase();
        
        if (dishName.contains("ebi maki")) {
            return "ebi_maki";
        } else if (dishName.contains("kappa maki")) {
            return "kappa_maki";
        } else if (dishName.contains("sakana maki")) {
            return "sakana_maki";
        } else if (dishName.contains("fish cucumber roll")) {
            return "fish_cucumber_roll";
        }
        
        return null;
    }
    
    /**
     * Get utensil state image based on contents and cooking state
     */
    private Image getUtensilStateImage(nimons.entity.item.KitchenUtensil utensil) {
        if (utensil == null) {
            return null;
        }
        
        String utensilName = utensil.getName().toLowerCase();
        
        // Check if utensil has contents
        if (utensil.getContents() != null && !utensil.getContents().isEmpty()) {
            nimons.entity.item.interfaces.Preparable firstItem = utensil.getContents().iterator().next();
            if (firstItem instanceof nimons.entity.item.Ingredient) {
                nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) firstItem;
                String ingName = ing.getName().toLowerCase();
                nimons.entity.item.IngredientState state = ing.getState();
                
                // Boiling Pot with Rice
                if (utensilName.contains("boiling") || utensilName.contains("pot")) {
                    if (ingName.contains("rice") || ingName.contains("beras") || ingName.contains("nasi")) {
                        if (state == nimons.entity.item.IngredientState.BURNED) {
                            return itemImages.get("boilingpot_rice_burned");
                        } else if (state == nimons.entity.item.IngredientState.COOKED) {
                            return itemImages.get("boilingpot_rice_cooked");
                        } else {
                            return itemImages.get("boilingpot_rice_raw");
                        }
                    }
                }
                // Frying Pan with Shrimp
                else if (utensilName.contains("frying") || utensilName.contains("pan")) {
                    if (ingName.contains("shrimp") || ingName.contains("udang")) {
                        if (state == nimons.entity.item.IngredientState.BURNED) {
                            return itemImages.get("fryingpan_shrimp_burned");
                        } else if (state == nimons.entity.item.IngredientState.COOKED) {
                            return itemImages.get("fryingpan_shrimp_cooked");
                        } else {
                            return itemImages.get("fryingpan_shrimp_raw");
                        }
                    }
                }
            }
        }
        
        // Empty utensil
        if (utensilName.contains("boiling") || utensilName.contains("pot")) {
            return itemImages.get("boilingpot_take");
        } else if (utensilName.contains("frying") || utensilName.contains("pan")) {
            return itemImages.get("fryingpan_empty");
        }
        
        return null;
    }
    
    /**
     * Get plate combination key based on dish components (order-independent)
     */
    private String getPlateCombinationKey(nimons.entity.item.Dish dish) {
        if (dish == null || dish.getComponents() == null || dish.getComponents().isEmpty()) {
            return null;
        }
        
        // Collect ingredient names (normalized)
        boolean hasRice = false;
        boolean hasNori = false;
        boolean hasCucumber = false;
        boolean hasFish = false;
        boolean hasShrimp = false;
        
        // Debug: print all components
        StringBuilder debugComps = new StringBuilder("Plate components: ");
        for (nimons.entity.item.interfaces.Preparable comp : dish.getComponents()) {
            if (comp instanceof nimons.entity.item.Item) {
                String compName = ((nimons.entity.item.Item) comp).getName();
                debugComps.append(compName).append(", ");
                
                String compNameLower = compName.toLowerCase();
                if (compNameLower.contains("rice") || compNameLower.contains("beras") || compNameLower.contains("nasi")) {
                    hasRice = true;
                } else if (compNameLower.contains("nori")) {
                    hasNori = true;
                } else if (compNameLower.contains("cucumber") || compNameLower.contains("timun")) {
                    hasCucumber = true;
                } else if (compNameLower.contains("fish") || compNameLower.contains("ikan")) {
                    hasFish = true;
                } else if (compNameLower.contains("shrimp") || compNameLower.contains("udang")) {
                    hasShrimp = true;
                }
            }
        }
        System.out.println(debugComps.toString());
        
        // Build key based on components (alphabetical order for consistency)
        // Check 4-ingredient combinations first
        if (hasNori && hasRice && hasFish && hasCucumber) {
            return "plate+nori+fish+cucumber"; // Note: image name doesn't have rice but it's implied
        }
        
        // Check 3-ingredient combinations
        if (hasNori && hasFish && hasCucumber) {
            return "plate+nori+fish+cucumber";
        }
        if (hasRice && hasFish && hasCucumber) {
            return "plate+rice+fish+cucumber";
        }
        if (hasNori && hasRice && hasShrimp) {
            return "plate+nori+rice"; // Using nori+rice as base, shrimp shown separately
        }
        if (hasNori && hasRice && hasCucumber) {
            return "plate+nori+rice"; // Using nori+rice as base
        }
        if (hasNori && hasRice && hasFish) {
            return "plate+nori+rice"; // Using nori+rice as base
        }
        if (hasRice && hasFish) {
            return "plate+rice+fish";
        }
        if (hasRice && hasCucumber) {
            return "plate+rice+cucumber";
        }
        if (hasRice && hasShrimp) {
            return "plate+rice+shrimp";
        }
        if (hasNori && hasFish) {
            return "plate+nori+fish";
        }
        if (hasNori && hasShrimp) {
            return "plate+nori+shrimp";
        }
        if (hasNori && hasCucumber) {
            return "plate+nori+cucumber";
        }
        if (hasNori && hasRice) {
            return "plate+nori+rice";
        }
        
        // Check 2-ingredient combinations
        if (hasRice) {
            return "plate+rice";
        }
        if (hasNori) {
            return "plate+nori";
        }
        if (hasCucumber) {
            return "plate+cucumber";
        }
        if (hasFish) {
            return "plate+fish";
        }
        if (hasShrimp) {
            return "plate+shrimp";
        }
        
        return null;
    }
    
    /**
     * Render cutting progress bar (yellow for cutting)
     */
    private void renderCuttingProgressBar(CuttingStation cs, double screenX, double screenY, double tileSize) {
        float progressRatio = cs.getProgressRatio();
        if (progressRatio <= 0) {
            return;
        }
        
        // Progress bar dimensions
        double barWidth = tileSize * 0.6;
        double barHeight = tileSize * 0.08;
        double barX = screenX + (tileSize - barWidth) / 2;
        double barY = screenY + tileSize - barHeight - 5; // Bottom of tile
        
        // Draw background (dark)
        gc.setFill(Color.web("#333333"));
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw progress (yellow for cutting)
        gc.setFill(Color.web("#FFD700")); // Gold/Yellow
        gc.fillRect(barX, barY, barWidth * progressRatio, barHeight);
        
        // Draw border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * Render washing progress bar (blue for washing)
     */
    private void renderWashingProgressBar(WashingStation ws, double screenX, double screenY, double tileSize) {
        float progressRatio = ws.getProgressRatio();
        if (progressRatio <= 0) {
            return;
        }
        
        // Progress bar dimensions
        double barWidth = tileSize * 0.6;
        double barHeight = tileSize * 0.08;
        double barX = screenX + (tileSize - barWidth) / 2;
        double barY = screenY + tileSize - barHeight - 5; // Bottom of tile
        
        // Draw background (dark)
        gc.setFill(Color.web("#333333"));
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw progress (blue for washing)
        gc.setFill(Color.web("#00AAFF")); // Cyan/Blue
        gc.fillRect(barX, barY, barWidth * progressRatio, barHeight);
        
        // Draw border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * Render cooking progress bar (green for COOKING, red for BURNED countdown)
     */
    private void renderCookingProgressBar(CookingStation cs, double screenX, double screenY, double tileSize) {
        if (cs.getUtensils() == null || cs.getUtensils().getContents().isEmpty()) {
            return;
        }
        
        // Get the first ingredient being cooked
        Preparable prep = cs.getUtensils().getContents().iterator().next();
        if (!(prep instanceof nimons.entity.item.Ingredient)) {
            return;
        }
        
        nimons.entity.item.Ingredient ingredient = (nimons.entity.item.Ingredient) prep;
        nimons.entity.item.IngredientState state = ingredient.getState();
        
        // Only show progress bar for COOKING and COOKED states (leading to BURNED)
        if (state != nimons.entity.item.IngredientState.COOKING && 
            state != nimons.entity.item.IngredientState.COOKED) {
            return;
        }
        
        // Progress bar dimensions
        double barWidth = tileSize * 0.6;
        double barHeight = tileSize * 0.08;
        double barX = screenX + (tileSize - barWidth) / 2;
        double barY = screenY + tileSize - barHeight - 5; // Bottom of tile
        
        // Draw background (dark)
        gc.setFill(Color.web("#333333"));
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw progress based on state
        if (state == nimons.entity.item.IngredientState.COOKING) {
            // GREEN progress bar for COOKING phase
            float requiredTime = ingredient.getRequiredCookingTime();
            float currentTime = ingredient.getCurrentCookingTime();
            float ratio = requiredTime > 0 ? Math.min(1.0f, currentTime / requiredTime) : 0;
            
            gc.setFill(Color.web("#00AA00")); // Green
            gc.fillRect(barX, barY, barWidth * ratio, barHeight);
            
        } else if (state == nimons.entity.item.IngredientState.COOKED) {
            // YELLOW progress bar for BURNED countdown (from COOKED to BURNED)
            float requiredCookTime = ingredient.getRequiredCookingTime();
            float totalTime = ingredient.getTotalCookingTime(); // Use total time including burn phase
            
            // Calculate how much time has passed in the burn phase
            // totalTime continues past requiredCookTime during burn phase
            float burnPhaseElapsed = Math.max(0, totalTime - requiredCookTime);
            float burnPhaseTotal = (float) GameConfig.TIME_TO_BURN_MS;
            float burnRatio = burnPhaseTotal > 0 ? Math.min(1.0f, burnPhaseElapsed / burnPhaseTotal) : 0;
            
            // Debug log
            System.out.println("[BURN] totalTime=" + totalTime + ", required=" + requiredCookTime + 
                             ", elapsed=" + burnPhaseElapsed + ", total=" + burnPhaseTotal + ", ratio=" + burnRatio);
            
            // YELLOW bar fills up as it approaches BURNED state
            gc.setFill(Color.web("#FFD700")); // Yellow/Gold
            gc.fillRect(barX, barY, barWidth * burnRatio, barHeight);
        }
        
        // Draw border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * Render items on stations (ingredients on cutting boards, items on assembly, etc.)
     */
    private void renderItemsOnStation(Station station, double screenX, double screenY, double tileSize) {
        if (station == null) return;
        
        // Render items on CuttingStation
        if (station instanceof CuttingStation) {
            CuttingStation cs = (CuttingStation) station;
            nimons.entity.item.Item placedItem = cs.getPlacedItem();
            if (placedItem != null) {
                renderItemOnTile(placedItem, screenX, screenY, tileSize);
                
                // Render cutting progress bar
                if (cs.isActive()) {
                    renderCuttingProgressBar(cs, screenX, screenY, tileSize);
                }
            }
        }
        
        // Render items on CookingStation (utensils with ingredients)
        if (station instanceof CookingStation) {
            CookingStation cs = (CookingStation) station;
            nimons.entity.item.KitchenUtensil utensil = cs.getUtensils();
            
            if (utensil != null) {
                // Utensil exists on station - render the utensil image as base layer
                boolean hasContents = utensil.getContents() != null && !utensil.getContents().isEmpty();
                
                // Check cooking states
                boolean isStillCooking = false;
                boolean isInBurnPhase = false;
                
                if (hasContents) {
                    for (nimons.entity.item.interfaces.Preparable prep : utensil.getContents()) {
                        if (prep instanceof nimons.entity.item.Ingredient) {
                            nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) prep;
                            if (ing.getState() == nimons.entity.item.IngredientState.COOKING) {
                                isStillCooking = true;
                            }
                            if (ing.getState() == nimons.entity.item.IngredientState.COOKED) {
                                isInBurnPhase = true;
                            }
                        }
                    }
                    
                    // Layer 1: Show utensil with ingredient state
                    Image utensilImg = getUtensilStateImage(utensil);
                    if (utensilImg != null) {
                        double itemSize = tileSize * 0.5;
                        double itemX = screenX + (tileSize - itemSize) / 2;
                        double itemY = screenY + (tileSize - itemSize) / 2 - tileSize * 0.1; // Raised position
                        gc.drawImage(utensilImg, itemX, itemY, itemSize, itemSize);
                    }
                    
                    // Render cooking progress bar if cooking OR in burn phase
                    if (isStillCooking || isInBurnPhase) {
                        renderCookingProgressBar(cs, screenX, screenY, tileSize);
                    }
                } else {
                    // Empty utensil on station - show the empty utensil image
                    String utensilName = utensil.getName().toLowerCase();
                    Image emptyImg = null;
                    if (utensilName.contains("boiling") || utensilName.contains("pot")) {
                        emptyImg = itemImages.get("boilingpot_take");
                    } else if (utensilName.contains("frying") || utensilName.contains("pan")) {
                        emptyImg = itemImages.get("fryingpan_empty");
                    }
                    if (emptyImg != null) {
                        double itemSize = tileSize * 0.5;
                        double itemX = screenX + (tileSize - itemSize) / 2;
                        double itemY = screenY + (tileSize - itemSize) / 2 - tileSize * 0.1; // Raised position
                        gc.drawImage(emptyImg, itemX, itemY, itemSize, itemSize);
                    }
                }
            }
            // If utensil == null, only cooking station base image is shown (no overlay)
        }
        
        // Render items on AssemblyStation
        if (station instanceof nimons.entity.station.AssemblyStation) {
            nimons.entity.station.AssemblyStation as = (nimons.entity.station.AssemblyStation) station;
            nimons.entity.item.Item placedItem = as.getPlacedItem();
            if (placedItem != null) {
                renderItemOnTile(placedItem, screenX, screenY, tileSize);
            }
        }
        
        // Render plate stack on PlateStorageStation
        if (station instanceof PlateStorageStation) {
            PlateStorageStation pss = (PlateStorageStation) station;
            int plateCount = pss.getPlateCount();
            
            if (plateCount > 0) {
                Image plateImg = itemImages.get("plate");
                if (plateImg != null) {
                    // Draw stack of plates with larger size and offset to show depth
                    double plateSize = tileSize * 0.6; // Increased from 0.4 to 0.6
                    double plateX = screenX + (tileSize - plateSize) / 2;
                    double baseY = screenY + (tileSize - plateSize) / 2 - tileSize * 0.05; // Raised position (was +0.15, now -0.05)
                    
                    // Draw up to 4 plates in stack (visual representation)
                    int visualCount = Math.min(plateCount, 4);
                    double stackOffset = 5; // Increased from 3 to 5 pixels per plate
                    
                    for (int i = 0; i < visualCount; i++) {
                        double plateY = baseY - (i * stackOffset);
                        gc.drawImage(plateImg, plateX, plateY, plateSize, plateSize);
                    }
                    
                    // Draw count text if more than 4 plates
                    if (plateCount > 4) {
                        gc.setFill(Color.WHITE);
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(2);
                        gc.setFont(javafx.scene.text.Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, tileSize * 0.3));
                        String countText = String.valueOf(plateCount);
                        double textX = screenX + tileSize * 0.7;
                        double textY = screenY + tileSize * 0.35;
                        gc.strokeText(countText, textX, textY);
                        gc.fillText(countText, textX, textY);
                    }
                }
            }
        }
        
        // Render washing progress bar on WashingStation
        if (station instanceof WashingStation) {
            WashingStation ws = (WashingStation) station;
            
            // Render washing progress bar for SINK
            if (ws.isActive()) {
                renderWashingProgressBar(ws, screenX, screenY, tileSize);
            }
        }
        
        // Render clean plate stack on Rack
        if (station instanceof nimons.entity.station.Rack) {
            nimons.entity.station.Rack rack = (nimons.entity.station.Rack) station;
            
            // Render clean plate stack
            int plateCount = rack.getCleanPlateCount();
            if (plateCount > 0) {
                Image plateImg = itemImages.get("plate");
                if (plateImg != null) {
                    // Draw stack of clean plates (similar to PlateStorageStation)
                    double plateSize = tileSize * 0.6;
                    double plateX = screenX + (tileSize - plateSize) / 2;
                    double baseY = screenY + (tileSize - plateSize) / 2 - tileSize * 0.05;
                    
                    // Draw up to 4 plates in stack
                    int visualCount = Math.min(plateCount, 4);
                    double stackOffset = 5;
                    
                    for (int i = 0; i < visualCount; i++) {
                        double plateY = baseY - (i * stackOffset);
                        gc.drawImage(plateImg, plateX, plateY, plateSize, plateSize);
                    }
                    
                    // Draw count text if more than 4 plates
                    if (plateCount > 4) {
                        gc.setFill(Color.WHITE);
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(2);
                        gc.setFont(javafx.scene.text.Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, tileSize * 0.3));
                        String countText = String.valueOf(plateCount);
                        double textX = screenX + tileSize * 0.7;
                        double textY = screenY + tileSize * 0.35;
                        gc.strokeText(countText, textX, textY);
                        gc.fillText(countText, textX, textY);
                    }
                }
            }
        }
        
        // Render items on IngredientStorageStation
        if (station instanceof IngredientStorageStation) {
            IngredientStorageStation iss = (IngredientStorageStation) station;
            nimons.entity.item.Item placedItem = iss.getPlacedItem();
            if (placedItem != null) {
                renderItemOnTile(placedItem, screenX, screenY, tileSize);
            }
        }
    }
    
    /**
     * Helper method to render any item on a tile/station, with special handling for plates
     */
    private void renderItemOnTile(nimons.entity.item.Item item, double screenX, double screenY, double tileSize) {
        if (item == null) return;
        
        // Use getItemImage which now handles all special cases
        Image itemImg = getItemImage(item);
        
        if (itemImg != null) {
            double itemSize = tileSize * 0.6;
            double itemX = screenX + (tileSize - itemSize) / 2;
            double itemY = screenY + (tileSize - itemSize) / 2;
            gc.drawImage(itemImg, itemX, itemY, itemSize, itemSize);
        } else {
            System.out.println("WARNING: No image found for item: " + item.getName() + " (type: " + item.getClass().getSimpleName() + ")");
        }
    }

    public void start() {
        // Reset game state and order manager
        gameState.reset();
        orderManager.reset();
        
        // Clear logs
        onScreenLogs.clear();
        
        // Reset move and pause flags
        isPaused = false;
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
        shiftPressed = false;
        
        // Initialize concurrency components
        taskExecutor = GameTaskExecutor.getInstance();
        orderGeneratorTask = new OrderGeneratorTask(orderManager, GameConfig.ORDER_SPAWN_INTERVAL_MS);
        orderGeneratorThread = new Thread(orderGeneratorTask, "OrderGenerator");
        orderGeneratorThread.setDaemon(true); // Daemon thread will stop when main program exits
        orderGeneratorThread.start();
        addLog("✓ Background order generation started");
        
        Scene scene = new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/mainmenu.css").toExternalForm());
        
        // Initialize OrderManager dengan available recipes untuk sushi stage
        List<Recipe> recipes = createDummyRecipes();
        orderManager.setAvailableRecipes(recipes);
        
        // Setup keyboard controls
        setupKeyboardControls(scene);
        
        // Setup mouse controls for pause menu
        setupMouseControls(scene);
        
        stage.setScene(scene);
        stage.setTitle("Nimonscooked - Game");
        gameStartTime = System.currentTimeMillis();
        
        // Stop main menu music when game starts
        SoundManager.getInstance().stopMusic();
        
        gameLoop.start();
    }
    
    /**
     * Create dummy recipes untuk testing
     * Recipes:
     * 1. Kappa Maki: Nori (Raw) + Nasi (Cooked) + Timun (Chopped)
     * 2. Sakana Maki: Nori (Raw) + Nasi (Cooked) + Ikan (Chopped)
     * 3. Ebi Maki: Nori (Raw) + Nasi (Cooked) + Udang (Cooked)
     * 4. Fish Cucumber Roll: Nori (Raw) + Nasi (Cooked) + Ikan (Chopped) + Timun (Chopped)
     */
    private List<Recipe> createDummyRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Recipe 1: Kappa Maki - Nori (Raw) + Nasi (Cooked) + Timun (Chopped)
        List<IngredientRequirement> kappaMakiReqs = new ArrayList<>();
        kappaMakiReqs.add(new IngredientRequirement(Nori.class, IngredientState.RAW));
        kappaMakiReqs.add(new IngredientRequirement(Rice.class, IngredientState.COOKED));
        kappaMakiReqs.add(new IngredientRequirement(Cucumber.class, IngredientState.CHOPPED));
        recipes.add(new Recipe("Kappa Maki", kappaMakiReqs));
        
        // Recipe 2: Sakana Maki - Nori (Raw) + Nasi (Cooked) + Ikan (Chopped)
        List<IngredientRequirement> sakanaMakiReqs = new ArrayList<>();
        sakanaMakiReqs.add(new IngredientRequirement(Nori.class, IngredientState.RAW));
        sakanaMakiReqs.add(new IngredientRequirement(Rice.class, IngredientState.COOKED));
        sakanaMakiReqs.add(new IngredientRequirement(Fish.class, IngredientState.CHOPPED));
        recipes.add(new Recipe("Sakana Maki", sakanaMakiReqs));
        
        // Recipe 3: Ebi Maki - Nori (Raw) + Nasi (Cooked) + Udang (Cooked)
        List<IngredientRequirement> ebiMakiReqs = new ArrayList<>();
        ebiMakiReqs.add(new IngredientRequirement(Nori.class, IngredientState.RAW));
        ebiMakiReqs.add(new IngredientRequirement(Rice.class, IngredientState.COOKED));
        ebiMakiReqs.add(new IngredientRequirement(Shrimp.class, IngredientState.COOKED));
        recipes.add(new Recipe("Ebi Maki", ebiMakiReqs));
        
        // Recipe 4: Fish Cucumber Roll - Nori (Raw) + Nasi (Cooked) + Ikan (Chopped) + Timun (Chopped)
        List<IngredientRequirement> fishCucumberRollReqs = new ArrayList<>();
        fishCucumberRollReqs.add(new IngredientRequirement(Nori.class, IngredientState.RAW));
        fishCucumberRollReqs.add(new IngredientRequirement(Rice.class, IngredientState.COOKED));
        fishCucumberRollReqs.add(new IngredientRequirement(Fish.class, IngredientState.CHOPPED));
        fishCucumberRollReqs.add(new IngredientRequirement(Cucumber.class, IngredientState.CHOPPED));
        recipes.add(new Recipe("Fish Cucumber Roll", fishCucumberRollReqs));
        
        return recipes;
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // Stop background order generation
        if (orderGeneratorTask != null) {
            orderGeneratorTask.stop();
        }
        
        // Shutdown executor service
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
    }
    
    private void goToMainMenu() {
        stop();
        // Reset singleton instance
        resetInstance();
        
        MainMenuScene menu = new MainMenuScene(stage);
        menu.playMusic();  // Play main menu music
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
                case F:
                    // Switch chef (changed from K to F)
                    switchChef();
                    break;
                case SHIFT:
                    shiftPressed = true;
                    break;
                case Q:
                    // Throw item 2 blocks in front
                    throwItem();
                    break;
                
                // --- SPACEBAR: DROP ITEM OR INTERACT ---
                case SPACE: 
                    if (!isPaused && activeChef != null && tileManager != null) {
                        // Check if chef has item in hand
                        nimons.entity.item.Item itemInHand = activeChef.getInventory();
                        
                        // Get position in front of chef
                        Position posInFront = getPositionInFront(activeChef);
                        Tile tileFront = tileManager.getTileAt(posInFront);
                        
                        if (itemInHand != null) {
                            // Check if chef wants to add ingredient to utensil in hand
                            if (itemInHand instanceof nimons.entity.item.interfaces.CookingDevice) {
                                // Chef is holding a cooking device - check if can add ingredient from tile
                                if (tileFront != null && tileFront.getItemOnTile() != null && 
                                    tileFront.getItemOnTile() instanceof nimons.entity.item.interfaces.Preparable) {
                                    // Try to add ingredient from tile to utensil
                                    addIngredientToUtensilInHand(tileFront);
                                    return; // Exit after handling
                                }
                            }
                            
                            // Chef punya item - cek apakah ada station di depan
                            if (tileFront != null && tileFront.getStation() != null) {
                                // Ada station di depan - INTERACT dengan station
                                tileFront.getStation().onInteract(activeChef);
                            } else if (tileFront != null && tileFront.isWalkable() && tileFront.getItemOnTile() == null) {
                                // Tidak ada station, tile walkable, dan kosong - DROP item
                                tileFront.setItemOnTile(itemInHand);
                                activeChef.setInventory(null);
                                addLog("✓ Dropped " + itemInHand.getName());
                            } else {
                                // Tidak bisa drop - wall atau tile tidak valid
                                addLog("✗ Cannot drop item here (wall or tile occupied)");
                            }
                        } else {
                            // Chef tangan kosong - cek station atau pick up item
                            if (tileFront != null && tileFront.getStation() != null) {
                                // Ada station - INTERACT
                                tileFront.getStation().onInteract(activeChef);
                            } else if (tileFront != null && tileFront.getItemOnTile() != null) {
                                // Ada item di lantai - PICK UP
                                pickupItemFromTile(tileFront);
                            }
                        }
                    }
                    break;
                // ----------------------------------------
                    
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
                case SHIFT:
                    shiftPressed = false;
                    break;
                default:
                    break;
            }
        });
    }
    

    /**
     * Pick up item dari lantai ke tangan chef
     */
    private void pickupItemFromTile(Tile tile) {
        if (activeChef == null || tile == null) {
            return;
        }
        
        nimons.entity.item.Item itemOnTile = tile.getItemOnTile();
        if (itemOnTile == null || activeChef.getInventory() != null) {
            return;
        }
        
        // Pick up item
        activeChef.setInventory(itemOnTile);
        tile.setItemOnTile(null);
        addLog("✓ Picked up " + itemOnTile.getName());
    }
    
    /**
     * Throw/drop item 2 blocks in front of chef
     */
    /**
     * Throw item dengan validasi path - item jatuh sebelum tembok atau ditangkap chef lain
     */
    private void throwItem() {
        if (activeChef == null || activeChef.getInventory() == null || tileManager == null) {
            return;
        }
        
        // CRITICAL: Ensure both chefs are tracked before checking throw path
        if (playerChef != null) {
            ensureChefTileTracking(playerChef);
        }
        if (chef2 != null) {
            ensureChefTileTracking(chef2);
        }
        
        nimons.entity.item.Item itemInHand = activeChef.getInventory();
        
        // Validasi: hanya bisa lempar ingredients yang belum dimasak
        boolean canThrow = false;
        if (itemInHand instanceof nimons.entity.item.Ingredient) {
            nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) itemInHand;
            // Hanya bisa lempar RAW atau CHOPPED ingredients
            if (ing.getState() == nimons.entity.item.IngredientState.RAW || 
                ing.getState() == nimons.entity.item.IngredientState.CHOPPED) {
                canThrow = true;
            }
        } else {
            // Non-ingredient items bisa dilempar
            canThrow = true;
        }
        
        if (!canThrow) {
            addLog("✗ Cannot throw cooked/burned ingredients");
            return;
        }
        
        // Calculate throw path with obstacle detection
        Position chefPos = activeChef.getPosition();
        Direction dir = activeChef.getDirection();
        int throwDistance = 3;
        
        Position landingPos = null;
        Chef catchingChef = null;
        
        // Check each tile in throw path
        for (int i = 1; i <= throwDistance; i++) {
            int checkX = chefPos.getX();
            int checkY = chefPos.getY();
            
            switch (dir) {
                case UP:
                    checkY -= i;
                    break;
                case DOWN:
                    checkY += i;
                    break;
                case LEFT:
                    checkX -= i;
                    break;
                case RIGHT:
                    checkX += i;
                    break;
            }
            
            Position checkPos = new Position(checkX, checkY);
            Tile checkTile = tileManager.getTileAt(checkPos);
            
            // Check if there's a wall/obstacle
            if (checkTile == null || !checkTile.isWalkable()) {
                // Item jatuh di posisi sebelumnya (sebelum tembok)
                if (landingPos != null) {
                    break; // Use previous valid position
                } else {
                    addLog("✗ Cannot throw - wall is too close");
                    return;
                }
            }
            
            // IMPORTANT: Only check for catching chef at EXACTLY tile 2 (final destination)
            if (i == throwDistance) {
                // Check if there's a chef to catch - prioritize direct position check
                Chef chefAtTile = null;
                
                // Direct position comparison (most reliable)
                if (playerChef != null && playerChef != activeChef) {
                    Position p1Pos = playerChef.getPosition();
                    if (p1Pos.getX() == checkPos.getX() && p1Pos.getY() == checkPos.getY()) {
                        chefAtTile = playerChef;
                        System.out.println("[THROW] Found " + playerChef.getName() + " at tile " + i + " by position (" + checkPos.getX() + ", " + checkPos.getY() + ")");
                    }
                }
                
                if (chefAtTile == null && chef2 != null && chef2 != activeChef) {
                    Position p2Pos = chef2.getPosition();
                    if (p2Pos.getX() == checkPos.getX() && p2Pos.getY() == checkPos.getY()) {
                        chefAtTile = chef2;
                        System.out.println("[THROW] Found " + chef2.getName() + " at tile " + i + " by position (" + checkPos.getX() + ", " + checkPos.getY() + ")");
                    }
                }
                
                // Fallback to tile tracking
                if (chefAtTile == null) {
                    chefAtTile = checkTile.getChefOnTile();
                    if (chefAtTile != null && chefAtTile != activeChef) {
                        System.out.println("[THROW] Found " + chefAtTile.getName() + " at tile " + i + " by tile tracking");
                    }
                }
                
                // Check if catching chef has empty inventory
                if (chefAtTile != null && chefAtTile != activeChef) {
                    if (chefAtTile.getInventory() == null) {
                        // Chef can catch the item
                        catchingChef = chefAtTile;
                        landingPos = checkPos;
                        System.out.println("[THROW] " + chefAtTile.getName() + " will catch item! (inventory: empty)");
                        break; // Chef will catch it
                    } else {
                        // Chef has item, cannot catch - item will land on ground
                        System.out.println("[THROW] " + chefAtTile.getName() + " cannot catch (inventory full), item will drop");
                    }
                }
            }
            
            // Check if tile is occupied by item (only matters if no catching chef)
            if (checkTile.getItemOnTile() != null) {
                // Item jatuh di posisi sebelumnya
                if (landingPos != null) {
                    break;
                } else {
                    addLog("✗ Cannot throw - path is blocked");
                    return;
                }
            }
            
            landingPos = checkPos; // Valid landing position
        }
        
        // Execute throw
        if (landingPos != null) {
            if (catchingChef != null) {
                System.out.println("[THROW] Found catching chef: " + catchingChef.getName() + 
                                 ", inventory empty: " + (catchingChef.getInventory() == null));
                
                if (catchingChef.getInventory() == null) {
                    // Chef catches the item
                    catchingChef.setInventory(itemInHand);
                    activeChef.setInventory(null);
                    addLog("✓ " + catchingChef.getName() + " caught " + itemInHand.getName());
                    System.out.println("[THROW] Item caught successfully!");
                } else {
                    // Chef's hands are full, item falls at their feet
                    Tile landingTile = tileManager.getTileAt(landingPos);
                    if (landingTile != null) {
                        landingTile.setItemOnTile(itemInHand);
                    }
                    activeChef.setInventory(null);
                    addLog("✓ Threw " + itemInHand.getName() + " (hands full, dropped)");
                    System.out.println("[THROW] Chef hands full, item dropped");
                }
            } else {
                // Item lands on tile
                System.out.println("[THROW] No chef found, landing at (" + landingPos.getX() + ", " + landingPos.getY() + ")");
                Tile landingTile = tileManager.getTileAt(landingPos);
                if (landingTile != null) {
                    landingTile.setItemOnTile(itemInHand);
                }
                activeChef.setInventory(null);
                int distance = Math.abs(landingPos.getX() - chefPos.getX()) + 
                               Math.abs(landingPos.getY() - chefPos.getY());
                addLog("✓ Threw " + itemInHand.getName() + " (" + distance + " blocks)");
            }
        } else {
            addLog("✗ Cannot throw item there");
        }
    }
    
    /**
     * Add ingredient from tile to cooking utensil in chef's hand
     */
    private void addIngredientToUtensilInHand(Tile tile) {
        if (activeChef == null || tile == null) {
            return;
        }
        
        nimons.entity.item.Item itemInHand = activeChef.getInventory();
        if (!(itemInHand instanceof nimons.entity.item.interfaces.CookingDevice)) {
            return;
        }
        
        nimons.entity.item.Item itemOnTile = tile.getItemOnTile();
        if (!(itemOnTile instanceof nimons.entity.item.interfaces.Preparable)) {
            return;
        }
        
        nimons.entity.item.interfaces.CookingDevice device = (nimons.entity.item.interfaces.CookingDevice) itemInHand;
        nimons.entity.item.interfaces.Preparable ingredient = (nimons.entity.item.interfaces.Preparable) itemOnTile;
        
        try {
            device.addIngredient(ingredient);
            tile.setItemOnTile(null);
            
            String ingredientName = ((nimons.entity.item.Item)ingredient).getName();
            String utensilName = ((nimons.entity.item.Item)device).getName();
            addLog("✓ Added " + ingredientName + " to " + utensilName + " (will cook when placed on station)");
            
        } catch (nimons.exceptions.StationFullException e) {
            addLog("✗ " + e.getMessage());
        } catch (nimons.exceptions.InvalidIngredientStateException e) {
            addLog("✗ " + e.getIngredientName() + " must be " + e.getRequiredState() + " (currently: " + e.getCurrentState() + ")");
        } catch (Exception e) {
            addLog("✗ Failed to add ingredient: " + e.getMessage());
        }
    }
    
    private void handleChefMovement(long currentTime) {
    if (activeChef == null || tileManager == null) {
            return;
        }

        // --- REVISI: TAMBAHKAN CHECK STATUS BUSY ---
        // Jika Chef sedang sibuk (misalnya memotong/mencuci), hentikan pemrosesan gerakan.
        // Asumsi: activeChef memiliki method isBusy()
        if (activeChef.isBusy()) { 
            return; 
        }
        // ------------------------------------------

        // Movement cooldown
        if (currentTime - lastMoveTime < GameConfig.MOVE_COOLDOWN_MS * 1_000_000) {
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
            Position currentPos = activeChef.getPosition();
            activeChef.setDirection(newDirection);
            
            // Check if SHIFT pressed for dash
            if (shiftPressed && !activeChef.isDashOnCooldown(currentTime)) {
                // Perform dash with obstacle detection
                Position dashTarget = calculateDashTarget(currentPos, newDirection);
                
                if (dashTarget != null && tileManager.isWalkable(dashTarget)) {
                    // Remove chef from old tile
                    Tile oldTile = tileManager.getTileAt(currentPos);
                    if (oldTile != null) {
                        oldTile.setChefOnTile(null);
                    }
                    
                    // Move to dash target
                    activeChef.setPosition(dashTarget);
                    activeChef.dash(newDirection, currentTime); // Update dash state
                    
                    // Update target position for smooth movement
                    chefTargetX = dashTarget.getX();
                    chefTargetY = dashTarget.getY();
                    
                    // Add chef to new tile
                    Tile newTile = tileManager.getTileAt(dashTarget);
                    if (newTile != null) {
                        newTile.setChefOnTile(activeChef);
                    }
                    
                    activeChef.setDashing(false);
                    lastMoveTime = currentTime;
                    
                    System.out.println("Chef dashed to: (" + dashTarget.getX() + ", " + dashTarget.getY() + ")");
                    return; // Exit after dash
                }
            }
            
            // Normal movement (1 tile)
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
                // Mark chef as moving
                chefIsMoving.put(activeChef, true);
                
                // Remove chef from old tile
                Tile oldTile = tileManager.getTileAt(currentPos);
                if (oldTile != null) {
                    oldTile.setChefOnTile(null);
                }
                
                // Move active chef
                activeChef.setPosition(newPos);
                
                // Update target position for smooth movement
                chefTargetX = newPos.getX();
                chefTargetY = newPos.getY();
                
                // Add chef to new tile
                // Catatan: Anda menggunakan playerChef di sini, bukan activeChef. 
                // Ini mungkin bug jika chef lain yang bergerak.
                Tile newTile = tileManager.getTileAt(newPos);
                if (newTile != null) {
                    newTile.setChefOnTile(activeChef); 
                }
                
                lastMoveTime = currentTime;
            }
        }
    }
    
    /**
     * Calculate dash target dengan obstacle detection
     * Dash 3 tiles, tapi stop di obstacle
     */
    /**
     * Calculate dash target dengan validasi - dash berhenti sebelum tembok
     */
    private Position calculateDashTarget(Position start, Direction direction) {
        int dashDistance = GameConfig.DASH_DISTANCE_TILES;
        int startX = start.getX();
        int startY = start.getY();
        
        Position lastValidPos = start;
        
        // Check setiap tile dalam path dash
        for (int i = 1; i <= dashDistance; i++) {
            int checkX = startX;
            int checkY = startY;
            
            switch (direction) {
                case UP:
                    checkY -= i;
                    break;
                case DOWN:
                    checkY += i;
                    break;
                case LEFT:
                    checkX -= i;
                    break;
                case RIGHT:
                    checkX += i;
                    break;
            }
            
            Position checkPos = new Position(checkX, checkY);
            Tile checkTile = tileManager.getTileAt(checkPos);
            
            // Jika tile tidak valid atau tidak walkable (tembok), berhenti di posisi sebelumnya
            if (checkTile == null || !checkTile.isWalkable()) {
                // Return posisi terakhir yang valid (sebelum tembok)
                if (lastValidPos.equals(start)) {
                    // Tembok tepat di depan, tidak bisa dash sama sekali
                    System.out.println("Dash blocked: wall directly in front");
                    return null;
                }
                System.out.println("Dash stopped before wall at distance " + (i-1));
                return lastValidPos;
            }
            
            // Jika ada chef lain di tile, berhenti di posisi sebelumnya
            if (checkTile.getChefOnTile() != null) {
                if (lastValidPos.equals(start)) {
                    System.out.println("Dash blocked: chef directly in front");
                    return null;
                }
                System.out.println("Dash stopped before chef at distance " + (i-1));
                return lastValidPos;
            }
            
            // Tile valid, update last valid position
            lastValidPos = checkPos;
        }
        
        // Jika semua tile dalam range valid, return posisi final
        System.out.println("Dash completed full distance: " + dashDistance);
        return lastValidPos;
    }
    
    /**
     * Method untuk menggambar chef di canvas
     * @param chef Chef yang akan digambar
     * @param offsetX Offset X untuk centering map
     * @param offsetY Offset Y untuk centering map
     * @param isActive Apakah chef ini sedang aktif (dikontrol)
     */
    private void drawChef(Chef chef, double offsetX, double offsetY, boolean isActive) {
        if (chef == null) return;
        
        // Gunakan smooth position untuk active chef, atau posisi langsung untuk inactive chef
        double drawX, drawY;
        if (isActive) {
            drawX = chefRenderX;
            drawY = chefRenderY;
        } else {
            drawX = chef.getPosition().getX();
            drawY = chef.getPosition().getY();
        }
        
        double chefScreenX = offsetX + drawX * tileSize;
        double chefScreenY = offsetY + drawY * tileSize;
        
        // Get the appropriate chef image based on movement state
        Image chefDisplayImage = getChefImage(chef);
        
        if (chefDisplayImage != null) {
            // Draw chef image with directional transformation
            drawChefWithDirection(gc, chefDisplayImage, chefScreenX, chefScreenY, tileSize, chef.getDirection());
        } else {
            // Fallback: Draw chef as a circle if image not loaded
            gc.setFill(Color.web("#ff6b6b"));
            double chefPadding = tileSize * 0.15;
            gc.fillOval(chefScreenX + chefPadding, chefScreenY + chefPadding,
                         tileSize - chefPadding * 2, tileSize - chefPadding * 2);
            
            // Draw direction indicator
            gc.setFill(Color.WHITE);
            double dirSize = tileSize * 0.15;
            double centerX = chefScreenX + tileSize / 2;
            double centerY = chefScreenY + tileSize / 2;
            
            switch (chef.getDirection()) {
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
        
        // Draw item held by chef (above head)
        if (chef.getInventory() != null) {
            Image itemImg = getItemImage(chef.getInventory());
            
            if (itemImg != null) {
                double itemSize = tileSize * 0.65;
                double itemX = chefScreenX + (tileSize - itemSize) / 2;
                double itemY = chefScreenY - itemSize - 5;
                gc.drawImage(itemImg, itemX, itemY, itemSize, itemSize);
            } else {
                System.out.println("WARNING: No image for chef held item: " + chef.getInventory().getName() + 
                                   " (type: " + chef.getInventory().getClass().getSimpleName() + ")");
            }
        }
    }
    
    /**
     * Method untuk switch antara chef 1 dan chef 2
     * Dipanggil saat tombol K ditekan
     */
    private void switchChef() {
        if (playerChef == null || chef2 == null) {
            return;
        }
        
        // Ensure both chefs are properly tracked on their tiles before switching
        ensureChefTileTracking(playerChef);
        ensureChefTileTracking(chef2);
        
        // Switch active chef
        if (activeChef == playerChef) {
            activeChef = chef2;
            System.out.println("Switched to Chef 2 at position (" + chef2.getPosition().getX() + ", " + chef2.getPosition().getY() + ")");
        } else {
            activeChef = playerChef;
            System.out.println("Switched to Chef 1 at position (" + playerChef.getPosition().getX() + ", " + playerChef.getPosition().getY() + ")");
        }
        
        // Update target position untuk smooth transition
        Position activePos = activeChef.getPosition();
        chefTargetX = activePos.getX();
        chefTargetY = activePos.getY();
        
        // Snap render position ke active chef
        chefRenderX = activePos.getX();
        chefRenderY = activePos.getY();
        
        // Reset movement flags
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
    }
    
    /**
     * Ensure chef is properly tracked on their current tile
     */
    private void ensureChefTileTracking(Chef chef) {
        if (chef == null || tileManager == null) {
            return;
        }
        
        Position chefPos = chef.getPosition();
        Tile chefTile = tileManager.getTileAt(chefPos);
        
        if (chefTile != null && chefTile.getChefOnTile() != chef) {
            chefTile.setChefOnTile(chef);
            System.out.println("[TRACKING] Updated " + chef.getName() + " tracking at (" + chefPos.getX() + ", " + chefPos.getY() + ")");
        }
    }
    
    // nimons/gui/GameScreen.java

    /**
     * Menghitung posisi tile di depan Chef berdasarkan arah (Direction)
     */
    private Position getPositionInFront(Chef chef) {
        Position currentPos = chef.getPosition();
        int newX = currentPos.getX();
        int newY = currentPos.getY();
        
        switch (chef.getDirection()) {
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
        
        return new Position(newX, newY);
    }
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseStartTime = System.currentTimeMillis();
            gameState.pause();
        } else {
            gameState.resume();
        }
    }
    
    private void renderPauseMenu() {
        // Draw semi-transparent overlay
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Draw pause menu box
        double menuWidth = 450;
        double menuHeight = 350;
        double menuX = (WINDOW_WIDTH - menuWidth) / 2;
        double menuY = (WINDOW_HEIGHT - menuHeight) / 2;
        
        // Menu background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
        
        // Menu border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
        
        // Title
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 56));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("PAUSED", WINDOW_WIDTH / 2, menuY + 70);
        
        // Instructions
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, 18));
        gc.setFill(Color.web("#E8A36B"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Press ESC or click RESUME to continue", WINDOW_WIDTH / 2, menuY + 130);
        
        // Resume Button
        drawButton(gc, "RESUME", menuX + (menuWidth - 200) / 2, menuY + 160, 200, 50);
        
        // Main Menu Button
        drawButton(gc, "MAIN MENU", menuX + (menuWidth - 200) / 2, menuY + 240, 200, 50);
    }

    private void setupMouseControls(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (isPaused) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                
                // Check if click is within Resume button
                if (mouseX >= menuResumeButtonX && mouseX <= menuResumeButtonX + menuResumeButtonWidth &&
                    mouseY >= menuResumeButtonY && mouseY <= menuResumeButtonY + menuResumeButtonHeight) {
                    togglePause(); // Resume game
                    return;
                }
                
                // Check if click is within Main Menu button
                if (mouseX >= menuMainMenuButtonX && mouseX <= menuMainMenuButtonX + menuMainMenuButtonWidth &&
                    mouseY >= menuMainMenuButtonY && mouseY <= menuMainMenuButtonY + menuMainMenuButtonHeight) {
                    goToMainMenu();
                }
            }
        });
    }
    
    private void drawButton(GraphicsContext gc, String text, double x, double y, double width, double height) {
        // Store button bounds based on button text
        if ("RESUME".equals(text)) {
            menuResumeButtonX = x;
            menuResumeButtonY = y;
            menuResumeButtonWidth = width;
            menuResumeButtonHeight = height;
        } else if ("MAIN MENU".equals(text)) {
            menuMainMenuButtonX = x;
            menuMainMenuButtonY = y;
            menuMainMenuButtonWidth = width;
            menuMainMenuButtonHeight = height;
        }
        
        // Button background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(x, y, width, height, 10, 10);
        
        // Button border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
        
        // Button text
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2 + 6);
    }
    
    private void renderGameUI() {
        // Margin dari tepi layar
        double margin = 20;
        
        // --- SCORE PANEL (Top Right) ---
        double scoreBoxX = WINDOW_WIDTH - 180 - margin;
        double scoreBoxY = margin;
        double scoreBoxWidth = 160;
        double scoreBoxHeight = 100;
        
        // Score background box
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(scoreBoxX, scoreBoxY, scoreBoxWidth, scoreBoxHeight, 10, 10);
        
        // Score border
        gc.setStroke(Color.web("#4b2a20"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(scoreBoxX, scoreBoxY, scoreBoxWidth, scoreBoxHeight, 10, 10);
        
        // Score label
        int currentScore = gameState.getScore().getCurrentScore();
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SCORE", scoreBoxX + scoreBoxWidth / 2, scoreBoxY + 25);
        
        // Score value
        gc.setFill(Color.web("#E8A36B"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 40));
        gc.fillText(String.valueOf(currentScore), scoreBoxX + scoreBoxWidth / 2, scoreBoxY + 75);
        
        // --- TIMER PANEL (Bottom Right) ---
        double timerBoxX = WINDOW_WIDTH - 180 - margin;
        double timerBoxY = WINDOW_HEIGHT - 100 - margin;
        double timerBoxWidth = 160;
        double timerBoxHeight = 80;
        
        // Timer background box
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight, 10, 10);
        
        // Timer border
        gc.setStroke(Color.web("#4b2a20"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight, 10, 10);
        
        // Timer label
        gc.setFill(Color.web("#F2C38F"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("TIME", timerBoxX + timerBoxWidth / 2, timerBoxY + 20);
        
        // Timer value
        String timeText = gameState.getTimer().getFormattedRemainingTime();
        gc.setFill(Color.web("#E8A36B"));
    gc.setFont(Font.font(nimons.core.GameConfig.DEFAULT_FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 36));
        gc.fillText(timeText, timerBoxX + timerBoxWidth / 2, timerBoxY + 65);
    }
    
    private void showResultScreen() {
        // Stop the game loop
        gameLoop.stop();
        
        // Always update stage progress (either pass or fail)
        nimons.logic.StageProgress.getInstance().completeStage(
            currentStageId, 
            gameState.getScore().getCurrentScore(),
            gameState.isPassed()
        );
        
        // Show result screen
        ResultScreen resultScreen = new ResultScreen(
            stage,
            gameState.getScore().getCurrentScore(),
            gameState.isPassed(),
            gameState.getPassThreshold(),
            gameState.getFailReason()
        );
        resultScreen.start();
    }
}
