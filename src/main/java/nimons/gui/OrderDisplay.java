package nimons.gui;

import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import nimons.core.GameConfig;
import nimons.entity.item.IngredientState;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Order;

public class OrderDisplay {
    
    // Vertical card dimensions (changed from horizontal)
    private static final double CARD_WIDTH = 180;
    private static final double CARD_HEIGHT = 180;
    private static final double CARD_SPACING = 15;
    private static final double MARGIN_LEFT = 20; // Left margin for positioning
    
    // Internal card layout
    private static final double TIMER_BAR_HEIGHT = 12;
    private static final double DISH_ICON_SIZE = 70;
    private static final double INGREDIENT_ICON_SIZE = 32;
    
    public static void renderOrders(GraphicsContext gc, List<Order> orders, double windowWidth, double startY, Map<String, Image> itemImages) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        // Position on left side of screen
        double startX = MARGIN_LEFT;
        int maxDisplay = Math.min(3, orders.size());
        for (int i = 0; i < maxDisplay; i++) {
            double cardY = startY + (i * (CARD_HEIGHT + CARD_SPACING));
            renderOrderCard(gc, orders.get(i), startX, cardY, itemImages);
        }
    }
    
    /**
     * Render single order card vertically
     * Layout: [Timer Bar] [Dish Icon] [Ingredients Horizontal]
     */
    private static void renderOrderCard(GraphicsContext gc, Order order, double x, double y, Map<String, Image> itemImages) {
        if (order == null) {
            return;
        }
        
        // Get opacity for fade in/out animation
        double opacity = order.getOpacity();
        
        // Save current state
        gc.save();
        gc.setGlobalAlpha(opacity);
        
        // Background
        gc.setFill(Color.web("#220606"));
        gc.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);
        
        // Border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 8, 8);
        
        // Timer bar at top (full width)
        renderTimerBar(gc, order, x, y);
        
        // Dish icon/placeholder (centered)
        double dishIconX = x + (CARD_WIDTH - DISH_ICON_SIZE) / 2;
        double dishIconY = y + TIMER_BAR_HEIGHT + 20;
        renderDishIcon(gc, order, dishIconX, dishIconY, itemImages);
        
        // Ingredients displayed horizontally (centered below dish)
        double ingredientsY = dishIconY + DISH_ICON_SIZE + 30;
        renderIngredientsHorizontal(gc, order, x, ingredientsY, itemImages);
        
        // Restore graphics context
        gc.restore();
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
        
        // Progress bar
        gc.setFill(progressColor);
        gc.fillRect(barX, barY, barWidth * timeRatio, barHeight);
        
        // Border
        gc.setStroke(Color.web("#E8A36B"));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    

    private static void renderDishIcon(GraphicsContext gc, Order order, double x, double y, Map<String, Image> itemImages) {
        // Try to get dish image first
        Image dishImage = null;
        String recipeName = "";
        
        if (order.getRecipe() != null) {
            recipeName = order.getRecipe().getName();
            String recipeNameLower = recipeName.toLowerCase();
            
            // Map recipe names to image keys
            if (recipeNameLower.contains("ebi maki")) {
                dishImage = itemImages.get("ebi_maki");
            } else if (recipeNameLower.contains("kappa maki")) {
                dishImage = itemImages.get("kappa_maki");
            } else if (recipeNameLower.contains("sakana maki")) {
                dishImage = itemImages.get("sakana_maki");
            } else if (recipeNameLower.contains("fish cucumber roll")) {
                dishImage = itemImages.get("fish_cucumber_roll");
            }
        }
        
        // If we have a dish image, render it
        if (dishImage != null) {
            gc.drawImage(dishImage, x, y, DISH_ICON_SIZE, DISH_ICON_SIZE);
        } else {
            // Fallback: Dish background circle (placeholder)
            gc.setFill(Color.web("#3a0f0f"));
            gc.fillOval(x, y, DISH_ICON_SIZE, DISH_ICON_SIZE);
            
            // Border
            gc.setStroke(Color.web("#E8A36B"));
            gc.setLineWidth(2);
            gc.strokeOval(x, y, DISH_ICON_SIZE, DISH_ICON_SIZE);
            
            // Plate icon (inner circle)
            gc.setFill(Color.web("#D4A574"));
            gc.fillOval(x + 8, y + 8, DISH_ICON_SIZE - 16, DISH_ICON_SIZE - 16);
        }
        
        // Recipe name with larger font and centered
        if (!recipeName.isEmpty()) {
            gc.setFill(Color.web("#F2C38F"));
                gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 12));
            
            // Center text below icon (use center of icon as reference)
            double centerX = x + (DISH_ICON_SIZE / 2);
            double textY = y + DISH_ICON_SIZE + 18;
            
            // Display full recipe name
            gc.fillText(recipeName, centerX, textY);
            
            // Reset text alignment
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }
    
    /**
     * Render ingredients horizontally with actual images and names
     */
    private static void renderIngredientsHorizontal(GraphicsContext gc, Order order, double cardX, double y, Map<String, Image> itemImages) {
        if (order == null || order.getRecipe() == null) {
            return;
        }
        
        List<IngredientRequirement> requirements = order.getRecipe().getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return;
        }
        
        // Calculate total width to center ingredients
        int displayCount = Math.min(4, requirements.size()); // Max 4 ingredients
        double iconSpacing = 5;
        double totalWidth = (displayCount * INGREDIENT_ICON_SIZE) + ((displayCount - 1) * iconSpacing);
        double startX = cardX + (CARD_WIDTH - totalWidth) / 2;
        
        double currentX = startX;
        
        for (int i = 0; i < displayCount; i++) {
            IngredientRequirement req = requirements.get(i);
            
            // Get ingredient image key
            String ingredientName = req.getIngredientType().getSimpleName().toLowerCase();
            IngredientState state = req.getRequiredState();
            String imageKey = getIngredientImageKey(ingredientName, state);
            
            // Draw ingredient image if available
            if (itemImages != null && itemImages.containsKey(imageKey)) {
                Image img = itemImages.get(imageKey);
                if (img != null) {
                    gc.drawImage(img, currentX, y, INGREDIENT_ICON_SIZE, INGREDIENT_ICON_SIZE);
                }
            } else {
                // Fallback to colored box if image not found
                Color iconColor = getIngredientColor(ingredientName);
                gc.setFill(iconColor);
                gc.fillRoundRect(currentX, y, INGREDIENT_ICON_SIZE, INGREDIENT_ICON_SIZE, 4, 4);
                
                // Draw initial
                String initial = ingredientName.substring(0, 1).toUpperCase();
                gc.setFill(Color.WHITE);
                    gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 14));
            }
            
            // Border around ingredient
            gc.setStroke(Color.web("#E8A36B"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(currentX, y, INGREDIENT_ICON_SIZE, INGREDIENT_ICON_SIZE, 4, 4);
            
            // Draw ingredient name below icon
            String displayName = capitalizeFirst(ingredientName);
            gc.setFill(Color.web("#E8A36B"));
                gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.NORMAL, 8));
            double textX = currentX + (INGREDIENT_ICON_SIZE / 2);
            double textY = y + INGREDIENT_ICON_SIZE + 10;
            gc.fillText(displayName, textX, textY);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
            
            currentX += INGREDIENT_ICON_SIZE + iconSpacing;
        }
        
        // Show "+N" if there are more ingredients
        if (requirements.size() > displayCount) {
            gc.setFill(Color.web("#E8A36B"));
                gc.setFont(Font.font(GameConfig.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 10));
        }
    }
    
    /**
     * Capitalize first letter of string
     */
    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Get image key for ingredient based on name and state
     */
    private static String getIngredientImageKey(String ingredientName, IngredientState state) {
        String stateStr = state.toString().toLowerCase();
        
        // Map state to image suffix
        if (stateStr.equals("raw")) {
            return ingredientName + "_raw";
        } else if (stateStr.equals("chopped")) {
            return ingredientName + "_chopped";
        } else if (stateStr.equals("cooked")) {
            return ingredientName + "_cooked";
        } else if (stateStr.equals("burned")) {
            return ingredientName + "_burned";
        }
        
        return ingredientName + "_raw"; // Default
    }
    
    /**
     * Get color for ingredient type
     */
    private static Color getIngredientColor(String ingredientName) {
        if (ingredientName == null) {
            return Color.web("#888888");
        }
        
        String lower = ingredientName.toLowerCase();
        
        if (lower.contains("fish")) {
            return Color.web("#ff6b6b"); // Red
        } else if (lower.contains("rice")) {
            return Color.web("#fff3bf"); // Yellow
        } else if (lower.contains("cucumber")) {
            return Color.web("#51cf66"); // Green
        } else if (lower.contains("nori") || lower.contains("seaweed")) {
            return Color.web("#2d3436"); // Dark green/black
        } else if (lower.contains("shrimp") || lower.contains("prawn")) {
            return Color.web("#fab1a0"); // Pink
        } else {
            return Color.web("#a29bfe"); // Purple (default)
        }
    }
}