package nimons.core;

public final class GameConfig {
    
    
    private GameConfig() {
        throw new AssertionError("GameConfig is a utility class and should not be instantiated");
    }
    
    
    
    
    public static final long GAME_DURATION_SECONDS = 300;
    
    
    public static final int PASSING_SCORE_THRESHOLD = 600;
    
    
    @Deprecated
    public static final int INITIAL_LIVES = 3;
    
    
    public static final long ORDER_SPAWN_INTERVAL_MS = 20000;
    
    
    public static final int MAX_ACTIVE_ORDERS = 3;
    
    
    public static final int ORDER_DURATION_SECONDS = 60;
    
    
    
    
    public static final int WINDOW_WIDTH = 1200;
    
    
    public static final int WINDOW_HEIGHT = 800;
    
    
    public static final double TILE_SIZE_SCALE = 0.70;
    
    
    public static final double MAP_TOP_MARGIN = 40.0;
    
    
    public static final double FIXED_LOG_X = 20.0;
    
    
    public static final int MAX_DISPLAYED_LOGS = 5;
    
    
    public static final double CHEF_RENDER_OFFSET_Y = -10.0;
    
    
    public static final double MOVE_SPEED = 0.3;

    

    
    public static final String DEFAULT_FONT_FAMILY = "Pixelify Sans";
    
    
    
    
    public static final long MOVE_COOLDOWN_MS = 150;
    
    
    public static final long DASH_COOLDOWN_MS = 3000;
    
    
    public static final int DASH_DISTANCE_TILES = 3;
    
    
    
    
    public static final float CUTTING_REQUIRED_TIME_MS = 3000.0f;
    
    
    public static final long TIME_TO_COOK_MS = 5000;
    
    
    public static final long TIME_TO_BURN_MS = 10000;
    
    
    public static final long COOKING_LOG_INTERVAL_MS = 1000;
    
    
    
    
    public static final float WASHING_REQUIRED_TIME_MS = 3000.0f;
    
    
    public static final float WASHING_LOG_INTERVAL_MS = 1000.0f;
    
    
    
    
    public static final float PLATE_RETURN_DELAY_MS = 10000.0f;
    
    
    public static final int INITIAL_PLATE_STOCK = 4;
    
    
    
    
    public static final double ORDER_CARD_WIDTH = 180;
    
    
    public static final double ORDER_CARD_HEIGHT = 180;
    
    
    public static final double ORDER_CARD_SPACING = 15;
    
    
    public static final double ORDER_MARGIN_LEFT = 20;
    
    
    
    
    public static final int BOILING_POT_CAPACITY = 3;
    
    
    public static final int FRYING_PAN_CAPACITY = 2;
    
    
    
    
        
    public static long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }
    
    
        
    public static double millisToSeconds(long millis) {
        return millis / 1000.0;
    }
    
    
        
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }
}
