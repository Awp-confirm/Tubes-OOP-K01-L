package nimons.core;

/**
 * Game settings singleton to manage difficulty and other preferences
 */
public class GameSettings {
    
    private static GameSettings instance;
    
    public enum Difficulty {
        EASY("Easy", -1),
        NORMAL("Normal", 3),
        HARDCORE("Hardcore", 1);
        
        private final String displayName;
        private final int lives;
        
        Difficulty(String displayName, int lives) {
            this.displayName = displayName;
            this.lives = lives;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLives() {
            return lives;
        }
        
        public boolean isUnlimitedLives() {
            return lives < 0;
        }
    }
    
    private Difficulty currentDifficulty;
    
    private GameSettings() {
        this.currentDifficulty = Difficulty.NORMAL;
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    public Difficulty getDifficulty() {
        return currentDifficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        System.out.println("[GameSettings] Difficulty changed to: " + difficulty.getDisplayName() + " (" + difficulty.getLives() + " lives)");
        this.currentDifficulty = difficulty;
    }
    
    public int getInitialLives() {
        return currentDifficulty.getLives();
    }
    
    public boolean isUnlimitedLives() {
        return currentDifficulty.isUnlimitedLives();
    }
}
