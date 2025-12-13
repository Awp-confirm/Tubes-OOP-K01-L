package nimons.logic;

import java.util.HashMap;
import java.util.Map;

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
        
        
        stageStatuses.put("stageSushi", StageStatus.AVAILABLE); 
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
        
        stageStatuses.put(stageId, passed ? StageStatus.SUCCESS : StageStatus.FAILED);
        
        
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
