package nimons.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks player progress across stages
 */
public class StageProgress {
    
    private static StageProgress instance;
    
    public enum StageStatus {
        LOCKED,
        AVAILABLE,
        SUCCESS,
        FAILED
    }
    
    private Map<String, StageStatus> stageStatuses;
    private Map<String, Integer> stageBestScores;
    
    private StageProgress() {
        this.stageStatuses = new HashMap<>();
        this.stageBestScores = new HashMap<>();
        
        // Initialize default stages
        stageStatuses.put("stageSushi", StageStatus.AVAILABLE); // First stage always available
    }
    
    public static StageProgress getInstance() {
        if (instance == null) {
            instance = new StageProgress();
        }
        return instance;
    }
    
    public StageStatus getStageStatus(String stageId) {
        return stageStatuses.getOrDefault(stageId, StageStatus.LOCKED);
    }
    
    public void completeStage(String stageId, int score, boolean passed) {
        // Set status based on whether player passed or failed
        stageStatuses.put(stageId, passed ? StageStatus.SUCCESS : StageStatus.FAILED);
        
        // Update best score
        int currentBest = stageBestScores.getOrDefault(stageId, 0);
        if (score > currentBest) {
            stageBestScores.put(stageId, score);
        }
    }
    
    public int getBestScore(String stageId) {
        return stageBestScores.getOrDefault(stageId, 0);
    }
    
    public void unlockStage(String stageId) {
        if (!stageStatuses.containsKey(stageId)) {
            stageStatuses.put(stageId, StageStatus.AVAILABLE);
        }
    }
    
    public void reset() {
        stageStatuses.clear();
        stageBestScores.clear();
        stageStatuses.put("stageSushi", StageStatus.AVAILABLE);
    }
}
