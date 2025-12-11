package nimons.gui;

import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Order;

public class OrderDisplay {
    
    // Horizontal card dimensions
    private static final double CARD_WIDTH = 280;
    private static final double CARD_HEIGHT = 100;
    private static final double CARD_SPACING = 15;
    private static final double MARGIN = 20;
    
    // Internal card layout
    private static final double TIMER_BAR_HEIGHT = 12;
    private static final double DISH_ICON_SIZE = 50;
    private static final double INGREDIENT_ICON_SIZE = 30;
    
    public static void renderOrders(GraphicsContext gc, List<Order> orders, double windowWidth, double startY) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        double margin = 20;
        double startX = margin;
        int maxDisplay = Math.min(5, orders.size());
        for (int i = 0; i < maxDisplay; i++) {
            double cardX = startX + (i * (CARD_WIDTH + CARD_SPACING));
            renderOrderCard(gc, orders.get(i), cardX, startY);
        }
    }
    
    /**
     * Render single order card horizontally
     * Layout: [Timer Bar] [Dish Icon] [Ingredients Horizontal]
     */
    private static void renderOrderCard(GraphicsContext gc, Order order, double x, double y) {
        if (order == null) {
            return;
        }
        
        // Background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);
        
        // Border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);
        
        // Timer bar at top (full width)
        renderTimerBar(gc, order, x, y);
        
        // Left section: Dish icon (placeholder)
        double dishIconX = x + 10;
        double dishIconY = y + TIMER_BAR_HEIGHT + 10;
        renderDishIcon(gc, order, dishIconX, dishIconY);
        
        // Middle section: Recipe name
        double recipeNameX = dishIconX + DISH_ICON_SIZE + 12;
        double recipeNameY = dishIconY + 15;
        renderRecipeName(gc, order, recipeNameX, recipeNameY);
        
        // Right section: Ingredients icons horizontal
        double ingredientsX = recipeNameX;
        double ingredientsY = recipeNameY + 20;
        renderIngredientsHorizontal(gc, order, ingredientsX, ingredientsY);
    }
    
    /**
     * Render timer bar at top of card
     */
    private static void renderTimerBar(GraphicsContext gc, Order order, double x, double y) {
        double barX = x + 5;
        double barY = y + 5;
        double barWidth = CARD_WIDTH - 10;
        double barHeight = TIMER_BAR_HEIGHT;
        
        // Background (dark gray)
        gc.setFill(Color.web("#404040"));
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Calculate time ratio
        double remainingTime = order.getRemainingTimeSeconds();
        double timeLimit = order.getTimeLimitSeconds();
        double timeRatio = timeLimit > 0 ? Math.max(0, Math.min(1.0, remainingTime / timeLimit)) : 0;
        
        // Progress bar color based on remaining time
        Color progressColor;
        if (timeRatio > 0.5) {
            progressColor = Color.web("#2ecc71"); // Green
        } else if (timeRatio > 0.25) {
            progressColor = Color.web("#f39c12"); // Orange
        } else {
            progressColor = Color.web("#e74c3c"); // Red
        }
        
        // Draw progress bar
        gc.setFill(progressColor);
        gc.fillRect(barX, barY, barWidth * timeRatio, barHeight);
        
        // Border
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
        
        // Time text on the right of bar
        gc.setFill(Color.web("#F2C38F"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.RIGHT);
        int seconds = (int) Math.ceil(remainingTime);
        gc.fillText(seconds + "s", barX + barWidth - 3, barY + barHeight - 2);
    }
    
    /**
     * Render dish icon (placeholder)
     */
    private static void renderDishIcon(GraphicsContext gc, Order order, double x, double y) {
        // Placeholder: colored circle
        gc.setFill(Color.web("#D4A574"));
        gc.fillOval(x, y, DISH_ICON_SIZE, DISH_ICON_SIZE);
        
        // Border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x, y, DISH_ICON_SIZE, DISH_ICON_SIZE);
        
        // Icon text (D for Dish)
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("D", x + DISH_ICON_SIZE / 2, y + DISH_ICON_SIZE / 2 + 5);
    }
    
    /**
     * Render recipe name
     */
    private static void renderRecipeName(GraphicsContext gc, Order order, double x, double y) {
        if (order.getRecipe() == null) {
            return;
        }
        
        String dishName = order.getRecipe().getName();
        gc.setFill(Color.web("#E8A36B"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(dishName, x, y);
    }
    
    /**
     * Render ingredients as horizontal list
     */
    private static void renderIngredientsHorizontal(GraphicsContext gc, Order order, double x, double y) {
        if (order.getRecipe() == null || order.getRecipe().getRequirements() == null) {
            return;
        }
        
        List<IngredientRequirement> requirements = order.getRecipe().getRequirements();
        double currentX = x;
        double iconSpacing = INGREDIENT_ICON_SIZE + 5;
        
        // Limit display ke 3 ingredients (untuk space)
        int maxDisplay = Math.min(3, requirements.size());
        
        for (int i = 0; i < maxDisplay; i++) {
            double iconX = currentX;
            double iconY = y;
            
            // Ingredient icon (small square)
            IngredientRequirement req = requirements.get(i);
            gc.setFill(getIngredientColor(req.getIngredientType().getSimpleName()));
            gc.fillRoundRect(iconX, iconY, INGREDIENT_ICON_SIZE, INGREDIENT_ICON_SIZE, 3, 3);
            
            // Ingredient initial
            String ingredientName = req.getIngredientType().getSimpleName();
            String initial = ingredientName.substring(0, 1).toUpperCase();
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(initial, iconX + INGREDIENT_ICON_SIZE / 2, 
                       iconY + INGREDIENT_ICON_SIZE / 2 + 3);
            
            currentX += iconSpacing;
        }
    }
    
    private static Color getIngredientColor(String ingredientName) {
        // Return color based on ingredient type
        if (ingredientName == null) {
            return Color.web("#888888");
        }
        
        String lower = ingredientName.toLowerCase();
        
        if (lower.contains("salmon") || lower.contains("tuna")) {
            return Color.web("#ff6b6b"); // Red
        } else if (lower.contains("rice")) {
            return Color.web("#fff3bf"); // Yellow
        } else if (lower.contains("avocado") || lower.contains("cucumber")) {
            return Color.web("#51cf66"); // Green
        } else if (lower.contains("soy") || lower.contains("sauce")) {
            return Color.web("#495057"); // Dark gray
        } else if (lower.contains("egg") || lower.contains("tobiko")) {
            return Color.web("#ffa500"); // Orange
        } else {
            return Color.web("#a29bfe"); // Purple (default)
        }
    }
}
