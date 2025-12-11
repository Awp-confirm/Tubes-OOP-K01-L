package nimons.logic.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nimons.core.GameConfig;
import nimons.entity.item.Dish;
import nimons.entity.order.Order;
import nimons.entity.order.OrderStatus;
import nimons.entity.order.Recipe;
import nimons.gui.GameScreen;
import nimons.logic.GameState;

public class OrderManager {
    
    private static OrderManager instance;
    private final List<Order> activeOrders;
    private final List<Recipe> availableRecipes;
    private int nextOrderIndex = 1;
    private final Random random = new Random();
    private static final int MAX_ACTIVE_ORDERS = 3;
    private long lastOrderTime = 0;
    private static final long ORDER_SPAWN_INTERVAL = 20000; // 20 detik spawn order baru
    
    private OrderManager() {
        this.activeOrders = new ArrayList<>();
        this.availableRecipes = new ArrayList<>();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }
    
    /**
     * Set available recipes untuk level ini
     */
    public void setAvailableRecipes(List<Recipe> recipes) {
        this.availableRecipes.clear();
        this.availableRecipes.addAll(recipes);
    }
    
    /**
     * Update order timer dan cek timeout
     */
    public void update(long deltaTimeMs) {
        final GameState gameState = getGameState();
        final double deltaSeconds = deltaTimeMs / 1000.0;
        
        activeOrders.removeIf(order -> {
            order.updateAnimation();
            
            if (order.getStatus() == OrderStatus.ACTIVE) {
                double newRemaining = order.getRemainingTimeSeconds() - deltaSeconds;
                order.setRemainingTimeSeconds(Math.max(0, newRemaining));
                
                if (newRemaining <= 0) {
                    order.setStatus(OrderStatus.FAILED);
                    if (gameState != null) {
                        gameState.loseLife();
                    }
                }
                return false;
            }
            
            return order.isFullyFaded();
        });
    }
    
    private GameState getGameState() {
        GameScreen gameScreen = GameScreen.getInstance();
        return gameScreen != null ? gameScreen.getGameState() : null;
    }
    
    /**
     * Check dan spawn order baru jika interval cukup dan belum max
     */
    public void trySpawnNewOrder(long currentTimeMs) {
        if (activeOrders.size() < MAX_ACTIVE_ORDERS && 
            (currentTimeMs - lastOrderTime) >= ORDER_SPAWN_INTERVAL) {
            spawnRandomOrder();
            lastOrderTime = currentTimeMs;
        }
    }
    
    /**
     * Spawn random order dari available recipes
     */
    private void spawnRandomOrder() {
        if (availableRecipes.isEmpty()) {
            return;
        }
        
        Recipe randomRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        int reward = 100 + random.nextInt(100); // 100-200 reward
        int penalty = -50;
        int timeLimit = GameConfig.ORDER_DURATION_SECONDS;
        
        Order newOrder = new Order(
            nextOrderIndex++,
            randomRecipe,
            reward,
            penalty,
            timeLimit,
            (double) timeLimit,
            OrderStatus.ACTIVE
        );
        
        activeOrders.add(newOrder);
    }

    public void addOrder(Order order) {
        order.setIndex(nextOrderIndex++);
        order.setStatus(OrderStatus.ACTIVE);
        activeOrders.add(order);
    }

    public boolean validateOrder(Dish dish) {
        if (dish == null) {
            return false;
        }
        
        // Check if any active order matches this dish
        for (Order order : activeOrders) {
            if (order.getRecipe() != null && 
                order.getRecipe().getName().equalsIgnoreCase(dish.getName())) {
                return true;
            }
        }
        
        // If no specific order found, check if dish is valid (not empty)
        return dish.getComponents() != null && !dish.getComponents().isEmpty();
    }

    /**
     * Complete order - cari order terlebih awal dengan recipe yang sama
     */
    public Order completeOrder(Dish dish) {
        if (dish == null || dish.getName() == null) {
            return null;
        }
        
        // Find earliest order (lowest index) yang recipe-nya match
        Order earliestMatch = null;
        for (Order order : activeOrders) {
            if (order.getStatus() == OrderStatus.ACTIVE &&
                order.getRecipe() != null && 
                order.getRecipe().getName().equalsIgnoreCase(dish.getName())) {
                if (earliestMatch == null || order.getIndex() < earliestMatch.getIndex()) {
                    earliestMatch = order;
                }
            }
        }
        
        if (earliestMatch != null) {
            earliestMatch.setStatus(OrderStatus.COMPLETED);
            activeOrders.remove(earliestMatch);
        }
        
        return earliestMatch;
    }

    public List<Order> getActiveOrders() {
        return new ArrayList<>(activeOrders);
    }
    
    public int getActiveOrderCount() {
        return activeOrders.size();
    }
    
    public void reset() {
        activeOrders.clear();
        nextOrderIndex = 1;
        lastOrderTime = 0;
    }
}
