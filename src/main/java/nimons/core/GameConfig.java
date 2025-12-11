package nimons.core;

/**
 * Centralized game configuration
 * All game settings and constants in one place
 */
public final class GameConfig {
    
    // Prevent instantiation
    private GameConfig() {
        throw new AssertionError("GameConfig is a utility class and should not be instantiated");
    }
    
    // ==================== GAME RULES ====================
    
    /** Game duration in seconds (default: 5 minutes) */
    public static final long GAME_DURATION_SECONDS = 300;
    
    /** Minimum score required to pass the game */
    public static final int PASSING_SCORE_THRESHOLD = 500;
    
    /** Initial number of lives - DEPRECATED: Use GameSettings.getInitialLives() instead */
    @Deprecated
    public static final int INITIAL_LIVES = 3;
    
    /** Order spawn interval in milliseconds */
    public static final long ORDER_SPAWN_INTERVAL_MS = 20000;
    
    /** Maximum active orders at once */
    public static final int MAX_ACTIVE_ORDERS = 3;
    
    /** Order time limit in seconds */
    public static final int ORDER_DURATION_SECONDS = 60;
    
    // ==================== WINDOW & RENDERING ====================
    
    /** Game window width */
    public static final int WINDOW_WIDTH = 1200;
    
    /** Game window height */
    public static final int WINDOW_HEIGHT = 800;
    
    /** Tile size scaling factor */
    public static final double TILE_SIZE_SCALE = 0.70;
    
    /** Top margin for map rendering */
    public static final double MAP_TOP_MARGIN = 40.0;
    
    /** X position for on-screen logs */
    public static final double FIXED_LOG_X = 20.0;
    
    /** Maximum number of logs displayed on screen */
    public static final int MAX_DISPLAYED_LOGS = 5;
    
    /** Chef render Y offset */
    public static final double CHEF_RENDER_OFFSET_Y = -10.0;
    
    /** Smooth movement interpolation speed */
    public static final double MOVE_SPEED = 0.3;
    
    // ==================== CHEF MOVEMENT ====================
    
    /** Movement cooldown in milliseconds */
    public static final long MOVE_COOLDOWN_MS = 150;
    
    /** Dash cooldown in milliseconds */
    public static final long DASH_COOLDOWN_MS = 3000;
    
    /** Dash distance in tiles */
    public static final int DASH_DISTANCE_TILES = 3;
    
    // ==================== COOKING & CUTTING ====================
    
    /** Cutting station required time in milliseconds */
    public static final float CUTTING_REQUIRED_TIME_MS = 3000.0f;
    
    /** Cooking time in milliseconds */
    public static final long TIME_TO_COOK_MS = 5000;
    
    /** Time before food burns in milliseconds */
    public static final long TIME_TO_BURN_MS = 10000;
    
    /** Cooking progress log interval in milliseconds */
    public static final long COOKING_LOG_INTERVAL_MS = 1000;
    
    // ==================== WASHING ====================
    
    /** Washing station required time in milliseconds */
    public static final float WASHING_REQUIRED_TIME_MS = 3000.0f;
    
    /** Washing progress log interval in milliseconds */
    public static final float WASHING_LOG_INTERVAL_MS = 1000.0f;
    
    // ==================== SERVING ====================
    
    /** Plate return delay in milliseconds */
    public static final float PLATE_RETURN_DELAY_MS = 10000.0f;
    
    /** Initial plate stock */
    public static final int INITIAL_PLATE_STOCK = 4;
    
    // ==================== ORDER DISPLAY ====================
    
    /** Order card width */
    public static final double ORDER_CARD_WIDTH = 180;
    
    /** Order card height */
    public static final double ORDER_CARD_HEIGHT = 180;
    
    /** Spacing between order cards */
    public static final double ORDER_CARD_SPACING = 15;
    
    /** Order card margin from left edge */
    public static final double ORDER_MARGIN_LEFT = 20;
    
    // ==================== UTENSIL CAPACITY ====================
    
    /** Boiling pot capacity */
    public static final int BOILING_POT_CAPACITY = 3;
    
    /** Frying pan capacity */
    public static final int FRYING_PAN_CAPACITY = 2;
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Convert nanoseconds to milliseconds
     */
    public static long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }
    
    /**
     * Convert milliseconds to seconds
     */
    public static double millisToSeconds(long millis) {
        return millis / 1000.0;
    }
    
    /**
     * Convert seconds to milliseconds
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }
}
