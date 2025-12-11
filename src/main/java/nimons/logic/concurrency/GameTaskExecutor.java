package nimons.logic.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages concurrent tasks in the game using ExecutorService.
 * Demonstrates thread pool management and task scheduling.
 */
public class GameTaskExecutor {
    
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicInteger taskCounter;
    
    private static GameTaskExecutor instance;
    
    private GameTaskExecutor() {
        // Thread pool with custom thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "GameTask-" + threadNumber.getAndIncrement());
                thread.setDaemon(true); // Daemon threads don't prevent JVM shutdown
                return thread;
            }
        };
        
        this.executorService = Executors.newFixedThreadPool(3, threadFactory);
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, threadFactory);
        this.taskCounter = new AtomicInteger(0);
        
        System.out.println("[GameTaskExecutor] Initialized with thread pool");
    }
    
    public static synchronized GameTaskExecutor getInstance() {
        if (instance == null) {
            instance = new GameTaskExecutor();
        }
        return instance;
    }
    
    /**
     * Submit a task for immediate execution
     */
    public Future<?> submitTask(Runnable task) {
        taskCounter.incrementAndGet();
        return executorService.submit(task);
    }
    
    /**
     * Submit a task with a return value
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        taskCounter.incrementAndGet();
        return executorService.submit(task);
    }
    
    /**
     * Schedule a task with fixed delay
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
    
    /**
     * Schedule a one-time task
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    /**
     * Get total tasks submitted
     */
    public int getTotalTasksSubmitted() {
        return taskCounter.get();
    }
    
    /**
     * Shutdown executor gracefully
     */
    public void shutdown() {
        System.out.println("[GameTaskExecutor] Shutting down...");
        executorService.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[GameTaskExecutor] Shutdown complete");
    }
    
    /**
     * Reset singleton instance
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }
}
