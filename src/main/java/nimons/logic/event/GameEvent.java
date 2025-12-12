package nimons.logic.event;

/**
 * OBSERVER PATTERN - Event Interface
 * 
 * Defines different types of game events that can occur
 */
public interface GameEvent {
    
    /**
     * Get the type of this event
     */
    GameEventType getEventType();
    
    /**
     * Get event data as generic Object
     * Cast to specific type based on event type
     */
    Object getData();
    
    /**
     * Get timestamp when event occurred
     */
    long getTimestamp();
}
