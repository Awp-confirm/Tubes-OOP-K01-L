package nimons.logic;

public class GameTimer {
    private long startTime;
    private long pausedTime = 0; 
    private long pauseStartTime = 0;
    private boolean isPaused = false;
    private long durationMs; 
    
    public GameTimer(long durationSeconds) {
        this.durationMs = durationSeconds * 1000;
        this.startTime = System.currentTimeMillis();
    }
    
    
        
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }
    
    
        
    public void resume() {
        if (isPaused) {
            isPaused = false;
            pausedTime += System.currentTimeMillis() - pauseStartTime;
        }
    }
    
    
    public long getRemainingTimeMs() {
        long elapsedTime = getElapsedTimeMs();
        return Math.max(0, durationMs - elapsedTime);
    }
    
    
    public long getElapsedTimeMs() {
        long currentPausedTime = pausedTime;
        if (isPaused) {
            currentPausedTime += System.currentTimeMillis() - pauseStartTime;
        }
        return System.currentTimeMillis() - startTime - currentPausedTime;
    }
    
    
    public long getRemainingTimeSeconds() {
        return getRemainingTimeMs() / 1000;
    }
    
    
    public long getElapsedTimeSeconds() {
        return getElapsedTimeMs() / 1000;
    }
    
    
        
    public boolean isTimeUp() {
        return getRemainingTimeMs() <= 0;
    }
    
    
        
    public boolean isPaused() {
        return isPaused;
    }
    
    
    public long getTotalDurationSeconds() {
        return durationMs / 1000;
    }
    
    
        
    public String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    
    public String getFormattedRemainingTime() {
        return formatTime(getRemainingTimeSeconds());
    }
    
    
    public String getFormattedElapsedTime() {
        return formatTime(getElapsedTimeSeconds());
    }
}
