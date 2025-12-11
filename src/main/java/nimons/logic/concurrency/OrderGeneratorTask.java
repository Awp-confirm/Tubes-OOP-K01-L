package nimons.logic.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;

import nimons.logic.order.OrderManager;

/**
 * Background thread task for generating orders periodically.
 * Demonstrates concurrency implementation.
 */
public class OrderGeneratorTask implements Runnable {
    
    private final OrderManager orderManager;
    private final long intervalMs;
    private final AtomicBoolean running;
    
    public OrderGeneratorTask(OrderManager orderManager, long intervalMs) {
        this.orderManager = orderManager;
        this.intervalMs = intervalMs;
        this.running = new AtomicBoolean(false);
    }
    
    @Override
    public void run() {
        running.set(true);
        System.out.println("[OrderGeneratorTask] Background thread started (interval: " + intervalMs + "ms)");
        
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // Wait for interval
                Thread.sleep(intervalMs);
                
                // Generate order (synchronized access)
                synchronized (orderManager) {
                    long currentTime = System.currentTimeMillis();
                    orderManager.trySpawnNewOrder(currentTime);
                    System.out.println("[OrderGeneratorTask] Order generation attempted by background thread");
                }
                
            } catch (InterruptedException e) {
                System.out.println("[OrderGeneratorTask] Thread interrupted, stopping...");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        running.set(false);
        System.out.println("[OrderGeneratorTask] Background thread stopped");
    }
    
    public void stop() {
        running.set(false);
    }
    
    public boolean isRunning() {
        return running.get();
    }
}
