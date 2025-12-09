package nimons.logic;

/**
 * Manages game timer functionality
 */
public class GameTimer {
    private long startTime;
    private long pausedTime = 0; // Total time spent paused
    private long pauseStartTime = 0;
    private boolean isPaused = false;
    private long durationMs; // Total duration in milliseconds
    
    public GameTimer(long durationSeconds) {
        this.durationMs = durationSeconds * 1000;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Pause the timer
     */
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Resume the timer
     */
    public void resume() {
        if (isPaused) {
            isPaused = false;
            pausedTime += System.currentTimeMillis() - pauseStartTime;
        }
    }
    
    /**
     * Get remaining time in milliseconds
     */
    public long getRemainingTimeMs() {
        long elapsedTime = getElapsedTimeMs();
        return Math.max(0, durationMs - elapsedTime);
    }
    
    /**
     * Get elapsed time in milliseconds
     */
    public long getElapsedTimeMs() {
        long currentPausedTime = pausedTime;
        if (isPaused) {
            currentPausedTime += System.currentTimeMillis() - pauseStartTime;
        }
        return System.currentTimeMillis() - startTime - currentPausedTime;
    }
    
    /**
     * Get remaining time in seconds
     */
    public long getRemainingTimeSeconds() {
        return getRemainingTimeMs() / 1000;
    }
    
    /**
     * Get elapsed time in seconds
     */
    public long getElapsedTimeSeconds() {
        return getElapsedTimeMs() / 1000;
    }
    
    /**
     * Check if time is up
     */
    public boolean isTimeUp() {
        return getRemainingTimeMs() <= 0;
    }
    
    /**
     * Check if timer is paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Get total duration in seconds
     */
    public long getTotalDurationSeconds() {
        return durationMs / 1000;
    }
    
    /**
     * Format time as MM:SS
     */
    public String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    /**
     * Get formatted remaining time
     */
    public String getFormattedRemainingTime() {
        return formatTime(getRemainingTimeSeconds());
    }
    
    /**
     * Get formatted elapsed time
     */
    public String getFormattedElapsedTime() {
        return formatTime(getElapsedTimeSeconds());
    }
}
