package nimons.logic.event;

/**
 * OBSERVER PATTERN - Event Types
 * 
 * Enumeration of all possible game events
 */
public enum GameEventType {
    // Order events
    ORDER_CREATED,
    ORDER_COMPLETED,
    ORDER_FAILED,
    
    // Score events
    SCORE_CHANGED,
    LIFE_LOST,
    LIFE_GAINED,
    
    // Cooking events
    INGREDIENT_CHOPPED,
    INGREDIENT_COOKED,
    INGREDIENT_BURNED,
    DISH_ASSEMBLED,
    
    // Game state events
    GAME_STARTED,
    GAME_PAUSED,
    GAME_RESUMED,
    GAME_ENDED
}
