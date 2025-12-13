package nimons.logic.concurrency;

import nimons.entity.order.Order;

public class OrderProcessingResult {
    private final Order order;
    private final boolean success;
    private final String message;
    
    public OrderProcessingResult(Order order, boolean success, String message) {
        this.order = order;
        this.success = success;
        this.message = message;
    }
    
    public Order getOrder() {
        return order;
    }
    
        
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
}
