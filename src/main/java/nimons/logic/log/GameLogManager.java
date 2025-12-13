package nimons.logic.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogManager {
    
    private static GameLogManager instance = new GameLogManager();
    
    private final List<String> logMessages = new ArrayList<>();
    private final int MAX_LOGS = 50; 

    private GameLogManager() {
        
    }
    
    public static GameLogManager getInstance() {
        return instance;
    }
    
    
        
    public void addLog(String message) {
        if (logMessages.size() >= MAX_LOGS) {
            logMessages.remove(0); 
        }
        logMessages.add(message);
    }
    
    
    public List<String> getRecentLogs() {
        
        return Collections.unmodifiableList(logMessages); 
    }
}