package nimons.logic.event;

public interface GameEvent {
    
    
    GameEventType getEventType();
    
    
    Object getData();
    
    
    long getTimestamp();
}
