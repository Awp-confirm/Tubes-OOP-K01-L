package nimons.logic.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEventManager {
    
    private static volatile GameEventManager instance;
    
    
    
    private final Map<GameEventType, CopyOnWriteArrayList<GameEventListener>> listeners;
    
    
    private final CopyOnWriteArrayList<GameEventListener> wildcardListeners;
    
    
    private final List<GameEvent> eventHistory;
    private final int MAX_HISTORY_SIZE = 100;
    
    private GameEventManager() {
        this.listeners = new EnumMap<>(GameEventType.class);
        this.wildcardListeners = new CopyOnWriteArrayList<>();
        this.eventHistory = new ArrayList<>();
        
        
        for (GameEventType type : GameEventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
        }
    }
    
    
    public static GameEventManager getInstance() {
        if (instance == null) {
            synchronized (GameEventManager.class) {
                if (instance == null) {
                    instance = new GameEventManager();
                }
            }
        }
        return instance;
    }
    
    
        
    public void addListener(GameEventType eventType, GameEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        CopyOnWriteArrayList<GameEventListener> listenerList = listeners.get(eventType);
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
            System.out.println("[EventManager] Added listener for " + eventType);
        }
    }
    
    
        
    public void addWildcardListener(GameEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        if (!wildcardListeners.contains(listener)) {
            wildcardListeners.add(listener);
            System.out.println("[EventManager] Added wildcard listener");
        }
    }
    
    
        
    public void removeListener(GameEventType eventType, GameEventListener listener) {
        CopyOnWriteArrayList<GameEventListener> listenerList = listeners.get(eventType);
        if (listenerList.remove(listener)) {
            System.out.println("[EventManager] Removed listener from " + eventType);
        }
    }
    
    
        
    public void removeWildcardListener(GameEventListener listener) {
        if (wildcardListeners.remove(listener)) {
            System.out.println("[EventManager] Removed wildcard listener");
        }
    }
    
    
        
    public void fireEvent(GameEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        
        addToHistory(event);
        
        
        CopyOnWriteArrayList<GameEventListener> specificListeners = listeners.get(event.getEventType());
        for (GameEventListener listener : specificListeners) {
            try {
                listener.onGameEvent(event);
            } catch (Exception e) {
                System.err.println("[EventManager] Error notifying listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        
        for (GameEventListener listener : wildcardListeners) {
            try {
                if (listener.isInterestedIn(event.getEventType())) {
                    listener.onGameEvent(event);
                }
            } catch (Exception e) {
                System.err.println("[EventManager] Error notifying wildcard listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    
        
    public void fireEvent(GameEventType eventType) {
        fireEvent(new SimpleGameEvent(eventType));
    }
    
    
        
    public void fireEvent(GameEventType eventType, Object data) {
        fireEvent(new SimpleGameEvent(eventType, data));
    }
    
    
        
    private void addToHistory(GameEvent event) {
        synchronized (eventHistory) {
            eventHistory.add(event);
            
            if (eventHistory.size() > MAX_HISTORY_SIZE) {
                eventHistory.remove(0);
            }
        }
    }
    
    
    public List<GameEvent> getEventHistory() {
        synchronized (eventHistory) {
            return new ArrayList<>(eventHistory);
        }
    }
    
    
        
    public void reset() {
        for (CopyOnWriteArrayList<GameEventListener> listenerList : listeners.values()) {
            listenerList.clear();
        }
        wildcardListeners.clear();
        synchronized (eventHistory) {
            eventHistory.clear();
        }
        System.out.println("[EventManager] Reset - all listeners removed");
    }
    
    
    public int getListenerCount(GameEventType eventType) {
        return listeners.get(eventType).size();
    }
    
    
    public int getTotalListenerCount() {
        int count = wildcardListeners.size();
        for (CopyOnWriteArrayList<GameEventListener> listenerList : listeners.values()) {
            count += listenerList.size();
        }
        return count;
    }
}
