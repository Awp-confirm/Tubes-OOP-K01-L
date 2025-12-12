package nimons.logic.event;

/**
 * OBSERVER PATTERN - Observer Interface
 * 
 * Interface untuk objek yang ingin menerima notifikasi game events
 */
public interface GameEventListener {
    
    /**
     * Dipanggil ketika event terjadi
     * 
     * @param event Event yang terjadi
     */
    void onGameEvent(GameEvent event);
    
    /**
     * Menentukan apakah listener ini tertarik pada event type tertentu
     * Untuk optimization - listener hanya menerima event yang relevan
     * 
     * @param eventType Tipe event yang dicek
     * @return true jika listener tertarik pada event type ini
     */
    default boolean isInterestedIn(GameEventType eventType) {
        return true; // Default: tertarik pada semua event
    }
}
