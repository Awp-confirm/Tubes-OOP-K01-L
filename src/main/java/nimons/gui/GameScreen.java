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
    private Chef playerChef; 	// Chef pertama
    private Chef chef2; 	 	// Chef kedua
    private Chef activeChef; 	// Chef yang sedang dikontrol
    
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

    // --- LOGGING & SINGLETON FIELDS ---
    private List<String> onScreenLogs = new ArrayList<>();
    private static GameScreen instance; // Static instance untuk Singleton
    private final static int MAX_LOGS = 5; // Maksimal 5 baris log di layar

    public GameScreen(Stage stage) {
        this.stage = stage;
        this.rootPane = new StackPane();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        
        // --- IMPLEMENTASI SINGLETON ---
        if (instance != null) {
            throw new IllegalStateException("GameScreen already instantiated.");
        }
        instance = this; // Set instance saat konstruktor dipanggil
        // ------------------------------
        
        rootPane.getChildren().add(canvas);
        
        // Load assets
        loadAssets();
        
        // Load default map
        loadMap("stageSushi");
        
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
        // 1. Inisialisasi Map (wajib)
        stationImages = new HashMap<>();
        
        System.out.println("=== Forcing Fallback UI for Debugging ===");
        
        // 2. Paksa semua Image utama menjadi NULL
        floorImage = null;
        wallImage = null;
        chefImage = null;
        
        // 3. Paksa semua Image station menjadi NULL
        stationImages.put("cook", null);
        stationImages.put("table", null);
        stationImages.put("serving", null);
        stationImages.put("wash", null);
        
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
                
                // --- PERUBAHAN SKALA: Skala Tile Size ke 80% dari ukuran maksimum ---
                tileSize = tileSize * 0.70; 
            }
            
            System.out.println("Map loaded: " + stageId);
            System.out.println("Map size: " + tileManager.getWidth() + "x" + tileManager.getHeight());
            System.out.println("Tile size: " + tileSize);
            System.out.println("Spawn positions: " + spawnPositions.size());
            
            // Create 2 chefs at different spawn positions
            if (!spawnPositions.isEmpty()) {
                // Chef 1
                Position spawnPos1 = spawnPositions.get(0);
                playerChef = new Chef("player1", "Chef 1", spawnPos1, Direction.DOWN);
                
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
                
                chef2 = new Chef("player2", "Chef 2", spawnPos2, Direction.DOWN);
                
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
        // Skip update jika game di-pause
        if (isPaused) {
            return;
        }
        
        // Handle chef movement
        handleChefMovement(System.nanoTime());
        
        // Smooth chef position interpolation (untuk active chef)
        if (activeChef != null) {
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
        
        // Calculate map dimensions
        double mapWidth = tileManager.getWidth() * tileSize;
        double mapHeight = tileManager.getHeight() * tileSize;
        
        // Offset X: Tetap di tengah
        double offsetX = (WINDOW_WIDTH - mapWidth) / 2; 
        
        // Offset Y: Letakkan map 20px dari tepi atas
        double mapTopMargin = 20; 
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
                    // Fallback: dark gray wall
                    gc.setFill(Color.web("#2d2d2d"));
                    gc.fillRect(screenX, screenY, tileSize, tileSize);
                } else {
                    // Fallback: light floor
                    gc.setFill(Color.web("#e8dcc8"));
                    gc.fillRect(screenX, screenY, tileSize, tileSize);
                }
                
                // 2. Draw station on top of floor
                Station station = tile.getStation();
                if (station != null) {
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

                        // --- LOGIC BARU: RENDER PROGRESS BAR ---
                    if (station.isActive()) {
                        float ratio = station.getProgressRatio();
                        
                        // Progress Bar Dimensions
                        double barHeight = tileSize * 0.1;
                        double barWidth = tileSize * 0.8;
                        double barX = screenX + tileSize * 0.1;
                        double barY = screenY - barHeight - 2; // Di atas tile
                        
                        // Background Bar (Merah/Abu-abu)
                        gc.setFill(Color.GRAY.darker());
                        gc.fillRect(barX, barY, barWidth, barHeight);
                        
                        // Progress Bar (Hijau)
                        gc.setFill(Color.LIMEGREEN);
                        gc.fillRect(barX, barY, barWidth * ratio, barHeight);
                        
                        // Border (Optional)
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                        gc.strokeRect(barX, barY, barWidth, barHeight);
                    }
                    // ----------------------------------------
            
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
            gc.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
            gc.fillText("Hand: " + handContent, fixedLogX, hudYStart);
        }
        
        // 2. Log Aksi (Stack dari Bawah)
        
        // Y awal untuk log (20px di bawah status tangan)
        double logYStart = hudYStart + 20; 
        
        gc.setFont(Font.font("Arial", 16));
        
        // Log terbaru ada di index 0 (ingin log terbaru di BARIS PALING BAWAH)
        for (int i = 0; i < onScreenLogs.size(); i++) {
            String logMsg = onScreenLogs.get(i);
            
            // Stack ke bawah (logYStart + 20*i)
            double drawY = logYStart + i * 20; 
            
            gc.setFill(Color.WHITE); 
            gc.fillText(logMsg, fixedLogX, drawY);
        }
        // -----------------------------------------------------
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
                case K:
                    // Switch chef
                    switchChef();
                    break;
                
                // --- PERUBAHAN KRITIS: INTERAKSI MENGGUNAKAN SPACE ---
                case SPACE: 
                    // Trigger interaction on the adjacent station
                    if (!isPaused && activeChef != null && tileManager != null) {
                        
                        // 1. Dapatkan posisi TILE di depan Chef
                        Position posInFront = getPositionInFront(activeChef);
                        
                        // 2. Dapatkan Tile di posisi tersebut
                        Tile adjacentTile = tileManager.getTileAt(posInFront);

                        // 3. Cek apakah Tile tersebut ada dan memiliki Station
                        if (adjacentTile != null && adjacentTile.getStation() != null) {
                            // Panggil onInteract pada Station yang ditemukan
                            adjacentTile.getStation().onInteract(activeChef);
                        }
                    }
                    break;
                // ----------------------------------------------------
                    
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
            Position currentPos = activeChef.getPosition();
            activeChef.setDirection(newDirection);
            
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
        
        if (chefImage != null) {
            // Draw chef image
            gc.drawImage(chefImage, chefScreenX, chefScreenY, tileSize, tileSize);
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
        
        // Draw chef name label
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(tileSize * 0.2));
        gc.fillText(chef.getName(), chefScreenX + tileSize * 0.1, chefScreenY - tileSize * 0.05);
    }
    
    /**
     * Method untuk switch antara chef 1 dan chef 2
     * Dipanggil saat tombol K ditekan
     */
    private void switchChef() {
        if (playerChef == null || chef2 == null) {
            return;
        }
        
        // Switch active chef
        if (activeChef == playerChef) {
            activeChef = chef2;
            System.out.println("Switched to Chef 2");
        } else {
            activeChef = playerChef;
            System.out.println("Switched to Chef 1");
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