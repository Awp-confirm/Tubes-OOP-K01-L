package nimons.logic.scoring;

/**
 * Manages game score without maximum limit
 */
public class GameScore {
    private int currentScore = 0;
    
    public GameScore() {
        this.currentScore = 0;
    }
    
    /**
     * Add points to score
     */
    public void addScore(int points) {
        currentScore += points;
    }
    
    /**
     * Subtract points from score
     */
    public void subtractScore(int points) {
        currentScore = Math.max(currentScore - points, 0);
    }
    
    /**
     * Set score directly
     */
    public void setScore(int score) {
        currentScore = Math.max(score, 0);
    }
    
    /**
     * Get current score
     */
    public int getCurrentScore() {
        return currentScore;
    }
    
    /**
     * Reset score to 0
     */
    public void reset() {
        currentScore = 0;
    }
    
    /**
     * Check if score meets pass threshold
     */
    public boolean isPassed(int passThreshold) {
        return currentScore >= passThreshold;
    }
}
