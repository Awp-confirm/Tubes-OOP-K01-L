package nimons.logic;

import nimons.logic.scoring.GameScore;

/**
 * Manages overall game state including timer and score
 */
public class GameState {
    private GameTimer timer;
    private GameScore score;
    private boolean isGameOver = false;
    private boolean isPassed = false;
    private int passThreshold = 1000; // Default pass threshold is 1000 points
    
    private static final long DEFAULT_GAME_DURATION = 300; // 5 minutes
    
    public GameState() {
        this(DEFAULT_GAME_DURATION, 1000);
    }
    
    public GameState(long durationSeconds, int passThreshold) {
        this.timer = new GameTimer(durationSeconds);
        this.score = new GameScore();
        this.passThreshold = passThreshold;
    }
    
    /**
     * Update game state (call every frame)
     */
    public void update() {
        if (timer.isTimeUp() && !isGameOver) {
            endGame();
        }
    }
    
    /**
     * End the game and determine pass/fail
     */
    public void endGame() {
        isGameOver = true;
        isPassed = score.isPassed(passThreshold);
    }
    
    /**
     * Check if game is over
     */
    public boolean isGameOver() {
        return isGameOver;
    }
    
    /**
     * Check if player passed
     */
    public boolean isPassed() {
        return isPassed;
    }
    
    /**
     * Pause the game
     */
    public void pause() {
        timer.pause();
    }
    
    /**
     * Resume the game
     */
    public void resume() {
        timer.resume();
    }
    
    /**
     * Get timer
     */
    public GameTimer getTimer() {
        return timer;
    }
    
    /**
     * Get score
     */
    public GameScore getScore() {
        return score;
    }
    
    /**
     * Get pass threshold
     */
    public int getPassThreshold() {
        return passThreshold;
    }
    
    /**
     * Get result message
     */
    public String getResultMessage() {
        if (isPassed) {
            return "PASS";
        } else {
            return "FAIL";
        }
    }
}
