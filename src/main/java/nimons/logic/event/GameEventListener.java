package nimons.logic.event;

public interface GameEventListener {
    
    
    void onGameEvent(GameEvent event);
    
    
    default boolean isInterestedIn(GameEventType eventType) {
        return true; 
    }
}
