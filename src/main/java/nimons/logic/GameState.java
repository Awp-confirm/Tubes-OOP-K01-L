package nimons.logic;

import nimons.core.GameConfig;
import nimons.core.GameSettings;
import nimons.logic.scoring.GameScore;

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
        
        
        GameSettings settings = GameSettings.getInstance();
        this.unlimitedLives = settings.isUnlimitedLives();
        this.lives = settings.getInitialLives();
        
        System.out.println("[GameState] Initialized with difficulty: " + settings.getDifficulty().getDisplayName());
        System.out.println("[GameState] Lives: " + this.lives + ", Unlimited: " + this.unlimitedLives);
    }
    
    
        
    public void update() {
        if (timer.isTimeUp() && !isGameOver) {
            failReason = FailReason.TIME_UP;
            endGame();
        }
        
        if (!unlimitedLives && lives <= 0 && !isGameOver) {
            failReason = FailReason.NO_LIVES;
            endGame();
        }
    }
    
    
        
    public void loseLife() {
        System.out.println("[GameState.loseLife] Called! Current state - Lives: " + lives + ", Unlimited: " + unlimitedLives);
        
        if (unlimitedLives) {
            System.out.println("[GameState.loseLife] Unlimited lives mode - ignoring life loss");
            return; 
        }
        
        if (lives > 0) {
            lives--;
            System.out.println("[GameState.loseLife] Life lost! Remaining lives: " + lives);
        }
        
        
        if (lives <= 0) {
            System.out.println("[GameState.loseLife] No lives remaining! Ending game...");
            failReason = FailReason.NO_LIVES;
            endGame();
        }
    }
    
    
    public int getLives() {
        return lives;
    }
    
    
        
    public void endGame() {
        isGameOver = true;
        isPassed = score.isPassed(passThreshold);
    }
    
    
        
    public boolean isGameOver() {
        return isGameOver;
    }
    
    
        
    public boolean isPassed() {
        return isPassed;
    }
    
    
        
    public void pause() {
        timer.pause();
    }
    
    
        
    public void resume() {
        timer.resume();
    }
    
    
    public GameTimer getTimer() {
        return timer;
    }
    
    
    public GameScore getScore() {
        return score;
    }
    
    
    public int getPassThreshold() {
        return passThreshold;
    }
    
    
    public FailReason getFailReason() {
        return failReason;
    }
    
    
        
    public boolean isUnlimitedLives() {
        return unlimitedLives;
    }
    
    
    public String getResultMessage() {
        if (isPassed) {
            return "PASS";
        } else {
            return "FAIL";
        }
    }
    
    
        
    public void reset() {
        this.timer = new GameTimer(GameConfig.GAME_DURATION_SECONDS);
        this.score = new GameScore();
        this.isGameOver = false;
        this.isPassed = false;
        this.failReason = FailReason.NONE;
        
        
        GameSettings settings = GameSettings.getInstance();
        this.unlimitedLives = settings.isUnlimitedLives();
        this.lives = settings.getInitialLives();
        
        System.out.println("[GameState.reset] Game reset with difficulty: " + settings.getDifficulty().getDisplayName());
        System.out.println("[GameState.reset] Lives: " + this.lives + ", Unlimited: " + this.unlimitedLives);
    }
}
