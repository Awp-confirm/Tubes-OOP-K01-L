package nimons.logic.concurrency;

import java.util.concurrent.Callable;

import nimons.entity.order.Order;

public class AsyncOrderProcessor implements Callable<OrderProcessingResult> {
    
    private final Order order;
    private final long processingTimeMs;
    
    public AsyncOrderProcessor(Order order, long processingTimeMs) {
        this.order = order;
        this.processingTimeMs = processingTimeMs;
    }
    
    @Override
        
    public OrderProcessingResult call() throws Exception {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Processing order: " + order.getRecipe().getName());
        
        try {
            
            Thread.sleep(processingTimeMs);
            
            
            if (order.getRemainingTimeSeconds() <= 0) {
                return new OrderProcessingResult(order, false, "Order expired during processing");
            }
            
            return new OrderProcessingResult(order, true, "Order processed successfully");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new OrderProcessingResult(order, false, "Processing interrupted");
        }
    }
}
