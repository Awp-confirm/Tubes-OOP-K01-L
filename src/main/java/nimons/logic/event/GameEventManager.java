package nimons.logic.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * OBSERVER PATTERN - Event Manager (Subject)
 * 
 * Observer Pattern memungkinkan objek (observers) untuk subscribe dan menerima
 * notifikasi ketika event terjadi pada objek lain (subject).
 * 
 * Manfaat:
 * 1. Loose Coupling: Publishers tidak perlu tahu detail tentang subscribers
 * 2. Dynamic Relationships: Observers dapat ditambah/hapus saat runtime
 * 3. Broadcast Communication: Satu event bisa notify banyak observers
 * 4. Open/Closed Principle: Mudah menambah observer baru tanpa ubah subject
 * 
 * Implementation Details:
 * - Menggunakan CopyOnWriteArrayList untuk thread-safety saat iteration
 * - Event filtering berdasarkan interest untuk performance optimization
 * - Support untuk wildcard listeners (tertarik pada semua event)
 */
public class GameEventManager {
    
    private static volatile GameEventManager instance;
    
    // Map untuk menyimpan listeners berdasarkan event type
    // CopyOnWriteArrayList aman untuk concurrent modification
    private final Map<GameEventType, CopyOnWriteArrayList<GameEventListener>> listeners;
    
    // Wildcard listeners yang tertarik pada semua event
    private final CopyOnWriteArrayList<GameEventListener> wildcardListeners;
    
    // Event queue untuk debugging atau replay
    private final List<GameEvent> eventHistory;
    private final int MAX_HISTORY_SIZE = 100;
    
    private GameEventManager() {
        this.listeners = new EnumMap<>(GameEventType.class);
        this.wildcardListeners = new CopyOnWriteArrayList<>();
        this.eventHistory = new ArrayList<>();
        
        // Initialize lists untuk setiap event type
        for (GameEventType type : GameEventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
        }
    }
    
    /**
     * Thread-safe singleton instance getter
     */
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
    
    /**
     * Subscribe listener untuk specific event type
     * 
     * @param eventType Tipe event yang ingin di-listen
     * @param listener Listener yang akan menerima notifikasi
     */
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
    
    /**
     * Subscribe listener untuk semua event types
     * 
     * @param listener Listener yang akan menerima semua notifikasi
     */
    public void addWildcardListener(GameEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        if (!wildcardListeners.contains(listener)) {
            wildcardListeners.add(listener);
            System.out.println("[EventManager] Added wildcard listener");
        }
    }
    
    /**
     * Unsubscribe listener dari specific event type
     * 
     * @param eventType Tipe event
     * @param listener Listener yang akan di-remove
     */
    public void removeListener(GameEventType eventType, GameEventListener listener) {
        CopyOnWriteArrayList<GameEventListener> listenerList = listeners.get(eventType);
        if (listenerList.remove(listener)) {
            System.out.println("[EventManager] Removed listener from " + eventType);
        }
    }
    
    /**
     * Unsubscribe wildcard listener
     * 
     * @param listener Listener yang akan di-remove
     */
    public void removeWildcardListener(GameEventListener listener) {
        if (wildcardListeners.remove(listener)) {
            System.out.println("[EventManager] Removed wildcard listener");
        }
    }
    
    /**
     * Publish event ke semua interested listeners
     * 
     * @param event Event yang akan di-broadcast
     */
    public void fireEvent(GameEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        // Add to history
        addToHistory(event);
        
        // Notify specific listeners
        CopyOnWriteArrayList<GameEventListener> specificListeners = listeners.get(event.getEventType());
        for (GameEventListener listener : specificListeners) {
            try {
                listener.onGameEvent(event);
            } catch (Exception e) {
                System.err.println("[EventManager] Error notifying listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Notify wildcard listeners
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
    
    /**
     * Convenience method untuk create dan fire event
     * 
     * @param eventType Tipe event
     */
    public void fireEvent(GameEventType eventType) {
        fireEvent(new SimpleGameEvent(eventType));
    }
    
    /**
     * Convenience method untuk create dan fire event dengan data
     * 
     * @param eventType Tipe event
     * @param data Data yang terkait dengan event
     */
    public void fireEvent(GameEventType eventType, Object data) {
        fireEvent(new SimpleGameEvent(eventType, data));
    }
    
    /**
     * Add event to history for debugging
     */
    private void addToHistory(GameEvent event) {
        synchronized (eventHistory) {
            eventHistory.add(event);
            // Keep history size limited
            if (eventHistory.size() > MAX_HISTORY_SIZE) {
                eventHistory.remove(0);
            }
        }
    }
    
    /**
     * Get recent event history (for debugging)
     */
    public List<GameEvent> getEventHistory() {
        synchronized (eventHistory) {
            return new ArrayList<>(eventHistory);
        }
    }
    
    /**
     * Clear all listeners and history
     */
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
    
    /**
     * Get count of listeners for specific event type
     */
    public int getListenerCount(GameEventType eventType) {
        return listeners.get(eventType).size();
    }
    
    /**
     * Get total listener count
     */
    public int getTotalListenerCount() {
        int count = wildcardListeners.size();
        for (CopyOnWriteArrayList<GameEventListener> listenerList : listeners.values()) {
            count += listenerList.size();
        }
        return count;
    }
}
