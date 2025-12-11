package nimons.logic.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogManager {
    
    private static GameLogManager instance = new GameLogManager();
    // Gunakan List<String> sebagai penyimpanan pusat log yang dibaca oleh GUI
    private final List<String> logMessages = new ArrayList<>();
    private final int MAX_LOGS = 50; // Batasi jumlah log

    private GameLogManager() {
        // Konstruktor private untuk pola Singleton
    }
    
    public static GameLogManager getInstance() {
        return instance;
    }
    
    /**
     * Menerima pesan log dari seluruh sistem. Dipanggil oleh method log() di Station.
     * @param message Pesan log yang sudah diformat.
     */
    public void addLog(String message) {
        if (logMessages.size() >= MAX_LOGS) {
            logMessages.remove(0); // Hapus yang tertua
        }
        logMessages.add(message);
    }
    
    /**
     * Dipanggil oleh GUI untuk mengambil daftar log terbaru.
     */
    public List<String> getRecentLogs() {
        // Mengembalikan unmodifiable list untuk mencegah modifikasi dari luar
        return Collections.unmodifiableList(logMessages); 
    }
}