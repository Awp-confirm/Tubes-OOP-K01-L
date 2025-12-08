package nimons.logic.order;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.item.Dish;
import nimons.entity.order.Order;

public class OrderManager {
    
    private static OrderManager instance;
    private List<Order> activeOrders;

    private OrderManager() {
        this.activeOrders = new ArrayList<>();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public void addOrder(Order order) {
        activeOrders.add(order);
    }

    public boolean validateOrder(Dish dish) {
        // TODO: Implement order validation logic
        return false;
    }

    public void completeOrder(Order order) {
        activeOrders.remove(order);
    }

    public List<Order> getActiveOrders() {
        return activeOrders;
    }

    public void update(float deltaTime) {
        // TODO: Update order timers
    }
}
