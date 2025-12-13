package nimons.logic.scoring;

public class GameScore {
    private int currentScore = 0;
    
    public GameScore() {
        this.currentScore = 0;
    }
    
    
        
    public void addScore(int points) {
        currentScore += points;
    }
    
    
        
    public void subtractScore(int points) {
        currentScore = Math.max(currentScore - points, 0);
    }
    
    
    public void setScore(int score) {
        currentScore = Math.max(score, 0);
    }
    
    
    public int getCurrentScore() {
        return currentScore;
    }
    
    
        
    public void reset() {
        currentScore = 0;
    }
    
    
        
    public boolean isPassed(int passThreshold) {
        return currentScore >= passThreshold;
    }
}
