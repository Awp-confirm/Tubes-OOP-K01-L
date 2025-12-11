package nimons.logic;

import nimons.core.GameConfig;
import nimons.core.GameSettings;
import nimons.logic.scoring.GameScore;

/**
 * Manages overall game state including timer, score, and lives
 */
public class GameState {
    
    public enum FailReason {
        NONE,
        TIME_UP,
        NO_LIVES
    }
    
    private GameTimer timer;
    private GameScore score;
    private boolean isGameOver = false;
    private boolean isPassed = false;
    private int passThreshold;
    private int lives;
    private boolean unlimitedLives;
    private FailReason failReason = FailReason.NONE;
    
    public GameState() {
        this(GameConfig.GAME_DURATION_SECONDS, GameConfig.PASSING_SCORE_THRESHOLD);
    }
    
    public GameState(long durationSeconds, int passThreshold) {
        this.timer = new GameTimer(durationSeconds);
        this.score = new GameScore();
        this.passThreshold = passThreshold;
        
        // Use difficulty settings
        GameSettings settings = GameSettings.getInstance();
        this.unlimitedLives = settings.isUnlimitedLives();
        this.lives = settings.getInitialLives();
        
        System.out.println("[GameState] Initialized with difficulty: " + settings.getDifficulty().getDisplayName());
        System.out.println("[GameState] Lives: " + this.lives + ", Unlimited: " + this.unlimitedLives);
    }
    
    /**
     * Update game state (call every frame)
     */
    public void update() {
        if (timer.isTimeUp() && !isGameOver) {
            failReason = FailReason.TIME_UP;
            endGame();
        }
        // Check if lives are exhausted (only if not unlimited)
        if (!unlimitedLives && lives <= 0 && !isGameOver) {
            failReason = FailReason.NO_LIVES;
            endGame();
        }
    }
    
    /**
     * Reduce lives by 1 (for wrong serve or expired order)
     */
    public void loseLife() {
        System.out.println("[GameState.loseLife] Called! Current state - Lives: " + lives + ", Unlimited: " + unlimitedLives);
        
        if (unlimitedLives) {
            System.out.println("[GameState.loseLife] Unlimited lives mode - ignoring life loss");
            return; // Unlimited lives, do nothing
        }
        
        if (lives > 0) {
            lives--;
            System.out.println("[GameState.loseLife] Life lost! Remaining lives: " + lives);
        }
        
        // If lives reach 0, end the game
        if (lives <= 0) {
            System.out.println("[GameState.loseLife] No lives remaining! Ending game...");
            failReason = FailReason.NO_LIVES;
            endGame();
        }
    }
    
    /**
     * Get current lives
     */
    public int getLives() {
        return lives;
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
     * Get fail reason
     */
    public FailReason getFailReason() {
        return failReason;
    }
    
    /**
     * Check if unlimited lives is enabled
     */
    public boolean isUnlimitedLives() {
        return unlimitedLives;
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
    
    /**
     * Reset game state (for playing again)
     */
    public void reset() {
        this.timer = new GameTimer(GameConfig.GAME_DURATION_SECONDS);
        this.score = new GameScore();
        this.isGameOver = false;
        this.isPassed = false;
        this.failReason = FailReason.NONE;
        
        // Use difficulty settings for lives
        GameSettings settings = GameSettings.getInstance();
        this.unlimitedLives = settings.isUnlimitedLives();
        this.lives = settings.getInitialLives();
        
        System.out.println("[GameState.reset] Game reset with difficulty: " + settings.getDifficulty().getDisplayName());
        System.out.println("[GameState.reset] Lives: " + this.lives + ", Unlimited: " + this.unlimitedLives);
    }
}
