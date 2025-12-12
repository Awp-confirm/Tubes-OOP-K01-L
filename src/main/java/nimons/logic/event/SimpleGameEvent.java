package nimons.logic.event;

/**
 * OBSERVER PATTERN - Concrete Event Implementation
 * 
 * Represents a specific game event with its data
 */
public class SimpleGameEvent implements GameEvent {
    
    private final GameEventType eventType;
    private final Object data;
    private final long timestamp;
    
    public SimpleGameEvent(GameEventType eventType) {
        this(eventType, null);
    }
    
    public SimpleGameEvent(GameEventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public GameEventType getEventType() {
        return eventType;
    }
    
    @Override
    public Object getData() {
        return data;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("GameEvent[type=%s, data=%s, timestamp=%d]", 
            eventType, data, timestamp);
    }
}
