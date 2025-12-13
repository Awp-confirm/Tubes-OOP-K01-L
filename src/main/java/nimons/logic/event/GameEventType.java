package nimons.logic.event;

public enum GameEventType {
    
    ORDER_CREATED,
    ORDER_COMPLETED,
    ORDER_FAILED,
    
    
    SCORE_CHANGED,
    LIFE_LOST,
    LIFE_GAINED,
    
    
    INGREDIENT_CHOPPED,
    INGREDIENT_COOKED,
    INGREDIENT_BURNED,
    DISH_ASSEMBLED,
    
    
    GAME_STARTED,
    GAME_PAUSED,
    GAME_RESUMED,
    GAME_ENDED
}
